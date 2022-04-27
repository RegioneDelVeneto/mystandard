/**
 *     My Standard
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.myp3.mystd.repository.jena.impl;

import it.regioneveneto.myp3.mybox.RepositoryAccessException;
import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardEntityOperationEnum;
import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardIndividualOperationEnum;
import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardStatoEnum;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardMyPortalFilter;
import it.regioneveneto.myp3.mystd.bean.generic.MyStandardQueryRequest;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntity;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntityPropertyIndividual;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardFunctionalPropertyIndividual;
import it.regioneveneto.myp3.mystd.bean.owl.OClass;
import it.regioneveneto.myp3.mystd.bean.owl.OModel;
import it.regioneveneto.myp3.mystd.bean.owl.OProperty;
import it.regioneveneto.myp3.mystd.bean.owl.ORelation;
import it.regioneveneto.myp3.mystd.config.MyStandardConfig;
import it.regioneveneto.myp3.mystd.config.OwlJenaConfig;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.query.MyStandardQuery;
import it.regioneveneto.myp3.mystd.repository.EntityDataRepository;
import it.regioneveneto.myp3.mystd.repository.jena.JenaRepositoryType;
import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.service.AttachmentsService;
import it.regioneveneto.myp3.mystd.service.StoricoService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import it.regioneveneto.myp3.mystd.utils.MyStandardUtil;
import org.apache.commons.io.IOUtils;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JenaEntityDataRepository implements EntityDataRepository {

    private static final Logger LOG = LoggerFactory.getLogger(JenaEntityDataRepository.class);

    public static final String PREFIX_MYSTD_DATA = "https://mystandard.regione.veneto.it/onto/BPO_data#";
    public static final String PREFIX_MYSTD = "https://mystandard.regione.veneto.it/onto/BPO#";
    public static final String PREFIX_MYSTD_TSI = "https://mystandard.regione.veneto.it/onto/TSI#";


    public static final String PREFIX_ONTOPIA_CLV = "https://w3id.org/italia/onto/CLV/";
    public static final String PREFIX_ONTOPIA_COV = "https://w3id.org/italia/onto/COV/";
    public static final String PREFIX_ONTOPIA_SM = "https://w3id.org/italia/onto/SM/";
    public static final String PREFIX_ONTOPIA_TI = "https://w3id.org/italia/onto/TI/";
    public static final String PREFIX_RDF_TYPE = "http://www.w3.org/1999/02/";
    public static final String PREFIX_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String PREFIX_OWL = "http://www.w3.org/2002/07/owl#";
    public static final String PREFIX_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String PREFIX_XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String PREFIX_THING = "https://w3id.org/italia/onto/l0/";
    public static final String PREFIX_LEGAL = "https://w3id.org/italia/controlled-vocabulary/classifications-for-organizations/legal-status#";


    private static final List<String> PREFIX_LIST = Arrays.asList(PREFIX_THING, PREFIX_MYSTD_TSI, PREFIX_MYSTD, PREFIX_MYSTD_DATA, PREFIX_ONTOPIA_CLV, PREFIX_ONTOPIA_COV, PREFIX_ONTOPIA_SM, PREFIX_ONTOPIA_TI, PREFIX_RDF_TYPE);
    public static final String NESSUNA_SPECIALIZZAZIONE = "Nessuna specializzazione";
    public static final Duration NUMBER_DAYS_NEW_IN_CATALOG = Duration.ofDays(5);


    private OwlJenaConfig owlJenaConfig;
    private MyStandardConfig mystandardConfig;
    private MyStandardProperties myStandardProperties;
    private AttachmentsService attachmentsService;
    private JenaRepositoryType jenaRepositoryType;
    private StoricoService storicoService;
    private MessageSource messageSource;

    public JenaEntityDataRepository(JenaRepositoryType jenaRepositoryType, OwlJenaConfig owlJenaConfig, MyStandardConfig mystandardConfig,
                                    MyStandardProperties myStandardProperties, AttachmentsService attachmentsService, StoricoService storicoService,
                                    MessageSource messageSource) {
        this.jenaRepositoryType = jenaRepositoryType;
        this.owlJenaConfig = owlJenaConfig;
        this.mystandardConfig = mystandardConfig;
        this.myStandardProperties = myStandardProperties;
        this.attachmentsService = attachmentsService;
        this.storicoService = storicoService;
        this.messageSource = messageSource;
    }


    /**
     * Si ottiene le informazioni della classe by entityType dal Model
     * @param entityType, entity type da cui tornare info sulla classe
     * @return informazioni classe
     */
    private OClass getOClassFromModel(String entityType) {
        OModel oModel = mystandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        return oModelClasses.entrySet().stream()
                .filter(entry -> entityType.equals(entry.getValue().getLocalName()))
                .findFirst().get().getValue();
    }

    @Override
    public List<JSONObject> getAll(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException, URISyntaxException {
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        List<JSONObject> individuals = new ArrayList<>();

        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            OClass oClass = getOClassFromModel(entityType);
            if (oClass != null) {

                String owlPrefix = oClass.getPrefix();
                String queryAllByEntity = MyStandardQuery.getQueryAllByEntity(myStandardProperties, owlPrefix, dominio, entityType, filter);

                conn.querySelect(queryAllByEntity, (querySolution) -> {
                    JSONObject data = new JSONObject();//Si aggiungono i dati dell'entità

                    data.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.VERSIONE_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.STATO_COLUMN_KEY) != null ?
                            messageSource.getMessage(getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()), null, getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()) , null) : "");
                    data.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.NAME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.NAME_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY) != null ?
                            messageSource.getMessage(getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()), null, getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()) , null) : "");
                    data.put(MyStandardConstants.DEFINITA_DA_COLUMN_KEY, querySolution.get(MyStandardConstants.IPA_CODE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.IPA_CODE_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.ID_ENTITA_COLUMN_KEY, owlPrefix + data.getString(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY) + "_" + data.getString(MyStandardConstants.VERSIONE_COLUMN_KEY));

                    individuals.add(data);
                });


            } else {
                throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
            }

        } catch (Exception e) {
            LOG.error("MyStandard - Errore generico nel recupero dei dati RDF", e);
            throw e;
        }

        return individuals;
    }


    @Override
    public Integer countAll(String dominio, String entityType, MyStandardFilter filter) {
        final Integer[] totalRecords = new Integer[1];
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            OClass oClass = getOClassFromModel(entityType);
            if (oClass != null) {

                String owlPrefix = oClass.getPrefix();
                String queryCountAllByEntity = MyStandardQuery.getQueryCountAllByEntity(myStandardProperties, owlPrefix, dominio, entityType, filter);

                conn.querySelect(queryCountAllByEntity, (querySolution) -> {
                    totalRecords[0] = querySolution.get(MyStandardConstants.COUNT_INDIVIDUALS_KEY) != null ? Integer.parseInt(querySolution.getLiteral(MyStandardConstants.COUNT_INDIVIDUALS_KEY).getString()) : 0;

                });

            } else {
                throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
            }

        } catch (Exception e) {
            LOG.error("MyStandard - Errore nel recupero dei dati RDF", e);
        }

        return totalRecords[0];
    }



    @Override
    public JSONObject getSingleEntityData(String entityType, String codice, Integer versione) throws MyStandardException, IOException, URISyntaxException {
        JSONObject individual = new JSONObject();

        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            OClass oClass = getOClassFromModel(entityType);

            if (oClass != null) {//

                String owlPrefix = oClass.getPrefix();

                //Si aggiungono in oggetto individual le info di dettaglio (data property, object property enumeration e entityproperty)
                populateDetailObject(owlPrefix, codice, versione, individual, conn);

                //Si aggiungono in oggetto individual le info di tabs
                populateTabsObject(owlPrefix, codice, versione, individual, conn);

            } else {
                throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
            }


        } catch (Exception e) {
            LOG.error("MyStandard - Errore generico nel recupero dei dati RDF", e);
            throw e;
        }

        return individual;
    }

    /**
     * Si popola oggetto individual con le informazioni delle relazioni dell'entità
     * @param owlPrefix, prefisso da utlizzare nella query
     * @param codice, codice dell'entià di cui ricavare le relazioni
     * @param versione, versione dell'entità di cui ricavare le relazioni
     * @param individual, object contenente i dati del dettaglio
     * @param conn, connessione rdf
     */
    private void populateTabsObject(String owlPrefix, String codice, Integer versione, JSONObject individual, RDFConnection conn) {

        //Si prende la queery SPARQL

        JSONArray tabsArray = new JSONArray();

        try {
            String queryFunctionalRelations = MyStandardQuery.getQueryFunctionalRelations(owlPrefix, codice, versione);
            conn.querySelect(queryFunctionalRelations, (querySolution) -> {

                RDFNode objProp = querySolution.get(MyStandardConstants.SPARQL_OBJ_PROP_COLUMN_KEY);//Nome della object property
                if (objProp != null) {
                    String objectPropKey = getStringWithoutPrefix(objProp.toString());//Replace di prefix
                    JSONObject tabObject = getJsonObjectIndividualName(tabsArray, MyStandardConstants.CUSTOM_TAB_NAME, objectPropKey);
                    if (tabObject == null) {//Se nell'array è la prima volta che si aggiunge info su relazione objectPropKey
                        tabObject = new JSONObject();
                        tabObject.put(MyStandardConstants.CUSTOM_TAB_NAME, objectPropKey);//Creazione object per objectPropKey
                        tabObject.put(MyStandardConstants.FUNCTIONAL_PROPERTY_IRI, objProp.toString());

                        JSONArray componentArray = new JSONArray();//Creazione di un array perchè per objectPropKey posso avere più "record"
                        JSONObject component = new JSONObject();
                        component.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.SPARQL_VERSIONE_COLUMN_KEY).getString()) : "");
                        component.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_CODICE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.SPARQL_CODICE_COLUMN_KEY).getString()) : "");
                        component.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_STATO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.SPARQL_STATO_COLUMN_KEY).getURI())  : "");
                        component.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_NOME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.SPARQL_NOME_COLUMN_KEY).getString()) : "");
                        component.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_TIPO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.SPARQL_TIPO_COLUMN_KEY).getURI()) : "");
                        component.put(MyStandardConstants.FUNCTIONAL_PROP_TARGET_INDIVIDUALS_IRI, querySolution.get(MyStandardConstants.SPARQL_VALUE_COLUMN_KEY) != null ? querySolution.getResource(MyStandardConstants.SPARQL_VALUE_COLUMN_KEY).getURI() : "" );


                        componentArray.put(component);
                        tabObject.put(MyStandardConstants.COMPONENTS_KEY, componentArray);//aggiunta array relazioni in oggetto MyStandardConstants.COMPONENTS_KEY

                        tabsArray.put(tabObject);


                    } else {//Se l'oggetto esiste, aggiungo la relazione all'array esistente components
                        JSONArray componentArray = tabObject.getJSONArray(MyStandardConstants.COMPONENTS_KEY);
                        JSONObject component = new JSONObject();
                        component.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.SPARQL_VERSIONE_COLUMN_KEY).getString()) : "");
                        component.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_CODICE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.SPARQL_CODICE_COLUMN_KEY).getString()) : "");
                        component.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_STATO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.SPARQL_STATO_COLUMN_KEY).getURI())  : "");
                        component.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_NOME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.SPARQL_NOME_COLUMN_KEY).getString()): "");
                        component.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.SPARQL_TIPO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.SPARQL_TIPO_COLUMN_KEY).getURI()) : "");
                        component.put(MyStandardConstants.FUNCTIONAL_PROP_TARGET_INDIVIDUALS_IRI, querySolution.get(MyStandardConstants.SPARQL_VALUE_COLUMN_KEY) != null ? querySolution.getResource(MyStandardConstants.SPARQL_VALUE_COLUMN_KEY).getURI() : "" );

                        componentArray.put(component);


                    }

                    //Si aggiungono le relazioni "speciali" come dataproperty
                    setSpecialRelationAsDataProperty(individual, objProp.toString(), objectPropKey, querySolution);

                }
            });

            //Se non ci sono le relazioni "speciali" si aggiungono con dei valori di default
            setDefaultValueSpecialRelations(individual);

            individual.put(MyStandardConstants.TABS_WRAPPER_LABEL, tabsArray);

        } catch (Exception e) {
            LOG.error("MyStandard - Errore generico in esecuzione query SPARQL", e);
        }




    }

    /**
     * Se non sono state aggiunte relazioni "speciali" come data property, lo si fa manualmente con dei valori di default
     * @param individual, json a cui aggiungere le relazioni come data property
     */
    private void setDefaultValueSpecialRelations(JSONObject individual) {
        List<String> specialRelations = Arrays.asList(myStandardProperties.getOwl().getDefinitaDaUri(), myStandardProperties.getOwl().getSpecializzaUri());

        for (String specialRelation : specialRelations) {
            if (!individual.has(specialRelation)) {
                JSONObject relationDataProperty = new JSONObject();
                relationDataProperty.put(MyStandardConstants.DATA_PROPERTY_IRI, specialRelation);
                relationDataProperty.put(MyStandardConstants.DATA_PROPERTY_LOCAL_NAME, getStringWithoutPrefix(specialRelation));

                if (myStandardProperties.getOwl().getDefinitaDaUri().equalsIgnoreCase(specialRelation)) {
                    relationDataProperty.put(MyStandardConstants.DATA_PROPERTY_VALUE, myStandardProperties.getEnteNazionale());
                } else if (myStandardProperties.getOwl().getSpecializzaUri().equalsIgnoreCase(specialRelation)) {
                    relationDataProperty.put(MyStandardConstants.DATA_PROPERTY_VALUE, NESSUNA_SPECIALIZZAZIONE);
                }


                individual.put(specialRelation, relationDataProperty);
            }
        }

    }

    /**
     * Si aggiungono le relazioni speciali come data property
     * @param individual, oggetto a cui aggiungere le relazioni
     * @param relationKeyIRI, IRI relazioni
     * @param relationKeyLocalName, local name relazione
     * @param querySolution, dati della query
     */
    private void setSpecialRelationAsDataProperty(JSONObject individual, String relationKeyIRI, String relationKeyLocalName, QuerySolution querySolution) {

        String relationValue = querySolution.get(MyStandardConstants.SPARQL_NOME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.SPARQL_NOME_COLUMN_KEY).getString()) : "";
        List<String> specialRelations = Arrays.asList(myStandardProperties.getOwl().getDefinitaDaUri(), myStandardProperties.getOwl().getSpecializzaUri());
        if (specialRelations.contains(relationKeyIRI)) {

            JSONObject relationDataProperty = new JSONObject();
            relationDataProperty.put(MyStandardConstants.DATA_PROPERTY_IRI, relationKeyIRI);
            relationDataProperty.put(MyStandardConstants.DATA_PROPERTY_LOCAL_NAME, relationKeyLocalName);
            relationDataProperty.put(MyStandardConstants.DATA_PROPERTY_VALUE, relationValue);

            individual.put(relationKeyIRI, relationDataProperty);
        }


    }

    /**
     * Si popola object individual contenente tutte le informazioni del dettaglio
     * @param owlPrefix, prefisso entità
     * @param codice, codice dell'entità di cui ottenere il dettaglio
     * @param versione, versione dell'entità di cui ottenere il dettaglio,
     * @param individual, object con dettaglio
     * @param conn, connessione rdf
     */
    private void populateDetailObject(String owlPrefix, String codice, Integer versione, JSONObject individual, RDFConnection conn) {
        try {

            String querySingleEntityByCodiceVersione = MyStandardQuery.getQuerySingleEntityByCodiceVersione(owlPrefix, codice, versione);
            conn.querySelect(querySingleEntityByCodiceVersione, (querySolution) -> {

                RDFNode objProp = querySolution.get(MyStandardConstants.SPARQL_OBJ_PROP_COLUMN_KEY);//Nome object property
                if (objProp != null) {
                    String objectPropKey = objProp.toString();//Replace di prefix
                    if (individual.has(objectPropKey)) {//Se object property g'à definita => devo aggiungere un subFields (ripetibile=
                        JSONObject jsonObject = individual.getJSONObject(objectPropKey);
                        JSONArray objKey = jsonObject.getJSONArray(MyStandardConstants.COMPONENTS_KEY);
                        addObjPropertyToJsonArray(owlPrefix, objKey, querySolution.getResource(MyStandardConstants.SPARQL_OBJ_PROP_COLUMN_KEY), querySolution.getResource(MyStandardConstants.SPARQL_DATA_PROP_COLUMN_KEY), querySolution.get(MyStandardConstants.SPARQL_VALORE_DAT_PROP_COLUMN_KEY), querySolution.getResource(MyStandardConstants.SPARQL_IND_PROP_COLUMN_KEY));

                    } else {
                        JSONArray objDataPropArray = new JSONArray();//Prima volta, aggiungo objectPropKey all'array

                        addObjPropertyToJsonArray(owlPrefix, objDataPropArray, querySolution.getResource(MyStandardConstants.SPARQL_OBJ_PROP_COLUMN_KEY), querySolution.getResource(MyStandardConstants.SPARQL_DATA_PROP_COLUMN_KEY), querySolution.get(MyStandardConstants.SPARQL_VALORE_DAT_PROP_COLUMN_KEY), querySolution.getResource(MyStandardConstants.SPARQL_IND_PROP_COLUMN_KEY));


                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_IRI, objProp.toString());
                        jsonObject.put(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_LOCAL_NAME, getStringWithoutPrefix(objProp.toString()));

                        jsonObject.put(MyStandardConstants.COMPONENTS_KEY, objDataPropArray);

                        individual.put(objectPropKey, jsonObject);

                    }

                } else {//Agigungo data property all'oggetto
                    addDataPropertyToJsonObject(individual, querySolution.getResource(MyStandardConstants.SPARQL_DATA_PROP_COLUMN_KEY), querySolution.getLiteral(MyStandardConstants.SPARQL_VALORE_DAT_PROP_COLUMN_KEY));
                }
            });

            //Si aggiunge manualmente il campo ultimaModifica
            setUltimaModificaForDetail(individual, owlPrefix);


        } catch (Exception e) {
            LOG.error("MyStandard - Errore generico in esecuzione query SPARQL", e);
        }


    }

    /**
     * Si aggiunge manualmente il campo ultima modifica all'individual (data ultima modifica o data inserimento). Metodo per il dettaglio
     * @param individual, individual a cui aggiungere il campo
     * @param owlPrefix, prefisso owl
     */
    private void setUltimaModificaForDetail(JSONObject individual, String owlPrefix) {

        //Ultima modifica: se esiste un update si tornano i dati di update, altrimenti si tornano i dati di insert

        String lastUpdateDate = null;
        String lastUpdateUser = null;

        if (individual.has(owlPrefix + MyStandardConstants.DT_UPD_COLUMN_KEY)
                && individual.has(owlPrefix + MyStandardConstants.UTE_UPD_COLUMN_KEY)) {

            JSONObject dateUpdate = individual.getJSONObject(owlPrefix + MyStandardConstants.DT_UPD_COLUMN_KEY);
            JSONObject userUpdate = individual.getJSONObject(owlPrefix + MyStandardConstants.UTE_UPD_COLUMN_KEY);

            lastUpdateDate = dateUpdate.getString(MyStandardConstants.DATA_PROPERTY_VALUE);
            lastUpdateUser = userUpdate.getString(MyStandardConstants.DATA_PROPERTY_VALUE);


        } else if (individual.has(owlPrefix + MyStandardConstants.DT_INS_COLUMN_KEY)
                && individual.has(owlPrefix + MyStandardConstants.UTE_INS_COLUMN_KEY)) {

            JSONObject dateInsert = individual.getJSONObject(owlPrefix + MyStandardConstants.DT_INS_COLUMN_KEY);
            JSONObject userInsert = individual.getJSONObject(owlPrefix + MyStandardConstants.UTE_INS_COLUMN_KEY);

            lastUpdateDate = dateInsert.getString(MyStandardConstants.DATA_PROPERTY_VALUE);
            lastUpdateUser = userInsert.getString(MyStandardConstants.DATA_PROPERTY_VALUE);
        }

        if (StringUtils.hasText(lastUpdateDate) && StringUtils.hasText(lastUpdateUser)) {

            JSONObject ultimaModifica = new JSONObject();
            ultimaModifica.put(MyStandardConstants.DATA_PROPERTY_IRI, owlPrefix + MyStandardConstants.ULTIMA_MODIFICA_KEY);
            ultimaModifica.put(MyStandardConstants.DATA_PROPERTY_LOCAL_NAME, MyStandardConstants.ULTIMA_MODIFICA_KEY);
            ultimaModifica.put(MyStandardConstants.DATA_PROPERTY_VALUE, lastUpdateUser + " " + MyStandardUtil.convertDateTimePattern(lastUpdateDate));

            individual.put(owlPrefix + MyStandardConstants.ULTIMA_MODIFICA_KEY, ultimaModifica);

        }

    }

    /**
     * Aggiunta di una proprietà nell'oggetto consono sall'interno di un json array
     * @param jsonArray, json array dove aggiungere elemento
     * @param dataProp, data property
     * @param valoreDatProp, valore data property
     * @param ind, individual name
     * @param owlPrefix , prefisso entità
     * @param objProp, resource obj property
     */
    private void addObjPropertyToJsonArray(String owlPrefix, JSONArray jsonArray, Resource objProp, Resource dataProp, RDFNode valoreDatProp, Resource ind) {
        if (ind != null) {
            JSONObject jsonObjectIndividualName = getJsonObjectIndividualName(jsonArray, MyStandardConstants.CUSTOM_IND_NAME, ind.getURI());

            if (jsonObjectIndividualName == null && (owlPrefix + MyStandardConstants.DOMINIO_BUSINESS_OBJPROP_KEY).equals(objProp.getURI())) {
                //Dominio di business non ha entity prop multiple ma appended
                jsonObjectIndividualName = getJsonObjectMultipleIndividualName(jsonArray, MyStandardConstants.CUSTOM_IND_NAME, ind.getURI());
            }

            if (jsonObjectIndividualName == null) {
                jsonObjectIndividualName = new JSONObject();
                jsonObjectIndividualName.put(MyStandardConstants.CUSTOM_IND_NAME, ind.getURI());
                jsonObjectIndividualName.put(MyStandardConstants.OBJ_PROPERTY_TARGET_INDIVIDUAL_IRI, ind.getURI());
                jsonObjectIndividualName.put(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_IRI, objProp.getURI());
                jsonObjectIndividualName.put(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_LOCAL_NAME, getStringWithoutPrefix(objProp.getURI()));

                if ((owlPrefix + MyStandardConstants.STATO_OBJPROP_KEY).equals(objProp.getURI()) ||
                        (owlPrefix + MyStandardConstants.DOMINIO_BUSINESS_OBJPROP_KEY).equals(objProp.getURI())) {
                    addDataToJsonArray(objProp,  ind, jsonObjectIndividualName, false);
                } else {
                    addDataToJsonArray(dataProp, valoreDatProp, jsonObjectIndividualName, true);
                }
                jsonArray.put(jsonObjectIndividualName);

            } else if ((owlPrefix + MyStandardConstants.DOMINIO_BUSINESS_OBJPROP_KEY).equals(objProp.getURI())) {
                JSONObject currentIndividualValue = jsonObjectIndividualName.getJSONObject(objProp.getURI());
                String currendDataPropertyValue = currentIndividualValue.getString(MyStandardConstants.DATA_PROPERTY_VALUE);
                currentIndividualValue.put(MyStandardConstants.DATA_PROPERTY_VALUE, currendDataPropertyValue + MyStandardConstants.MULTIPLE_VALUES_SEPARATOR + ind.getURI() );
            } else {//Oggetto esiste già, aggiungo la proprietà come elemento all'array
                addDataToJsonArray(dataProp, valoreDatProp, jsonObjectIndividualName, true);
            }

        } else {
            LOG.debug("MyStandard - Individual name is null");
        }

    }

    /**
     *  Aggiunta di una data property come array
     * @param individual, json object relativo all'individual
     * @param keyValue, valore key
     * @param dataValue, value
     */
    private void addDataPropertyToJsonObject(JSONObject individual, Resource keyValue, Literal dataValue) {

        if (keyValue != null) {//Replacing ns or till last / (ontopia)

            String key = keyValue.getURI();
            String value = dataValue.toString() != null ? getStringWithoutPrefix(dataValue.toString()) : "";

            JSONObject dataPropertyInfo = new JSONObject();
            dataPropertyInfo.put(MyStandardConstants.DATA_PROPERTY_IRI, keyValue.getURI());
            dataPropertyInfo.put(MyStandardConstants.DATA_PROPERTY_VALUE, value.replaceAll("\\\\", ""));
            dataPropertyInfo.put(MyStandardConstants.DATA_PROPERTY_LOCAL_NAME, getStringWithoutPrefix(keyValue.getURI()));

            individual.put(key, dataPropertyInfo);
        }
    }

    /**
     * Si cerca in un array se esiste json object con un individual ma nome diverso per appenderli
     * @param jsonArray, array su cui cercare
     * @param customIndName, nome proprietà chiave su cui effettuare la ricerca
     * @param individualName, nome individual
     * @return json object con nome individual o null se non presente
     */
    private JSONObject getJsonObjectMultipleIndividualName(JSONArray jsonArray, String customIndName, String individualName) {
        JSONObject objectInArray = null;
        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            try {
                JSONObject objectInArrayWithName = jsonArray.getJSONObject(i);
                if (objectInArrayWithName.has(customIndName) && !individualName.equals(objectInArrayWithName.getString(customIndName))) {
                    return objectInArrayWithName;
                }
            } catch (JSONException e) {
                LOG.error("MyStandard - Object in JSONArray finding {} is not a JSONObject as expected", individualName);
            }
        }

        return objectInArray;
    }

    /**
     * Si cerca in un array se esiste json object con nome dell'individual
     * @param jsonArray, array su cui cercare
     * @param customIndName, nome proprietà chiave su cui effettuare la ricerca
     * @param individualName, nome individual
     * @return json object con nome individual o null se non presente
     */
    private JSONObject getJsonObjectIndividualName(JSONArray jsonArray, String customIndName, String individualName) {
        JSONObject objectInArray = null;
        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            try {
                JSONObject objectInArrayWithName = jsonArray.getJSONObject(i);
                if (objectInArrayWithName.has(customIndName) && individualName.equals(objectInArrayWithName.getString(customIndName))) {
                    return objectInArrayWithName;
                }
            } catch (JSONException e) {
                LOG.error("MyStandard - Object in JSONArray finding {} is not a JSONObject as expected", individualName);
            }
        }

        return objectInArray;
    }

    /**
     * Aggiunta di una data property come array
     * @param dataProp, nome data property
     * @param valoreDatProp, valore data property
     * @param jsonObjectIndividualName, json object relativo all'individual
     * @param dataProp
     * @param valoreDatProp
     */
    private void addDataToJsonArray(Resource dataProp, RDFNode valoreDatProp, JSONObject jsonObjectIndividualName, Boolean removeValuePrefix) {
        String key = dataProp.getURI();
        String value;
        if (removeValuePrefix) {
            if (valoreDatProp instanceof Literal) {
                value = valoreDatProp != null ? getStringWithoutPrefix(((Literal) valoreDatProp).getString()) : "";
            } else {
                value = valoreDatProp != null ? getStringWithoutPrefix(valoreDatProp.toString()) : "";
            }

        } else {
            if (valoreDatProp instanceof  Literal) {
                value = valoreDatProp != null ? ((Literal)valoreDatProp).getString() : "";
            } else {
                value = valoreDatProp != null ? valoreDatProp.toString() : "";
            }

        }

        JSONObject dataPropertyInfo = new JSONObject();
        dataPropertyInfo.put(MyStandardConstants.DATA_PROPERTY_IRI, dataProp.getURI());
        dataPropertyInfo.put(MyStandardConstants.DATA_PROPERTY_VALUE, value.replaceAll("\\\\", ""));
        dataPropertyInfo.put(MyStandardConstants.DATA_PROPERTY_LOCAL_NAME, getStringWithoutPrefix(dataProp.getURI()));

        jsonObjectIndividualName.put(key, dataPropertyInfo);
    }


    @Override
    public List<JSONObject> getAllByCodiceAndVersione(String entityType, String codice, Integer versione) throws MyStandardException {

        List<JSONObject> individuals = new ArrayList<>();
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            OClass oClass = getOClassFromModel(entityType);

            if (oClass != null) {//

                String owlPrefix = oClass.getPrefix();
                String queryStoricoByEntityAndCodice = MyStandardQuery.getQueryStoricoByEntityAndCodice(myStandardProperties, entityType, owlPrefix, codice, versione);
                conn.querySelect(queryStoricoByEntityAndCodice, (querySolution) -> {
                    JSONObject data = new JSONObject();//Si aggiungono i dati dell'entità

                    data.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.VERSIONE_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.STATO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()) : "");
                    data.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.NAME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.NAME_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY) != null ? 
messageSource.getMessage(getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()), null, getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()) , null) : "");
                    data.put(MyStandardConstants.ID_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.ID_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.ID_ENTITA_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.DEFINITA_DA_COLUMN_KEY, querySolution.get(MyStandardConstants.IPA_CODE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.IPA_CODE_COLUMN_KEY).getString()) : "");


                    individuals.add(data);
                });


            } else {
                throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
            }

        } catch (Exception e) {
            LOG.error("MyStandard - Errore nel recupero dei dati RDF", e);
        }

        return individuals;
    }

    @Override
    public void addEntityIndividual(String domain, String entityType, MyStandardEntity entity, Map<String, InputStream> allegatiMap, UserWithAdditionalInfo user) throws MyStandardException, IOException {
        if (entity != null) {
            List<String> allegatiInseritiMyBox = new ArrayList<>();
            List<String> queryList = new ArrayList<>();
            String individualIdEntita = null;

            try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

                try {
                    conn.begin(ReadWrite.WRITE);

                    OModel oModel = mystandardConfig.getModel();
                    Map<String, OClass> oModelClasses = oModel.getClasses();
                    OClass oClass = oModelClasses.entrySet().stream()
                            .filter(entry -> entityType.equals(entry.getValue().getLocalName()))
                            .findFirst().get().getValue();

                    if (oClass != null) {

                        String owlPrefix = oClass.getPrefix();
                        String owlPrefixData = oClass.getPrefixData();

                        Map<String, Object> dataProperty = entity.getDataProperty();
                        individualIdEntita = getIndividualIdEntita(owlPrefix, dataProperty);

                        Resource individual = ResourceFactory.createResource(owlPrefixData + individualIdEntita);
                        String finalIndividualIdEntita = individualIdEntita;


                        if (!individualAlreadyExists(conn, owlPrefix, individual)) {

                            Resource entityTypeIRI = ResourceFactory.createResource(owlPrefix + entityType);

                            queryList.add(getUpdateBuilderInsert(individual, RDF.Init.type(), entityTypeIRI));
                            queryList.add(getUpdateBuilderInsert(individual, RDF.Init.type(), OWL2.NamedIndividual));
                            queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.ID_ENTITA_COLUMN_KEY), ResourceFactory.createStringLiteral(finalIndividualIdEntita)));

                            checkRequiredFields(dataProperty);

                            //Set delle data property
                            for (String key : dataProperty.keySet()) {
                                Property propertyIri = ResourceFactory.createProperty(key);
                                Object dataPropValueObj = dataProperty.get(key);
                                String dataPropValue = dataPropValueObj != null ? dataPropValueObj.toString() : "";

                                queryList.add(getUpdateBuilderInsert(individual, propertyIri, ResourceFactory.createStringLiteral(dataPropValue)));
                            }

                            queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.DT_INS_COLUMN_KEY), ResourceFactory.createStringLiteral(ZonedDateTime.now().format(MyStandardUtil.MYSTANDARD_DATETIME_ZONED_FORMATTER))));
                            queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.UTE_INS_COLUMN_KEY), ResourceFactory.createStringLiteral(user.getMystandardUsername())));

                            //Add entity property
                            List<MyStandardEntityPropertyIndividual> entityPropertiesList = entity.getEntityProperty();
                            if (entityPropertiesList != null && entityPropertiesList.size() > 0) {
                                for (MyStandardEntityPropertyIndividual entityPropertyIndividual : entityPropertiesList) {

                                    //Creazione entity property
                                    MyStandardIndividualOperationEnum entityPropertyOperation = entityPropertyIndividual.get_operation();
                                    if (MyStandardIndividualOperationEnum.ADD.equals(entityPropertyOperation)) {
                                        addEntityProperty(queryList, allegatiMap, owlPrefix, owlPrefixData,
                                                conn, individualIdEntita, individual, entityPropertyIndividual, allegatiInseritiMyBox);
                                    } else {
                                        LOG.error("MyStandard - In un inserimento, è arrivata una {} per entity {}. Sono permesse solo le operazioni di ADD", entityPropertyOperation, entityPropertyIndividual.get_entityPropertyIRI());
                                    }
                                }

                                if ( !isEntityPropertyPresent(entityPropertiesList, myStandardProperties.getOwl().getRelazioneMenu())) {
                                    Property dominioBusinessIRI = ResourceFactory.createProperty(myStandardProperties.getOwl().getRelazioneMenu());
                                    String dominioGenerale = owlPrefix + MyStandardConstants.DOMINIO_GENERALE;

                                    queryList.add(getUpdateBuilderInsert(individual, dominioBusinessIRI, ResourceFactory.createResource(dominioGenerale)));
                                }

                            } else {
                                //Se non è stata mandata nessuna entity property, oppure se tra le entity property non c'è dominio
                                //Allora Add dominio generale come default
                                Property dominioBusinessIRI = ResourceFactory.createProperty(myStandardProperties.getOwl().getRelazioneMenu());
                                String dominioGenerale = owlPrefix + MyStandardConstants.DOMINIO_GENERALE;

                                queryList.add(getUpdateBuilderInsert(individual, dominioBusinessIRI, ResourceFactory.createResource(dominioGenerale)));
                            }


                            //Functional property
                            List<MyStandardFunctionalPropertyIndividual> functionalPropertyList = entity.getFunctionalProperty();
                            if (functionalPropertyList != null && functionalPropertyList.size() > 0) {
                                for (MyStandardFunctionalPropertyIndividual functionalPropertyIndividual : functionalPropertyList) {
                                    addFunctionalProperty(queryList, oModel, conn, individual, functionalPropertyIndividual);
                                }
                            }

                            //Si aggiungono alcune relazioni automatiche in base al mio profilo
                            addRelationsByProfile(queryList, owlPrefix, owlPrefixData, oModel, oClass, conn, individual, user);



                            String fullQuery = String.join(" ; \n", queryList);

                            conn.update(fullQuery);
                            LOG.debug("MyStandard - Individual " + individualIdEntita + " creato.");

                            conn.commit();

                            //Inserimento nello storico dell'operazione di insert
                            storicoService.insertStorico(MyStandardIndividualOperationEnum.ADD.get_description(), finalIndividualIdEntita,
                                    null, MyStandardStatoEnum.INSERITO.getCode(), ZonedDateTime.now().format(MyStandardUtil.MYSTANDARD_DATETIME_ZONED_FORMATTER), user.getMystandardUsername(), null);

                        } else {
                            throw new MyStandardException("Individual " + individualIdEntita + " già esistente");
                        }


                    } else {
                        throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
                    }
                } catch (Exception e) {

                    if (allegatiInseritiMyBox.size() > 0) {
                        for (String idAllegato : allegatiInseritiMyBox) {
                            try {
                                attachmentsService.delete(individualIdEntita, idAllegato);
                            } catch (RepositoryAccessException ex) {
                                throw new MyStandardException("Impossibile accedere a MyBox");
                            } catch (Exception ex) {
                                throw new MyStandardException("Errore generico nella cancellazione di un file accesso a MyBox");
                            }
                        }
                    }
                    if (!conn.isClosed()) {
                        conn.abort();
                    }
                    throw e;
                } finally {
                    conn.end();
                }

            }
        } else {
            throw new MyStandardException("Oggetto entity ricevuto nullo");
        }
    }



    private String getUpdateBuilderDelete(Resource subject, Property predicate, Resource object) {

        WhereBuilder whereBuilder = new WhereBuilder()
                .addWhere(subject, predicate == null ? "?predicate" : predicate, object == null ? "?object" : object);

        UpdateDeleteWhere updateDeleteWhere = new UpdateBuilder(PrefixMapping.Standard)
                .buildDeleteWhere(whereBuilder);

        return updateDeleteWhere.toString();

    }

    private String getUpdateBuilderInsert(Resource subject, Property predicate, Resource object) {
        return new UpdateBuilder(PrefixMapping.Standard)
                .addInsert(subject, predicate, object).buildRequest().toString();

    }

    private String getUpdateBuilderInsert(Resource subject, Property predicate, Literal object) {
        return new UpdateBuilder(PrefixMapping.Standard)
                .addInsert(subject, predicate, object).buildRequest().toString();

    }

    private void checkRequiredFields(Map<String, Object> dataProperty) throws MyStandardException {
        Object nome = dataProperty.get(PREFIX_THING + MyStandardConstants.NAME_COLUMN_KEY);
        if (nome == null) {
            throw new MyStandardException("Nome entità non presente");
        }
    }

    /**
     * Si calcola idEntita
     * @param owlPrefix, prefix da utilizzare
     * @param dataProperty, lista di dataproperty
     * @return idEntita
     * @throws MyStandardException
     */
    private String getIndividualIdEntita(String owlPrefix, Map<String, Object> dataProperty) throws MyStandardException {
        Object codiceEntita = dataProperty.get(owlPrefix + MyStandardConstants.CODICE_ENTITA_COLUMN_KEY);
        Object versione = dataProperty.get(owlPrefix + MyStandardConstants.VERSIONE_COLUMN_KEY);
        if (codiceEntita == null || versione == null) {
            throw new MyStandardException("Codice o versione entità non presenti.");
        }
        return codiceEntita.toString() + "_" + versione.toString();//1 è versione
    }

    /**
     * Metodo per l'aggiunta di una entity property nuova
     * @param allegatiMap, eventuali allegati da aggiungere
     * @param owlPrefix, prefisso owl da utilizzare
     * @param owlPrefixData, prefisso da utilizzare per gli individual
     * @param conn, repository con i dati
     * @param individualIdEntita, idEntita
     * @param individual, IRI dell'individual
     * @param entityPropertyIndividual, entity property ricevuta in input
     * @param allegatiInseritiMyBox, allegati inseriti in mybox
     * @throws MyStandardException
     */
    private void addEntityProperty(List<String> queryList, Map<String, InputStream> allegatiMap, String owlPrefix, String owlPrefixData, RDFConnection conn,
                                   String individualIdEntita, Resource individual, MyStandardEntityPropertyIndividual entityPropertyIndividual,
                                   List<String> allegatiInseritiMyBox) throws MyStandardException, IOException {

        String targetIndividualIRI = entityPropertyIndividual.get_targetIndividualIRI();
        if (StringUtils.hasText(targetIndividualIRI)) {
            throw new MyStandardException("Target individual IRI non nullo per individual " + individualIdEntita + ". Il campo deve essere nullo per aggiungere una nuova entity property");
        }

        String entityRangeIRI = entityPropertyIndividual.get_entityRangeIRI();
        if (!StringUtils.hasText(entityRangeIRI)) {
            throw new MyStandardException("Entity Range IRI non nullo per individual " + individualIdEntita + ". Il campo deve essere nullo per aggiungere una nuova entity property");
        }
        String rangeClassName = getStringWithoutPrefix(entityRangeIRI);
        String statoPrefix = owlPrefix + MyStandardConstants.STATO_OBJPROP_KEY;
        String dominioPrefix = myStandardProperties.getOwl().getRelazioneMenu();
        if (statoPrefix.equals(entityPropertyIndividual.get_entityPropertyIRI())) {//Logica solo per il campo stato

            Map<String, Object> statoDataProp = entityPropertyIndividual.getDataProperty();
            if (statoDataProp == null || !statoDataProp.containsKey(statoPrefix)) {
                throw new MyStandardException("In entity property stato non è presente la dataproperty con il valore dello stato");
            }
            String stato = String.valueOf(statoDataProp.get(statoPrefix));
            //Creazione Object property in elemento padre
            queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(entityPropertyIndividual.get_entityPropertyIRI()), ResourceFactory.createResource(stato)));


        } else if (dominioPrefix.equals(entityPropertyIndividual.get_entityPropertyIRI())) {//Logica per la lista dei domini

            Map<String, Object> domainDataProp = entityPropertyIndividual.getDataProperty();
            if (domainDataProp == null || !domainDataProp.containsKey(dominioPrefix)) {
                throw new MyStandardException("In entity property dominio non è presente la dataproperty con il valore del dominio");
            }

            String dominioProp = String.valueOf(domainDataProp.get(dominioPrefix));

            List<String> listaDomini = Stream.of(dominioProp.split(",", -1))
                    .collect(Collectors.toList());

            Property dominioBusinessIRI = ResourceFactory.createProperty(myStandardProperties.getOwl().getRelazioneMenu());
            String dominioGenerale = owlPrefix + MyStandardConstants.DOMINIO_GENERALE;
            if (!listaDomini.contains(dominioGenerale)) listaDomini.add(dominioGenerale);

            for (String domain: listaDomini) {
                queryList.add(getUpdateBuilderInsert(individual, dominioBusinessIRI, ResourceFactory.createResource(domain)));
            }



        } else {//Creazione di un nuovo individual per entity property

            Resource entityPropertyIRI = ResourceFactory.createResource(owlPrefixData + rangeClassName + "_" + UUID.randomUUID());
            queryList.add(getUpdateBuilderInsert(entityPropertyIRI, RDF.Init.type(), ResourceFactory.createResource(entityPropertyIndividual.get_entityRangeIRI())));
            queryList.add(getUpdateBuilderInsert(entityPropertyIRI, RDF.Init.type(), OWL2.NamedIndividual));

            Map<String, Object> entityDataProperty = entityPropertyIndividual.getDataProperty();
            //Set delle data property
            for (String key : entityDataProperty.keySet()) {
                if (!key.equals(MyStandardConstants.ID_FILE_COLUMN_KEY) &&
                        !key.equals(MyStandardConstants.ID_ENTITA_ORIGINE_COLUMN_KEY)  &&
                        !key.equals(owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY)) {//IdFile e idEntita origine non sono campi di OWL. Id documento in input non deve essere inserito (si inserisce solo dopo operazione con mybox)
                    Property propertyIri = ResourceFactory.createProperty(key);
                    Object entityDataPropertyObj = entityDataProperty.get(key);
                    String entityPropValue = entityDataPropertyObj != null ? entityDataPropertyObj.toString() : "";

                    queryList.add(getUpdateBuilderInsert(entityPropertyIRI, propertyIri, ResourceFactory.createStringLiteral(entityPropValue)));

                }
            }

            String allegatoPrefix = owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN;
            if (allegatoPrefix.equals(entityPropertyIndividual.get_entityPropertyIRI())) {

                //GET da json id del file
                Object idFileObject = entityDataProperty.get(MyStandardConstants.ID_FILE_COLUMN_KEY);
                String idFile = idFileObject != null ? idFileObject.toString() : "";
                if (!StringUtils.hasText(idFile)) {
                    Object idDocumentoObject = entityDataProperty.get(owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY);
                    String idDocumento = Objects.toString(idDocumentoObject, "");

                    if (!StringUtils.hasText(idDocumento)) {
                        throw new MyStandardException("Id file non presente e id documento non presente (quindi non è una nuova versione.");
                    } else {
                        // get input stream from mybox
                        Object idEntitaOrigineObject = entityDataProperty.get(MyStandardConstants.ID_ENTITA_ORIGINE_COLUMN_KEY);
                        String idEntitaOrigine = Objects.toString(idEntitaOrigineObject, "");

                        InputStream data = getFileFromMyBox(idEntitaOrigine, idDocumento);
                        if (data == null) {
                            throw new MyStandardException("Nessun file presente in mybox con idEntita " + idEntitaOrigine + " e id allegato " + idDocumento);
                        } else {

                            final Path path = Files.createTempFile("myTempFile", null);
                            try {
                                //get correct inputstream
                                InputStream targetStream = readAndCreateInputStream(data, path);

                                //Put file in mybox
                                putFileInMyBox(queryList, owlPrefix, conn, individualIdEntita, entityPropertyIRI, entityDataProperty, targetStream, allegatiInseritiMyBox);

                            } finally {
                                Files.deleteIfExists(path);
                            }

                        }

                    }

                } else {
                    InputStream inputStreamFile = allegatiMap.get(idFile);//Check in hashmap entry con quell'id
                    if (inputStreamFile == null) {
                        throw new MyStandardException("Impossibile trovare il file inviato con id " + idFile);
                    } else {

                        //Put in MyBox
                        putFileInMyBox(queryList, owlPrefix, conn, individualIdEntita, entityPropertyIRI, entityDataProperty, inputStreamFile, allegatiInseritiMyBox);
                    }
                }

            }

            //Creazione Object property in elemento padre
            queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(entityPropertyIndividual.get_entityPropertyIRI()), entityPropertyIRI));

        }


    }

    /**
     * Metodo per la modifica di una entity property esistente
     * @param conn, repository connection
     * @param owlPrefix, prefisso owl da utilizzare
     * @param allegatiMap, eventuali allegati da aggiungere
     * @param conn, model con i dati
     * @param individualIdEntita, idEntita
     * @param individual, IRI dell'individual
     * @param entityPropertyIndividual, entity property ricevuta in input
     * @throws MyStandardException
     */
    private void modifyEntityProperty(List<String> queryList, RDFConnection conn, String owlPrefix, Map<String, InputStream> allegatiMap, String individualIdEntita, Resource individual, MyStandardEntityPropertyIndividual entityPropertyIndividual) throws MyStandardException {

        String targetIndividualIRI = entityPropertyIndividual.get_targetIndividualIRI();
        if (!StringUtils.hasText(targetIndividualIRI)) {
            throw new MyStandardException("Target individual IRI nullo. Il campo è necessario per l'eliminazione della entity property");
        }

        Resource targetEntityPropertyIndividualIRI = ResourceFactory.createResource(targetIndividualIRI);
        Map<String, Object> entityDataProperties = entityPropertyIndividual.getDataProperty();

        String dominioPrefix = myStandardProperties.getOwl().getRelazioneMenu();
        for (String key : entityDataProperties.keySet()) {
            Property propertyIri = ResourceFactory.createProperty(key);

            if (dominioPrefix.equals(entityPropertyIndividual.get_entityPropertyIRI())) {

                Map<String, Object> domainDataProp = entityPropertyIndividual.getDataProperty();
                if (domainDataProp == null || !domainDataProp.containsKey(dominioPrefix)) {
                    throw new MyStandardException("In entity property dominio non è presente la dataproperty con il valore del dominio");
                }

                String dominioProp = String.valueOf(domainDataProp.get(dominioPrefix));

                List<String> listaDomini = Stream.of(dominioProp.split(",", -1))
                        .collect(Collectors.toList());

                Property dominioBusinessIRI = ResourceFactory.createProperty(myStandardProperties.getOwl().getRelazioneMenu());
                String dominioGenerale = owlPrefix + MyStandardConstants.DOMINIO_GENERALE;

                if (!listaDomini.contains(dominioGenerale)) listaDomini.add(dominioGenerale);

                queryList.add(getUpdateBuilderDelete(individual, propertyIri, null));

                for (String domain: listaDomini) {
                    queryList.add(getUpdateBuilderInsert(individual, dominioBusinessIRI, ResourceFactory.createResource(domain)));
                }

            } else {
                //Get value from JSON
                Object entityDataPropertyObj = entityDataProperties.get(key);
                String entityPropValue = entityDataPropertyObj != null ? entityDataPropertyObj.toString() : "";

                List<String> dataPropertyToBeUpdated = getDataPropertyStatement(conn, targetEntityPropertyIndividualIRI.getURI(), propertyIri.getURI());
                if (dataPropertyToBeUpdated == null || dataPropertyToBeUpdated.size() == 0) {
                    queryList.add(getUpdateBuilderInsert(targetEntityPropertyIndividualIRI, propertyIri, ResourceFactory.createStringLiteral(entityPropValue)));
                } else {

                    String dataPropertyValue = dataPropertyToBeUpdated.get(0);
                    if (!dataPropertyValue.equals(entityPropValue)) {

                        if (StringUtils.hasText(entityPropValue)) {
                            //Sto modificando una data property con un valore
                            queryList.add(getUpdateBuilderDelete(targetEntityPropertyIndividualIRI, propertyIri, null));
                            queryList.add(getUpdateBuilderInsert(targetEntityPropertyIndividualIRI, propertyIri, ResourceFactory.createStringLiteral(entityPropValue)));

                        } else {
                            //sto sbiancando una data property quindi la elimino
                            queryList.add(getUpdateBuilderDelete(targetEntityPropertyIndividualIRI, propertyIri, null));
                        }

                        if (MyStandardConstants.ID_FILE_COLUMN_KEY.equals(key) && StringUtils.hasText(entityPropValue)) {
                            InputStream inputStreamFile = allegatiMap.get(entityPropValue);//Check in hashmap entry con quell'id
                            if (inputStreamFile == null) {
                                throw new MyStandardException("Impossibile trovare il file inviato con id " + entityPropValue);
                            } else {
                                //Rimozione vecchio file su MyBox
                                Object idAllegatoObj = entityDataProperties.get(owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY);
                                String idAllegato = idAllegatoObj != null ? idAllegatoObj.toString() : "";
                                try {
                                    attachmentsService.delete(individualIdEntita, idAllegato);

                                    queryList.add(getUpdateBuilderDelete(targetEntityPropertyIndividualIRI, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY), null));

                                    Object nomeFileObj = entityDataProperties.get(owlPrefix + MyStandardConstants.NOME_FILE_COLUMN_KEY);
                                    Object tipoFileObj = entityDataProperties.get(owlPrefix + MyStandardConstants.TIPO_FILE_COLUMN_KEY);
                                    String nomeFile = nomeFileObj != null ? nomeFileObj.toString() : "";
                                    String tipoFile = tipoFileObj != null ? tipoFileObj.toString() : "";

                                    String idAllegatoNew = attachmentsService.put(individualIdEntita, nomeFile, tipoFile, inputStreamFile);
                                    queryList.add(getUpdateBuilderInsert(targetEntityPropertyIndividualIRI, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY), ResourceFactory.createStringLiteral(idAllegatoNew)));

                                } catch (RepositoryAccessException e) {
                                    throw new MyStandardException("Impossibile accedere a MyBox");
                                } catch (Exception e) {
                                    throw new MyStandardException("Errore generico nella cancellazione di un file accesso a MyBox");
                                }



                            }
                        }
                    }
                }
            }


        }


    }

    /**
     * Metodo per la rimozione di una entity property esistente
     * @param owlPrefix, prefisso owl da utilizzare
     * @param conn, model con i dati
     * @param individualIdEntita, idEntita
     * @param individual, IRI dell'individual
     * @param entityPropertyIndividual, entity property ricevuta in input
     * @throws MyStandardException
     */
    private void removeEntityProperty(List<String> queryList, String owlPrefix, RDFConnection conn, String individualIdEntita, Resource individual,
                                      MyStandardEntityPropertyIndividual entityPropertyIndividual) throws MyStandardException {

        String targetIndividualIRI = entityPropertyIndividual.get_targetIndividualIRI();
        if (!StringUtils.hasText(targetIndividualIRI)) {
            throw new MyStandardException("Target individual IRI nullo. Il campo è necessario per l'eliminazione della entity property");
        }

        Resource entityPropertyTargetIndividualIRI = ResourceFactory.createResource(entityPropertyIndividual.get_targetIndividualIRI());

        String allegatoPrefix = owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN;
        if (allegatoPrefix.equals(entityPropertyIndividual.get_entityPropertyIRI())) {
            //
            // rimozione su mybox
            Map<String, Object> entityDataProperty = entityPropertyIndividual.getDataProperty();
            Object idAllegatoObj = entityDataProperty.get(owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY);
            String idAllegato = idAllegatoObj != null ? idAllegatoObj.toString() : "";

            removeAllegatoFromMyBox(queryList, conn, individualIdEntita, individual, entityPropertyIndividual, entityPropertyTargetIndividualIRI, idAllegato);
        } else {

            String entityPropertyIRI = entityPropertyIndividual.get_entityPropertyIRI();

            queryList.add(getUpdateBuilderDelete(individual, ResourceFactory.createProperty(entityPropertyIRI), entityPropertyTargetIndividualIRI));
            queryList.add(getUpdateBuilderDelete(entityPropertyTargetIndividualIRI, null, null));

        }
    }


    /**
     * Adding functional property
     * @param oModel, modello generali con le classi
     * @param conn, repository con i dati
     * @param individual, IRI dell'individual principale
     * @param functionalPropertyIndividual, functional property da aggiungere all'individual
     */
    private void addFunctionalProperty(List<String> queryList, OModel oModel, RDFConnection conn, Resource individual, MyStandardFunctionalPropertyIndividual functionalPropertyIndividual) throws MyStandardException {

        String targetIndividual = functionalPropertyIndividual.get_targetIndividualsIRI();
        if (!StringUtils.hasText(targetIndividual)) {
            throw new MyStandardException("Nell'aggiunta di una functional property il target individual IRI deve essere valorizzato");
        } else {
            Property functionalPropertyIRI = ResourceFactory.createProperty(functionalPropertyIndividual.get_functionalPropertyIRI());
            Resource targetIndividualIRI = ResourceFactory.createResource(functionalPropertyIndividual.get_targetIndividualsIRI());

            queryList.add(getUpdateBuilderInsert(individual, functionalPropertyIRI, targetIndividualIRI));

            //Adding inverse
            Map<String, ORelation> relations = oModel.getRelations();
            if (relations.containsKey(functionalPropertyIndividual.get_functionalPropertyIRI())) {
                ORelation relation = relations.get(functionalPropertyIndividual.get_functionalPropertyIRI());
                if (StringUtils.hasText(relation.getInverseOf())) {
                    Property inverseFunctionalPropertyIRI = ResourceFactory.createProperty(relation.getInverseOf());
                    queryList.add(getUpdateBuilderInsert(targetIndividualIRI, inverseFunctionalPropertyIRI, individual));

                } else {
                    LOG.info("MyStandard - La relazione " + functionalPropertyIndividual.get_functionalPropertyIRI() + " non ha un'inversa");
                }
            } else if(myStandardProperties.getOwl().getDefinisceUri().equals(functionalPropertyIndividual.get_functionalPropertyIRI())) {
                //Se la functional è Definisce, allora so che la inverse è_definita. Questa functional definita in property perchè fondamentale
                Property inverseFunctionalPropertyIRI = ResourceFactory.createProperty(myStandardProperties.getOwl().getDefinitaDaUri());
                queryList.add(getUpdateBuilderInsert(targetIndividualIRI, inverseFunctionalPropertyIRI, individual));

            }


        }
    }

    /**
     * Remove functional property
     * @param oModel, modello dei dati
     * @param conn, modello con i dati
     * @param individual, IRI dell'individual principale
     * @param functionalPropertyIndividual, functional property da aggiungere all'individual
     */
    private void removeFunctionalProperty(List<String> queryList, OModel oModel, RDFConnection conn, Resource individual, MyStandardFunctionalPropertyIndividual functionalPropertyIndividual) throws MyStandardException {
        String targetIndividual = functionalPropertyIndividual.get_targetIndividualsIRI();
        if (!StringUtils.hasText(targetIndividual)) {
            throw new MyStandardException("Nella rimozione di una functional property il target individual IRI deve essere valorizzato");
        } else {

            Property functionalPropertyIRI = ResourceFactory.createProperty(functionalPropertyIndividual.get_functionalPropertyIRI());
            Resource targetIndividualIRI = ResourceFactory.createResource(targetIndividual);

            queryList.add(getUpdateBuilderDelete(individual, functionalPropertyIRI, targetIndividualIRI));

            //Adding inverse
            Map<String, ORelation> relations = oModel.getRelations();
            if (relations.containsKey(functionalPropertyIndividual.get_functionalPropertyIRI())) {
                ORelation relation = relations.get(functionalPropertyIndividual.get_functionalPropertyIRI());
                Property inverseFunctionalPropertyIRI = ResourceFactory.createProperty(relation.getInverseOf());

                queryList.add(getUpdateBuilderDelete(targetIndividualIRI, inverseFunctionalPropertyIRI, individual));

            }
        }

    }

    /**
     * Si aggiungono delle relazioni automatiche in base al profilo dell'utente autenticato
     * @param oModel, struttura dei dati
     * @param conn, repository con i dati
     * @param individual, IRI dell'individual principale
     * @param oClass , struttura classe
     * @param user, utente che esegue operazioni
     * @throws MyStandardException
     */
    private void addRelationsByProfile(List<String> queryList, String owlPrefix, String owlPrefixData, OModel oModel, OClass oClass, RDFConnection conn, Resource individual, UserWithAdditionalInfo user) throws MyStandardException, IOException {

        if (user.isOperatoreEnteLocale()) {
            //Operatore o responsabile ente locale, e nessun ruolo su ente nazionale
            if (oClass.getShowIpaFilter()) {//se entità è una sottoclasse di entità strutturata
                //Ottenere la lista di enti a cui faccio parte

                String ipa = user.getIpa();
                //La lista contiene il ruolo e le classi per cui operare. Dovrebbe sempre contenere un elemento solo
                Optional<ProfileUser> optionalProfileUser = user.getUserProfiles().stream()
                        .filter(profUser -> ipa.equalsIgnoreCase(profUser.getIpa()))
                        .findFirst();

                if (optionalProfileUser.isPresent()) {



                    //Si verifica se IPA esiste come ente
                    JSONObject enteByIpa = findEntePubblicatoRecenteByIPA(conn, owlPrefix, ipa);
                    if (enteByIpa == null) {
                        throw new MyStandardException("Nessun ente in MyStandard con codice " + ipa + " per dominio " + org.apache.commons.lang3.StringUtils.join(user.getDomains(), ","));
                    } else {


                        MyStandardFunctionalPropertyIndividual myStandardFunctionalPropertyIndividual = new MyStandardFunctionalPropertyIndividual();
                        myStandardFunctionalPropertyIndividual.set_targetIndividualsIRI(individual.getNameSpace() + individual.getLocalName());
                        myStandardFunctionalPropertyIndividual.set_functionalPropertyIRI(myStandardProperties.getOwl().getDefinisceUri());

                        Resource enteByIPAIndividual = ResourceFactory.createResource(owlPrefixData + enteByIpa.get(MyStandardConstants.ID_ENTITA_COLUMN_KEY));
                        addFunctionalProperty(queryList, oModel, conn, enteByIPAIndividual, myStandardFunctionalPropertyIndividual);
                    }
                } else {
                    throw new MyStandardException("Utente non possiede un profilo per ipa " + ipa);
                }



            }
        }

    }

    private boolean isEntityPropertyPresent(List<MyStandardEntityPropertyIndividual> entityPropertiesList, String relazioneMenu) {

        if (entityPropertiesList != null) {
            return entityPropertiesList.stream().filter(element -> relazioneMenu.equals(element.get_entityPropertyIRI())).findFirst().isPresent();
        } else return false;
    }



    /**
     * Check if inddividual with IRI exists
     * @param conn, rdf connection
     * @param individual, individual IRI
     * @return true if exists, false otherwise
     */
    private boolean individualAlreadyExists(RDFConnection conn, String owlPrefix, Resource individual) throws MyStandardException {

        String idEntita = owlPrefix + MyStandardConstants.ID_ENTITA_COLUMN_KEY;
        return getDataPropertyStatement(conn, individual.getURI(), idEntita).size() > 0;

    }
    /**
     * Get file from MyBox
     * @param idEntitaOrigine, idEntita con cui è presente in MyBox
     * @param idDocumento, id allegato
     * @return allegato
     */
    private InputStream getFileFromMyBox(String idEntitaOrigine, String idDocumento) throws MyStandardException {
        try {
            return attachmentsService.get(idEntitaOrigine, idDocumento);
        } catch (RepositoryAccessException e) {
            throw new MyStandardException("Impossibile accedere a MyBox");
        } catch (Exception e) {
            throw new MyStandardException("Errore generico nell'accesso a MyBox");
        }
    }

    /**
     * Create temp file and get input stream
     * @param data, original input stream
     * @param path, temp file
     * @return final input stream
     * @throws IOException
     */
    private InputStream readAndCreateInputStream(InputStream data, Path path) throws IOException {
        byte[] fileByteArray = IOUtils.toByteArray(data);
        Files.write(path, fileByteArray);
        return Files.newInputStream(path);
    }

    /**
     * Metodo per inserire file su mybox
     * @param owlPrefix, prefisso da utilizzare per ottenere / impostare property
     * @param conn, connessione con i dati
     * @param individualIdEntita, idEntita individual
     * @param entityPropertyIRI, IRI entityProperty
     * @param entityDataProperty, data property dell'entità
     * @param inputStreamFile, file
     * @param allegatiInseritiMyBox, lista id allegati per rollback
     * @throws MyStandardException
     */
    private void putFileInMyBox(List<String> queryList, String owlPrefix, RDFConnection conn, String individualIdEntita,
                                Resource entityPropertyIRI, Map<String, Object> entityDataProperty,
                                InputStream inputStreamFile, List<String> allegatiInseritiMyBox) throws MyStandardException {
        Object nomeFileObj = entityDataProperty.get(owlPrefix + MyStandardConstants.NOME_FILE_COLUMN_KEY);
        Object tipoFileObj = entityDataProperty.get(owlPrefix + MyStandardConstants.TIPO_FILE_COLUMN_KEY);
        String nomeFile = nomeFileObj != null ? nomeFileObj.toString() : "";
        String tipoFile = tipoFileObj != null ? tipoFileObj.toString() : "";
        try {
            String idAllegato = attachmentsService.put(individualIdEntita, nomeFile, tipoFile, inputStreamFile);
            allegatiInseritiMyBox.add(idAllegato);

            queryList.add(getUpdateBuilderInsert(entityPropertyIRI, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY), ResourceFactory.createStringLiteral(idAllegato)));

       } catch (RepositoryAccessException e) {
            throw new MyStandardException("Impossibile accedere a MyBox");
        } catch (Exception e) {
            throw new MyStandardException("Errore generico nell'accesso a MyBox");
        }
    }

    /**
     * Rimozione allegato da MyBox
     * @param conn, connessione al server rdf
     * @param individualIdEntita, id entità individual
     * @param individual, resource individual
     * @param entityPropertyIndividual, dati individual ricevuti in input
     * @param entityPropertyTargetIndividualIRI, target individual iri
     * @param idAllegato, id allegato da rimuovere da mybox
     * @throws MyStandardException
     */
    private void removeAllegatoFromMyBox(List<String> queryList, RDFConnection conn, String individualIdEntita, Resource individual, MyStandardEntityPropertyIndividual entityPropertyIndividual, Resource entityPropertyTargetIndividualIRI, String idAllegato) throws MyStandardException {
        try {
            attachmentsService.delete(individualIdEntita, idAllegato);
            queryList.add(getUpdateBuilderDelete(individual, ResourceFactory.createProperty(entityPropertyIndividual.get_entityPropertyIRI()), entityPropertyTargetIndividualIRI));
            queryList.add(getUpdateBuilderDelete(entityPropertyTargetIndividualIRI, null, null));
        } catch (RepositoryAccessException e) {
            throw new MyStandardException("Impossibile accedere a MyBox");
        } catch (Exception e) {
            throw new MyStandardException("Errore generico nella cancellazione di un file accesso a MyBox");
        }
    }


    /**
     * Si ritorna l'ente pubblicato conv ersione più recente by IPA
     * @param conn, connessione rdf
     * @param owlPrefix, prefisso owl
     * @param ipa, ipa dell'ente da cercare
     * @return dati dell'ente con versione più recente pubblicato per ipa
     * @throws IOException
     */
    private JSONObject findEntePubblicatoRecenteByIPA(RDFConnection conn, String owlPrefix, String ipa) throws IOException {

        JSONObject jsonResult = new JSONObject();

        try {
            //Get queryEntePubblicatoRecenteByIPA

            String queryEntePubblicatoRecenteByIPA = MyStandardQuery.getQueryEntePubblicatoRecenteByIPA(myStandardProperties, owlPrefix, ipa);

            conn.querySelect(queryEntePubblicatoRecenteByIPA, (querySolution) -> {
                jsonResult.put(MyStandardConstants.ID_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.ID_ENTITA_COLUMN_KEY).toString());
            });


        } catch (Exception e) {
            LOG.error("MyStandard - Errore generico in esecuzione query SPARQL", e);
        }

        return jsonResult.get(MyStandardConstants.ID_ENTITA_COLUMN_KEY) != null ? jsonResult : null;
    }



    @Override
    public void updateEntityIndividual(String domain, String entityType, MyStandardEntity entity, Map<String, InputStream> allegatiMap, String username) throws MyStandardException, IOException {
        if (entity != null) {

            List<String> allegatiInseritiMyBox = new ArrayList<>();
            List<String> queryList = new ArrayList<>();
            String individualIdEntita = null;

            try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

                try {
                    conn.begin(ReadWrite.WRITE);

                    OModel oModel = mystandardConfig.getModel();
                    OClass oClass = getOClassFromModel(entityType);

                    if (oClass != null) {//

                        String owlPrefix = oClass.getPrefix();
                        String owlPrefixData = oClass.getPrefixData();

                        Map<String, Object> dataProperty = entity.getDataProperty();

                        Object idEntitaObject = dataProperty.get(owlPrefix + MyStandardConstants.ID_ENTITA_COLUMN_KEY);
                        if (idEntitaObject == null) {
                            throw new MyStandardException("Entità non modificabile in quanto l'id ricevuto è nullo.");
                        } else {
                            individualIdEntita = idEntitaObject.toString();
                            Resource individual = ResourceFactory.createResource(owlPrefixData + individualIdEntita);

                            if (individualAlreadyExists(conn, owlPrefix, individual)) {//Se esiste, modificare lo statement

                                //Get Statement
                                Property entityTypeIRI = ResourceFactory.createProperty(owlPrefix + entityType);
                                List<String> individualToBeUpdated = getDataPropertyStatement(conn, individual.getURI(), entityTypeIRI.getURI());

                                //Override Data properties
                                //Set delle data property
                                for (String key : dataProperty.keySet()) {
                                    Property propertyIri = ResourceFactory.createProperty(key);

                                    //Get value from json
                                    Object dataPropValueObj = dataProperty.get(key);
                                    String dataPropValue = dataPropValueObj != null ? dataPropValueObj.toString() : "";

                                    List<String> dataPropertyToBeUpdated = getDataPropertyStatement(conn, individual.getURI(), propertyIri.getURI());

                                    if (dataPropertyToBeUpdated == null || dataPropertyToBeUpdated.size() == 0) {
                                        queryList.add(getUpdateBuilderInsert(individual, propertyIri, ResourceFactory.createStringLiteral(dataPropValue)));
                                    } else {

                                        String dataPropertyValue = dataPropertyToBeUpdated.get(0);//Data Property singola

                                        if (!dataPropertyValue.equals(dataPropValue)) {
                                            if (StringUtils.hasText(dataPropValue)) {
                                                //Sto modificando una data property con un valore
                                                queryList.add(getUpdateBuilderDelete(individual, propertyIri, null));
                                                queryList.add(getUpdateBuilderInsert(individual, propertyIri, ResourceFactory.createStringLiteral(dataPropValue)));

                                            } else {
                                                //sto sbiancando una data property
                                                queryList.add(getUpdateBuilderDelete(individual, propertyIri, null));
                                            }
                                        }
                                    }


                                }

                                //Aggiornamento data update
                                queryList.add(getUpdateBuilderDelete(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.DT_UPD_COLUMN_KEY), null));
                                queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.DT_UPD_COLUMN_KEY), ResourceFactory.createStringLiteral(ZonedDateTime.now().format(MyStandardUtil.MYSTANDARD_DATETIME_ZONED_FORMATTER))));

                                queryList.add(getUpdateBuilderDelete(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.UTE_UPD_COLUMN_KEY), null));
                                queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.UTE_UPD_COLUMN_KEY), ResourceFactory.createStringLiteral(username)));


                                //Entity Properties
                                List<MyStandardEntityPropertyIndividual> entityPropertyList = entity.getEntityProperty();
                                if (entityPropertyList != null && entityPropertyList.size() > 0) {
                                    for (MyStandardEntityPropertyIndividual entityPropertyIndividual : entityPropertyList) {

                                        MyStandardIndividualOperationEnum entityPropertyOperation = entityPropertyIndividual.get_operation();
                                        if (MyStandardIndividualOperationEnum.ADD.equals(entityPropertyOperation)) {
                                            addEntityProperty(queryList, allegatiMap, owlPrefix, owlPrefixData, conn, individualIdEntita, individual, entityPropertyIndividual, allegatiInseritiMyBox);
                                        } else if (MyStandardIndividualOperationEnum.MODIFY.equals(entityPropertyOperation)) {
                                            modifyEntityProperty(queryList, conn, owlPrefix, allegatiMap, individualIdEntita, individual, entityPropertyIndividual);
                                        } else if (MyStandardIndividualOperationEnum.REMOVE.equals(entityPropertyOperation)) {
                                            removeEntityProperty(queryList, owlPrefix, conn, individualIdEntita, individual, entityPropertyIndividual);

                                        } else {
                                            throw new MyStandardException("Nessuna operazione prevista per codice " + (entityPropertyOperation != null ? entityPropertyOperation.get_operation() : null));
                                        }

                                    }
                                }

                                //Functional properties
                                //Functional property
                                List<MyStandardFunctionalPropertyIndividual> functionalPropertyList = entity.getFunctionalProperty();
                                if (functionalPropertyList != null && functionalPropertyList.size() > 0) {
                                    for (MyStandardFunctionalPropertyIndividual functionalPropertyIndividual : functionalPropertyList) {
                                        MyStandardIndividualOperationEnum entityPropertyOperation = functionalPropertyIndividual.get_operation();
                                        if (MyStandardIndividualOperationEnum.ADD.equals(entityPropertyOperation)) {
                                            addFunctionalProperty(queryList, oModel, conn, individual, functionalPropertyIndividual);
                                        } else if (MyStandardIndividualOperationEnum.REMOVE.equals(entityPropertyOperation)) {
                                            removeFunctionalProperty(queryList, oModel, conn, individual, functionalPropertyIndividual);
                                        } else {
                                            throw new MyStandardException("Nessuna operazione prevista per codice " + (entityPropertyOperation != null ? entityPropertyOperation.get_operation() : null));
                                        }
                                    }
                                }

                                String fullQuery = String.join(" ; \n", queryList);

                                conn.update(fullQuery);
                                LOG.debug("MyStandard - Individual aggiornato correttamente.");

                                conn.commit();

                                //Nessun inserimento nello storico
                            } else {
                                throw new MyStandardException("Entità " + individualIdEntita + " non modificabile perchè non esistente");
                            }
                        }


                    } else {
                        throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
                    }



                } catch (Exception e) {
                    if (allegatiInseritiMyBox.size() > 0) {
                        for (String idAllegato : allegatiInseritiMyBox) {
                            try {
                                attachmentsService.delete(individualIdEntita, idAllegato);
                            } catch (RepositoryAccessException ex) {
                                throw new MyStandardException("Impossibile accedere a MyBox");
                            } catch (Exception ex) {
                                throw new MyStandardException("Errore generico nella cancellazione di un file accesso a MyBox");
                            }
                        }
                    }
                    if (!conn.isClosed()) {
                        conn.abort();
                    }
                    throw e;
                } finally {
                    conn.end();
                }
            }

        } else {
            throw new MyStandardException("Oggetto entity ricevuto nullo");
        }
    }

    @Override
    public void publishEntityIndividual(String entityType, String codice, Integer versione, String username) throws MyStandardException, IOException {

        List<String> queryList = new ArrayList<>();
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            try {
                conn.begin(ReadWrite.WRITE);

                OClass oClass = getOClassFromModel(entityType);

                if (oClass != null) {//

                    String owlPrefix = oClass.getPrefix();
                    String owlPrefixData = oClass.getPrefixData();

                    String individualIdEntita = codice + "_" + versione;
                    Resource individual = ResourceFactory.createResource(owlPrefixData + individualIdEntita);

                    if (individualAlreadyExists(conn, owlPrefix, individual)) {//Se esiste, modificare lo statement

                        //Get vecchio stato
                        Property statoIRI = ResourceFactory.createProperty(owlPrefix + MyStandardConstants.STATO_OBJPROP_KEY);
                        List<String> dataPropertyStato = getDataPropertyStatement(conn, individual.getURI(), statoIRI.getURI());

                        if (dataPropertyStato == null || dataPropertyStato.size() == 0) {
                            throw new MyStandardException("Stato nullo. Deve essere valorizzato ad inserito prima di poter essere pubblicato.");
                        } else {

                            String statoInserito = owlPrefix + MyStandardStatoEnum.INSERITO.getCode();
                            if (statoInserito.equals(dataPropertyStato.get(0))) {

                                queryList.add(getUpdateBuilderDelete(individual, statoIRI, null));
                                queryList.add(getUpdateBuilderInsert(individual, statoIRI, ResourceFactory.createResource(owlPrefix + MyStandardStatoEnum.PUBBLICATO.getCode())));


                                String fullQuery = String.join(" ; \n", queryList);

                                conn.update(fullQuery);
                                LOG.debug("MyStandard - Individual pubblicato correttamente.");

                                conn.commit();


                                //Inserimento nello storico dell'operazione di insert
                                storicoService.insertStorico(MyStandardIndividualOperationEnum.PUBLISH.get_description(), individualIdEntita,
                                        MyStandardStatoEnum.INSERITO.getCode(), MyStandardStatoEnum.PUBBLICATO.getCode(), ZonedDateTime.now().format(MyStandardUtil.MYSTANDARD_DATETIME_ZONED_FORMATTER), username, null);


                            } else {
                                throw new MyStandardException("Stato diverso da inserito. Stato: " + dataPropertyStato.get(0));
                            }
                            //Set data update

                        }

                    } else {
                        throw new MyStandardException("Entità " + individualIdEntita + " non modificabile perchè non esistente");
                    }


                } else {
                    throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
                }
            } catch (Exception e) {
                if (!conn.isClosed()) {
                    conn.abort();
                }
                throw e;
            } finally {
                conn.end();
            }
        }
    }

    /**
     * Si verifica che ci sia un unico statement per individual, e lo si ritorna
     * @param conn, repository i dati
     * @param subject, soggetto da ricercare
     * @param predicate, predicato da ricercare
     * @return uri
     * @throws MyStandardException
     */
    private List<String> getDataPropertyStatement(RDFConnection conn, String subject, String predicate) throws MyStandardException {

        List<String> result = new ArrayList<>();
        try {
            //Get queryObjectStatement
            String queryObjectStatement =  MyStandardQuery.getQueryObjectStatement(subject, predicate);

            conn.querySelect(queryObjectStatement, (querySolution) -> {

                RDFNode value = querySolution.get(MyStandardConstants.OBJECT_COLUMN_KEY);
                if (value instanceof Literal) {
                    result.add(querySolution.getLiteral(MyStandardConstants.OBJECT_COLUMN_KEY).getString());
                } else {
                    result.add(querySolution.getResource(MyStandardConstants.OBJECT_COLUMN_KEY).getURI());
                }

            });


        } catch (Exception e) {
            LOG.error("MyStandard - Errore generico in esecuzione query SPARQL", e);
        }

        return result;

    }


    @Override
    public void deleteEntityIndividual(String entityType, String codice, Integer versione, String username, String note) throws MyStandardException, IOException {

        List<String> queryList = new ArrayList<>();
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            try {
                conn.begin(ReadWrite.WRITE);

                OModel oModel = mystandardConfig.getModel();
                OClass oClass = getOClassFromModel(entityType);

                if (oClass != null) {//

                    String owlPrefix = oClass.getPrefix();
                    String owlPrefixData = oClass.getPrefixData();

                    String individualIdEntita = codice + "_" + versione;
                    ;
                    Resource individual = ResourceFactory.createResource(owlPrefixData + individualIdEntita);
                    if (individualAlreadyExists(conn, owlPrefix, individual)) {//Se esiste, modificare lo statement

                        Map<String, OClass> modelClasses = oModel.getClasses();
                        if (modelClasses.containsKey(owlPrefix + entityType)) {
                            OClass modelClass = modelClasses.get(owlPrefix + entityType);

                            Map<String, OProperty> objectProperties = modelClass.getObjectProperty();
                            for (Map.Entry<String, OProperty> entry : objectProperties.entrySet()) {
                                OProperty objectProperty = entry.getValue();

                                //Per lo stato ed il dominio l'individual non deve essere eliminato
                                if (!(owlPrefix + MyStandardConstants.STATO_OBJPROP_KEY).equals(objectProperty.getIRI())
                                 && !(owlPrefix + MyStandardConstants.DOMINIO_BUSINESS_OBJPROP_KEY).equals(objectProperty.getIRI())) {

                                    List<String> statements = getDataPropertyStatement(conn, individual.getURI(), objectProperty.getIRI());
                                    for (String objStmtValue : statements) {
                                        //Gestione allegati: mybox e owl
                                        if ((owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN).equals(objectProperty.getIRI())) {
                                            List<String> idAllegatoList = getDataPropertyStatement(conn, objStmtValue, owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY);
                                            if (idAllegatoList == null || idAllegatoList.size() != 1) {
                                                throw new MyStandardException("Nessun  o più id_documento per allegato " + objStmtValue);
                                            } else {
                                                String idAllegato = idAllegatoList.get(0);
                                                try {
                                                    attachmentsService.delete(individualIdEntita, idAllegato);
                                                    LOG.debug("MyStandard - Allegato " + objStmtValue + " eliminato correttamente da MyBox.");
                                                } catch (RepositoryAccessException e) {
                                                    throw new MyStandardException("Impossibile accedere a MyBox");
                                                } catch (Exception e) {
                                                    throw new MyStandardException("Errore generico nella cancellazione di un file accesso a MyBox");
                                                }
                                            }
                                        }


                                        try {
                                            queryList.add(getUpdateBuilderDelete(ResourceFactory.createResource(objStmtValue), null, null));
                                        } catch (Exception e) {
                                            LOG.debug("MyStandard - Value " + objStmtValue + " is not a valid IRI");
                                        }

                                    }
                                }
                            }

                            //Gestione lista relazioni
                            List<String> relationsList = modelClass.getRelations();
                            for (String relation: relationsList) {

                                List<String> statements = getDataPropertyStatement(conn, individual.getURI(), relation);
                                for (String individualFunctionalInverse: statements) {
                                    //Si cerca IRI dell'inversa
                                    Map<String, ORelation> oModelRelation = oModel.getRelations();
                                    ORelation oRelation = oModelRelation.get(relation);
                                    if (oRelation != null) {
                                        String inverseIRI = oRelation.getInverseOf();
                                        if (StringUtils.hasText(inverseIRI)) {
                                            queryList.add(getUpdateBuilderDelete(ResourceFactory.createResource(individualFunctionalInverse), ResourceFactory.createProperty(inverseIRI), individual));

                                        } else {
                                            LOG.error("MyStandard - Relazione inversa di " + relation + " non trovata. Errore non bloccante");
                                        }
                                    } else {
                                        LOG.error("MyStandard - Relazione " + relation + " non presente nel model. Errore non bloccante");
                                    }
                                }

                            }

                        }


                        //Delete individual and functional properties relations
                        queryList.add(getUpdateBuilderDelete(individual, null, null));

                        String fullQuery = String.join(" ; \n", queryList);

                        conn.update(fullQuery);
                        LOG.debug("MyStandard - Individual eliminato correttamente.");

                        conn.commit();

                        //Inserimento nello storico dell'operazione di insert
                        storicoService.insertStorico(MyStandardIndividualOperationEnum.REMOVE.get_description(), individualIdEntita,
                                MyStandardStatoEnum.INSERITO.getCode(), MyStandardStatoEnum.ELIMINATO.getCode(), ZonedDateTime.now().format(MyStandardUtil.MYSTANDARD_DATETIME_ZONED_FORMATTER), username, note);



                    } else {
                        throw new MyStandardException("Entità " + individualIdEntita + " non eliminabile perchè non esistente");
                    }

                } else {
                    throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
                }


                } catch (Exception e) {
                if (!conn.isClosed()) {
                    conn.abort();
                }
                throw e;
            } finally {
                conn.end();
            }
        }
    }

    @Override
    public byte[] exportRdfStatements() throws MyStandardException {

        LOG.debug("MyStandard - Richiesta export RDF file");

        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {


            Model model = conn.fetch();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();

            Txn.executeRead(conn, ()-> {
                RDFDataMgr.write(bao, model, RDFFormat.RDFXML) ;
            }) ;

            LOG.debug("MyStandard - Export RDF file estratto.");

            return bao.toByteArray();

        }

    }

    @Override
    public List<JSONObject> findRelazioniByEntitaCodiceVersione(String entityType, String codice, Integer versione, MyStandardFilter filter) {
        List<JSONObject> individuals = new ArrayList<>();
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            OClass oClass = getOClassFromModel(entityType);

            if (oClass != null) {//

                String owlPrefix = oClass.getPrefix();
                String queryAllRelationsByCodiceAndVersione = MyStandardQuery.getQueryAllRelationsByCodiceAndVersione(entityType, owlPrefix, codice, versione, filter);

                conn.querySelect(queryAllRelationsByCodiceAndVersione, (querySolution) -> {
                    JSONObject data = new JSONObject();//Si aggiungono i dati dell'entità

                    data.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.VERSIONE_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.STATO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()) : "");
                    data.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.NAME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.NAME_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY) != null ? 
messageSource.getMessage(getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()), null, getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()) , null) : "");
                    data.put(MyStandardConstants.ID_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.ID_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.ID_ENTITA_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.FUNCTIONAL_PROP_TARGET_INDIVIDUALS_IRI, querySolution.get(MyStandardConstants.INDIVIDUALS_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.INDIVIDUALS_COLUMN_KEY).getURI()) : "");

                    individuals.add(data);
                });


            } else {
                throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
            }

        } catch (Exception e) {
            LOG.error("MyStandard - Errore nel recupero dei dati RDF", e);
        }
        return individuals;
    }

    @Override
    public List<JSONObject> findAllRelazioni(String entityType, String dominio, MyStandardFilter filter) {
        List<JSONObject> individuals = new ArrayList<>();
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            //Get prefix da oModel
            OModel oModel = mystandardConfig.getModel();
            OClass oClass;
            Map<String, OClass> oModelClasses = oModel.getClasses();
            Optional<Map.Entry<String, OClass>> optionaloClass = oModelClasses.entrySet().stream()
                    .filter(entry -> entityType.equals(entry.getValue().getLocalName()))
                    .findFirst();

            if (optionaloClass.isPresent()) {

                oClass = optionaloClass.get().getValue();
                String owlPrefix = oClass.getPrefix();

                String queryAllRelationsByEntity = MyStandardQuery.getQueryAllRelationsByEntity(myStandardProperties, entityType, owlPrefix, dominio, filter);

                conn.querySelect(queryAllRelationsByEntity, (querySolution) -> {
                    JSONObject data = new JSONObject();//Si aggiungono i dati dell'entità

                    data.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.VERSIONE_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.STATO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()) : "");
                    data.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.NAME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.NAME_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.ID_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.ID_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.ID_ENTITA_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.FUNCTIONAL_PROP_TARGET_INDIVIDUALS_IRI, querySolution.get(MyStandardConstants.INDIVIDUALS_COLUMN_KEY) != null ? querySolution.getResource(MyStandardConstants.INDIVIDUALS_COLUMN_KEY).getURI() : "");


                    data.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()) : entityType);

                    individuals.add(data);
                });

            } else {
                throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
            }

        } catch (Exception e) {
            LOG.error("MyStandard - Errore nel recupero dei dati RDF", e);
        }
        return individuals;

    }

    @Override
    public JSONObject executeQuery(MyStandardQueryRequest queryRequest) throws IOException, MyStandardException {
        JSONObject jsonResult = new JSONObject();
        JSONArray values = new JSONArray();
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            String query = addPaginationAndSortingManuallyToQuery(queryRequest);
            
            conn.querySelect(query, (querySolution) -> {
                List<String> columns = new ArrayList<>();
                querySolution.varNames().forEachRemaining(columns::add);

                columns.sort(Comparator.naturalOrder());
                jsonResult.put("columns", columns);
                JSONObject record = new JSONObject();
                for (String column: columns) {
                    record.put(column, getStringWithoutPrefix(querySolution.get(column).toString()));
                }

                values.put(record);
            });

            jsonResult.put("values", values);

        } catch (Exception e) {
            LOG.error("MyStandard - Errore generico nell'esecuzione della query SPARQL", e);
            throw new MyStandardException("Errore generico nell'esecuzione della query SPARQL");
        }

        return jsonResult;
    }


    /**
     * Si aggiunge la paginazione manualmente
     * @param queryRequest, dati query
     * @return
     */
    private String addPaginationAndSortingManuallyToQuery(MyStandardQueryRequest queryRequest) {

        String query = queryRequest.getQuery();
        Integer pageSize = queryRequest.getPageSize();
        Integer pageNum = queryRequest.getPageNum();
        String sortField = queryRequest.getSortField();
        String sortDirection = queryRequest.getSortDirection();

        if (StringUtils.hasText(sortField)) {
            if (StringUtils.hasText(sortDirection)) {
                query += sortDirection.equals("desc") ? " ORDER BY DESC(?" + sortField + ") " : " ORDER BY ASC(?" + sortField + ") ";
            } else {
                query += " ORDER BY ASC(?" + sortField + ") ";
            }

        }

        if (pageSize != null && pageNum != null) {
            Integer offset = (pageNum * pageSize);
            if (offset > 0) query+= " OFFSET " + offset;
            query += " LIMIT " + pageSize;
        }
        return query;
    }

    @Override
    public JSONObject findDatiDaVocabolario(String vocabularyPath, String codeIRI, String descIRI, String objPropIRI) {

        LOG.debug("MyStandard - Esecuzione query per ottenere i dati da vocabolario.");

        JSONObject vocabularyData = new JSONObject();
        // Open a connection to the database
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            List<JSONObject> values = new ArrayList<>();
            String queryVocabulary = MyStandardQuery.getQueryVocabulary(codeIRI, descIRI);

            conn.querySelect(queryVocabulary, (querySolution) -> {
                JSONObject codeObj = getVocabularyJsonColumn(MyStandardUtil.mystandardPrefixForMorfeo(getStringWithoutPrefix(objPropIRI), getStringWithoutPrefix(codeIRI)),  MyStandardConstants.CODE_KEY);
                JSONObject descObj = getVocabularyJsonColumn(MyStandardUtil.mystandardPrefixForMorfeo(getStringWithoutPrefix(objPropIRI), getStringWithoutPrefix(descIRI)), MyStandardConstants.DESC_KEY);


                vocabularyData.put(MyStandardConstants.COLUMNS_TYPE, Arrays.asList(codeObj, descObj));

                JSONObject data = new JSONObject();

                String literalCode = querySolution.getLiteral(MyStandardConstants.CODE_KEY).getString();
                String legalDesc = querySolution.getLiteral(MyStandardConstants.DESC_KEY).getString();
                data.put(MyStandardUtil.mystandardPrefixForMorfeo(getStringWithoutPrefix(objPropIRI), getStringWithoutPrefix(codeIRI)), literalCode);
                data.put(MyStandardUtil.mystandardPrefixForMorfeo(getStringWithoutPrefix(objPropIRI), getStringWithoutPrefix(descIRI)), legalDesc);


                values.add(data);
            });

            vocabularyData.put(MyStandardConstants.SPARQL_VALUE_COLUMN_KEY, values);

            LOG.debug("MyStandard - Ottenuti {} dati da vocabolario.", values.size());

        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
        }



        return vocabularyData;
    }

    private JSONObject getVocabularyJsonColumn(String value, String name) {
        JSONObject descObj = new JSONObject();
        descObj.put(MyStandardConstants.VALUE_KEY, value);
        descObj.put(MyStandardConstants.NAME_KEY, name);
        return descObj;
    }


    @Override
    public JSONObject findMaxVersioneByCodice(String entityType, String codice) {
        JSONObject maxVersioneStato = new JSONObject();
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            OClass oClass = getOClassFromModel(entityType);

            if (oClass != null) {//

                String owlPrefix = oClass.getPrefix();
                String queryMaxVersioneByCodice = MyStandardQuery.getBuilderQueryMaxVersioneByCodice(entityType, owlPrefix, codice);

                conn.querySelect(queryMaxVersioneByCodice, (querySolution) -> {

                    RDFNode versioneResult = querySolution.get(MyStandardConstants.VERSIONE_COLUMN_KEY);
                    RDFNode statoResult = querySolution.get(MyStandardConstants.STATO_COLUMN_KEY);
                    maxVersioneStato.put(MyStandardConstants.VERSIONE_COLUMN_KEY, versioneResult.toString());
                    maxVersioneStato.put(MyStandardConstants.STATO_COLUMN_KEY, getStringWithoutPrefix(statoResult.toString()));

                });


            } else {
                throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
            }

        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
        }

        return maxVersioneStato;
    }


    @Override
    public String findIpaCodeDefinitoDaIdEntita(String idEntita) {
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        final String[] ipaCode = {null};
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            OModel oModel = mystandardConfig.getModel();
            Map<String, OClass> oModelClasses = oModel.getClasses();
            OClass oClass = oModelClasses.entrySet().stream()
                    .filter(entry -> "Ente".equals(entry.getValue().getLocalName()))
                    .findFirst().get().getValue();

            if (oClass != null) {//

                String owlPrefix = oClass.getPrefix();

                String queryIpaCodeDefinisceIdEntita = MyStandardQuery.getQueryIpaCodeDefinisceIdEntita(myStandardProperties, owlPrefix, idEntita);
                conn.querySelect(queryIpaCodeDefinisceIdEntita, (querySolution) -> {
                    ipaCode[0] = querySolution.getLiteral(MyStandardConstants.IPA_CODE_COLUMN_KEY).getString();
                });



            } else {
                throw new MyStandardException("Entity Ente non presente come container nel file di configurazione.");
            }

        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
        }

        return ipaCode[0];
    }

    @Override
    public List<JSONObject> getStatoAndIpaSingleEntityData(String entityType, String codice, Integer versione) throws MyStandardException, IOException, URISyntaxException {
        List<JSONObject> statoDomainIpaData = new ArrayList<>();

        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            OClass oClass = getOClassFromModel(entityType);

            if (oClass != null) {//

                String owlPrefix = oClass.getPrefix();
                String queryStatoAndIpaCodeEntita = MyStandardQuery.getQueryStatoAndIpaCodeEntita(myStandardProperties, owlPrefix, entityType, codice, versione);
                conn.querySelect(queryStatoAndIpaCodeEntita, (querySolution) -> {
                    JSONObject data = new JSONObject();//Si aggiungono i dati dell'entità

                    data.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.STATO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()) : "");
                    data.put(MyStandardConstants.DEFINITA_DA_COLUMN_KEY, querySolution.get(MyStandardConstants.IPA_CODE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.IPA_CODE_COLUMN_KEY).getString()) : "");
                    data.put(MyStandardConstants.DOMINIO_BUSINESS_KEY, querySolution.get(MyStandardConstants.DOMINIO_BUSINESS_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.DOMINIO_BUSINESS_KEY).getURI()) : "");
                    data.put(MyStandardConstants.SPECIALIZZAZIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.SPECIALIZZAZIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.SPECIALIZZAZIONE_COLUMN_KEY).getURI()) : "");





                    statoDomainIpaData.add(data);
                });

            } else {
                throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
            }


        } catch (Exception e) {
            LOG.error("Errore generico nel recupero dei dati RDF", e);
            throw e;
        }

        return statoDomainIpaData;
    }

    @Override
    public void updateEntityState(String operazione, String entityType, String codice, Integer versione, String originalState, String nextState, String username, String note) throws MyStandardException {
        List<String> queryList = new ArrayList<>();
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            try {
                conn.begin(ReadWrite.WRITE);

                OClass oClass = getOClassFromModel(entityType);

                if (oClass != null) {//

                    String owlPrefix = oClass.getPrefix();
                    String owlPrefixData = oClass.getPrefixData();

                    String individualIdEntita = codice + "_" + versione;
                    Resource individual = ResourceFactory.createResource(owlPrefixData + individualIdEntita);

                    if (individualAlreadyExists(conn, owlPrefix, individual)) {//Se esiste, modificare lo statement

                        //Get vecchio stato
                        Property statoIRI = ResourceFactory.createProperty(owlPrefix + MyStandardConstants.STATO_OBJPROP_KEY);

                        queryList.add(getUpdateBuilderDelete(individual, statoIRI, null));
                        queryList.add(getUpdateBuilderInsert(individual, statoIRI, ResourceFactory.createResource(owlPrefix + nextState)));

                        queryList.add(getUpdateBuilderDelete(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.DT_UPD_COLUMN_KEY), null));
                        queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.DT_UPD_COLUMN_KEY), ResourceFactory.createStringLiteral(ZonedDateTime.now().format(MyStandardUtil.MYSTANDARD_DATETIME_ZONED_FORMATTER))));

                        queryList.add(getUpdateBuilderDelete(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.UTE_UPD_COLUMN_KEY), null));
                        queryList.add(getUpdateBuilderInsert(individual, ResourceFactory.createProperty(owlPrefix + MyStandardConstants.UTE_UPD_COLUMN_KEY), ResourceFactory.createStringLiteral(username)));


                        String fullQuery = String.join(" ; \n", queryList);

                        conn.update(fullQuery);
                        LOG.debug("Stato aggiornato correttamente.");

                        conn.commit();

                        //Inserimento nello storico dell'operazione di insert

                        storicoService.insertStorico(MyStandardEntityOperationEnum.of(operazione).getDescription(), individualIdEntita,
                                originalState, nextState, ZonedDateTime.now().format(MyStandardUtil.MYSTANDARD_DATETIME_ZONED_FORMATTER), username, note);


                    } else {
                        throw new MyStandardException("Entità " + individualIdEntita + " non modificabile perchè non esistente");
                    }


                } else {
                    throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
                }
            } catch (Exception e) {
                if (!conn.isClosed()) {
                    conn.abort();
                }
                throw e;
            } finally {
                conn.end();
            }
        }
    }


    /**
     * Si ritorna una stringa senza il prefix (se presente) oppure come substring a partire dall'ultimo "/" (per evitare nome completo in ontopia)
     * @param value, valore da ritornare manipolato
     * @return valore manipolato
     */
    private String getStringWithoutPrefix(String value) {

        for (String prefixToBeReplaced: PREFIX_LIST) {
            value = value.replace(prefixToBeReplaced, "");
        }
        return value;
    }


    @Override
    public List<JSONObject> findAllBacheca(MyStandardFilter filter) throws MyStandardException {
        List<JSONObject> individuals = new ArrayList<>();

        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            String queryAllByEntity = MyStandardQuery.getQueryBachecaAllByEntity(myStandardProperties, filter);

            conn.querySelect(queryAllByEntity, (querySolution) -> {
                JSONObject data = new JSONObject();//Si aggiungono i dati dell'entità

                data.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()) : "");
                data.put(MyStandardConstants.LABEL_TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY) != null ?
                        messageSource.getMessage(getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()), null, getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()) , null) : "");

                data.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.VERSIONE_COLUMN_KEY).getString()) : "");
                data.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY).getString()) : "");
                data.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.STATO_COLUMN_KEY) != null ?
                        messageSource.getMessage(getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()), null, getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()) , null) : "");
                data.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.NAME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.NAME_COLUMN_KEY).getString()) : "");
                data.put(MyStandardConstants.DEFINITA_DA_COLUMN_KEY, querySolution.get(MyStandardConstants.IPA_CODE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.IPA_CODE_COLUMN_KEY).getString()) : "");
                data.put(MyStandardConstants.DOMINIO_BUSINESS_KEY, querySolution.get(MyStandardConstants.DOMINIO_BUSINESS_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.DOMINIO_BUSINESS_KEY).getString()) : "");

                setUltimaModificaData(querySolution, data);

                individuals.add(data);

            });

        } catch (Exception e) {
            LOG.error("Errore generico nel recupero dei dati RDF", e);
            throw e;
        }

        return individuals;
    }

    /**
     * Si aggiungono come JSONObject i dati da ritornare nella bacheca
     * @param querySolution, dati tornati dalla query
     * @param data, object da tornare con i dati della bacheca
     */
    private void addBachecaRowAsJsonObject(QuerySolution querySolution, JSONObject data) {

        data.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY) != null ? 
messageSource.getMessage(getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()), null, getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()) , null) : "");
        data.put(MyStandardConstants.DOMINIO_BUSINESS_KEY, querySolution.get(MyStandardConstants.DOMINIO_BUSINESS_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.DOMINIO_BUSINESS_KEY).getURI()) : "");
        data.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.VERSIONE_COLUMN_KEY).getString()) : "");
        data.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY).getString()) : "");
        data.put(MyStandardConstants.STATO_COLUMN_KEY, querySolution.get(MyStandardConstants.STATO_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.STATO_COLUMN_KEY).getURI()) : "");
        data.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.NAME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.NAME_COLUMN_KEY).getString()) : "");
        data.put(MyStandardConstants.DEFINITA_DA_COLUMN_KEY, querySolution.get(MyStandardConstants.IPA_CODE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.IPA_CODE_COLUMN_KEY).getString()) : "");

        setUltimaModificaData(querySolution, data);

    }

    /**
     * Si aggiungono i dati di ultima modifica. Si ritornano due colonne per permettere ordinamento per data. Metodo specifico per la bacheca
     * @param querySolution, risultato della query
     * @param data, oggetto a cui aggiungere i dati
     */
    private void setUltimaModificaData(QuerySolution querySolution, JSONObject data) {
        //Ultima modifica: se esiste un update si tornano i dati di update, altrimenti si tornano i dati di insert
        String lastUpdateDate = querySolution.get(MyStandardConstants.DT_UPD_COLUMN_KEY) != null ? querySolution.getLiteral(MyStandardConstants.DT_UPD_COLUMN_KEY).getString() : null;
        String lastUserUpdate = querySolution.get(MyStandardConstants.UTE_UPD_COLUMN_KEY)!= null ? querySolution.getLiteral(MyStandardConstants.UTE_UPD_COLUMN_KEY).getString() : null;
        if (StringUtils.hasText(lastUpdateDate) && StringUtils.hasText(lastUserUpdate)) {
            data.put(MyStandardConstants.DATA_ULTIMA_MODIFICA_KEY, MyStandardUtil.convertDateTimePattern(lastUpdateDate));
            data.put(MyStandardConstants.UTENTE_ULTIMA_MODIFICA_KEY, lastUserUpdate);
        } else {

            String insertDate = querySolution.get(MyStandardConstants.DT_INS_COLUMN_KEY) != null ? querySolution.getLiteral(MyStandardConstants.DT_INS_COLUMN_KEY).getString() : null;
            String userInsert = querySolution.get(MyStandardConstants.UTE_INS_COLUMN_KEY)!= null ? querySolution.getLiteral(MyStandardConstants.UTE_INS_COLUMN_KEY).getString() : null;

            data.put(MyStandardConstants.DATA_ULTIMA_MODIFICA_KEY, MyStandardUtil.convertDateTimePattern(insertDate));
            data.put(MyStandardConstants.UTENTE_ULTIMA_MODIFICA_KEY, userInsert);

        }
    }


    @Override
    public Integer countAllBacheca(MyStandardFilter filter) {
        // Si ritorna il numero totale di records
        final Integer[] totalRecords = new Integer[1];
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            String queryCountAllByEntity = MyStandardQuery.getQueryCountBachecaAllByEntity(myStandardProperties, filter);

            conn.querySelect(queryCountAllByEntity, (querySolution) -> {
                totalRecords[0] = querySolution.get(MyStandardConstants.COUNT_INDIVIDUALS_KEY) != null ? Integer.parseInt(querySolution.getLiteral(MyStandardConstants.COUNT_INDIVIDUALS_KEY).getString()) : 0;

            });

        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
        }

        return totalRecords[0];
    }

    @Override
    public Integer countAllRelazioni(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException {
        // Si ritorna il numero totale di records
        final Integer[] totalRecords = new Integer[1];
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {
            String queryCountAllByEntity = MyStandardQuery.getQueryCountRelazioniAllByEntity(myStandardProperties, dominio, entityType, filter);
            conn.querySelect(queryCountAllByEntity, (querySolution) -> {
                totalRecords[0] = querySolution.get(MyStandardConstants.COUNT_INDIVIDUALS_KEY) != null ? Integer.parseInt(querySolution.getLiteral(MyStandardConstants.COUNT_INDIVIDUALS_KEY).getString()) : 0;
            });
        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
        }
        return totalRecords[0];
    }


    /**
     * Si verifica se ci sono entità pubblicate di recente
     */
    @Override
    public Boolean checkIfEntityPublishedRecently(String entityIRI) throws MyStandardException {
        // Si ritorna il numero totale di records
        final Integer[] totalRecords = new Integer[1];
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            String queryCountAllByEntity = MyStandardQuery.getQueryCountEntityPublishedInLastDays(myStandardProperties, entityIRI, NUMBER_DAYS_NEW_IN_CATALOG);

            conn.querySelect(queryCountAllByEntity, (querySolution) -> {
                totalRecords[0] = querySolution.get(MyStandardConstants.COUNT_INDIVIDUALS_KEY) != null ? Integer.parseInt(querySolution.getLiteral(MyStandardConstants.COUNT_INDIVIDUALS_KEY).getString()) : 0;

            });

        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
            throw e;
        }

        return totalRecords[0] > 0;
    }

    @Override
    public Integer countAllRelazioniByEntitaCodiceVersione(String entita, String codice, Integer versione, MyStandardFilter filter) throws MyStandardException {
        // Si ritorna il numero totale di records
        final Integer[] totalRecords = new Integer[1];
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {
            String queryCountAllByEntity = MyStandardQuery.getQueryCountRelazioniByEntitaCodiceVersione(myStandardProperties, entita, codice, versione, filter);
            conn.querySelect(queryCountAllByEntity, (querySolution) -> {
                totalRecords[0] = querySolution.get(MyStandardConstants.COUNT_INDIVIDUALS_KEY) != null ? Integer.parseInt(querySolution.getLiteral(MyStandardConstants.COUNT_INDIVIDUALS_KEY).getString()) : 0;
            });
        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
        }
        return totalRecords[0];
    }


    @Override
    public String getDataUltimoAggiornamento() throws MyStandardException {
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        final String[] dataUltimoAggiornamento = {null};
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {
            String queryDataUltimoAggiornamentoCatalogo = MyStandardQuery.getQueryDataUltimoAggiornamentoCatalogo(myStandardProperties);
            conn.querySelect(queryDataUltimoAggiornamentoCatalogo, (querySolution) -> {
                dataUltimoAggiornamento[0] = querySolution.getLiteral(MyStandardConstants.DATA_ULTIMO_AGGIORNAMENTO_COLUMN_KEY).getString();
            });
        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
        }
        return dataUltimoAggiornamento[0];
    }

    @Override
    public List<JSONObject> getEntitaCatalogo(MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        List<JSONObject> individuals = new ArrayList<>();

        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            String owlPrefix = myStandardProperties.getOwl().getDefaultPrefix();
            String queryCatalogoEntity = MyStandardQuery.getQueryCatalogoEntity(myStandardProperties, owlPrefix, myStandardMyPortalFilter);

            conn.querySelect(queryCatalogoEntity, (querySolution) -> {
                JSONObject data = new JSONObject();//Si aggiungono i dati dell'entità

                data.put(MyStandardConstants.VERSIONE_COLUMN_KEY, querySolution.get(MyStandardConstants.VERSIONE_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.VERSIONE_COLUMN_KEY).getString()) : "");
                data.put(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY).getString()) : "");
                data.put(MyStandardConstants.NAME_COLUMN_KEY, querySolution.get(MyStandardConstants.NAME_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getLiteral(MyStandardConstants.NAME_COLUMN_KEY).getString()) : "");
                data.put(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, querySolution.get(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY) != null ? getStringWithoutPrefix(querySolution.getResource(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY).getURI()) : "");
                data.put(MyStandardConstants.DOMINIO_BUSINESS_KEY, MyStandardConstants.DOMINIO_GENERALE);

                individuals.add(data);
            });



        } catch (Exception e) {
            LOG.error("Errore generico nel recupero dei dati RDF", e);
            throw e;
        }

        return individuals;

    }

    @Override
    public Integer countEntitaCatalogo(MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {


        // Si ritorna il numero totale di records
        final Integer[] totalRecords = new Integer[1];
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            String owlPrefix = myStandardProperties.getOwl().getDefaultPrefix();
            String queryCountCatalogo = MyStandardQuery.getQueryCountCatalogoEntity(myStandardProperties, owlPrefix, myStandardMyPortalFilter);
            conn.querySelect(queryCountCatalogo, (querySolution) -> {
                totalRecords[0] = querySolution.get(MyStandardConstants.COUNT_INDIVIDUALS_KEY) != null ? Integer.parseInt(querySolution.getLiteral(MyStandardConstants.COUNT_INDIVIDUALS_KEY).getString()) : 0;
            });
        } catch (Exception e) {
            LOG.error("Errore nel recupero dei dati RDF", e);
        }
        return totalRecords[0];
    }

    @Override
    public void testFusekiConnection() throws MyStandardException {
        try (RDFConnection conn = jenaRepositoryType.getRdfConnection(myStandardProperties)) {

            String queryTestFuseki = MyStandardQuery.getHealthFusekiQuery();
            conn.querySelect(queryTestFuseki, (querySolution) -> { });
        }
    }
}
