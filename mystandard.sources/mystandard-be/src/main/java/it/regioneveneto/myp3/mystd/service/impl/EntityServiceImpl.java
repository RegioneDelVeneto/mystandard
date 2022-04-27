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
package it.regioneveneto.myp3.mystd.service.impl;

import it.regioneveneto.myp3.mystd.bean.MyStandardRequest;
import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardEntityOperationEnum;
import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardRoleEnum;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntity;
import it.regioneveneto.myp3.mystd.bean.owl.OClass;
import it.regioneveneto.myp3.mystd.bean.owl.OIdentificable;
import it.regioneveneto.myp3.mystd.bean.owl.OModel;
import it.regioneveneto.myp3.mystd.bean.owl.OProperty;
import it.regioneveneto.myp3.mystd.config.MyStandardConfig;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardDetailProperties;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.fsm.CustomFSMReader;
import it.regioneveneto.myp3.mystd.repository.EntityDataRepository;
import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.service.*;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import it.regioneveneto.myp3.mystd.utils.MyStandardUtil;
import it.regioneveneto.myp3.mystd.validator.MyStandardValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EntityServiceImpl implements EntityService {

    public static final String ENTITIES = "/entities/";
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityServiceImpl.class);

    @Autowired
    private MyStandardProperties myStandardProperties;

    @Autowired
    private EntityStructureService entityStructureService;

    @Autowired
    private EntityDataRepository entityDataRepository;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private MyStandardConfig myStandardConfig;

    @Autowired
    private EntitySearchService entitySearchService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MyStandardDetailProperties myStandardDetailProperties;





    @Override
    public Object findAll(String dominio, String entityType, MyStandardFilter filter, UserWithAdditionalInfo user) throws MyStandardException, IOException, URISyntaxException {

        LOGGER.debug("MyStandard - Ricerca di tutte le entità {}", entityType);

        if (filter != null && filter.getIpa()) {//Si aggiungono al filtro per comodità l'info dell'ipa dell'utente
            if (user != null) {//Si cercano entità definite dall'ipa dell'utente autenticato.
                filter.setUserIpa(user.getIpa());
            } else {//Se non c'è un utente autenticato dare errore
                throw new MyStandardException("Nessun utente autenticato.");
            }
        }
        List<JSONObject> allDataFiltered = entityDataRepository.getAll(dominio, entityType, filter);

        MyStandardUtil.setPaginationFiltersAsNull(filter);
        Integer totalRecords = entityDataRepository.countAll(dominio, entityType, filter);//Numero totale, la lista jsonObject torna solo gli elementi paginati

        LOGGER.info("MyStandard - Estratte {} entità per tipo {}", totalRecords, entityType);

        return formDataService.getPaginatedDatatable(allDataFiltered, totalRecords);
    }

    @Override
    public Object findAll(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException, URISyntaxException {
        return entityDataRepository.getAll(dominio, entityType, filter);
    }


    @Override
    public List<JSONObject> findAllByCodice(String entityType, String codice, Integer versione) throws MyStandardException {
        return entityDataRepository.getAllByCodiceAndVersione(entityType, codice, versione);
    }

    @Override
    public Object findByCodiceAndVersione(String dominio, String entityType, String codice, Integer versione, boolean readOnly,
                                          UserWithAdditionalInfo user, Boolean skipDataInInverseTabs) throws MyStandardException, IOException, URISyntaxException {

        LOGGER.debug("MyStandard - Si ottiene entità per codice e versione");

        Boolean canUserOperateOnEntity = checkIfUserIsAuthenticated(dominio, entityType, user);
        OModel oModel = entityStructureService.getStructureModel(entityType);

        JSONObject entityData = entityDataRepository.getSingleEntityData(entityType, codice, versione);//Si ottengono dati da rdf
        List<JSONObject> historicEntityData = findAllByCodice(entityType, codice, versione);

        return formDataService.mergeStructureAndData(oModel, entityData, historicEntityData, entityType, getEntityOwlPrefix(entityType), readOnly, canUserOperateOnEntity,  skipDataInInverseTabs);//si esegue il merge dei dati all'interno della struttura
    }

    @Override
    public JSONObject getRawEntityByCodiceAndVersione(String entityType, String codice, Integer versione) throws MyStandardException, IOException, URISyntaxException {

        JSONObject entityData = entityDataRepository.getSingleEntityData(entityType, codice, versione);//Si ottengono dati da rdf

        return entityData;
    }
    

    private Boolean checkIfUserIsAuthenticated(String dominio, String entityType, UserWithAdditionalInfo user) {

        try {
            return checkOperationWithDomainAndRole(dominio, entityType, user);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object findAllRelations(String entityType, String dominio, MyStandardFilter filter) throws MyStandardException {

        LOGGER.debug("MyStandard - Si estraggono tutte le relazioni associabili all'entita.");

        List<JSONObject> allRelationsFiltered = entityDataRepository.findAllRelazioni(entityType, dominio, filter);

        MyStandardUtil.setPaginationFiltersAsNull(filter);
        Integer totalRecords = entityDataRepository.countAllRelazioni(dominio, entityType, filter);//Numero totale, la lista jsonObject torna solo gli elementi paginati

        LOGGER.debug("MyStandard - Estratte {} relazioni", totalRecords);

        return formDataService.getPaginatedDatatable(allRelationsFiltered, totalRecords);

    }


    private Object getDetailUpdatedReadonly(String dominio, String entityType, MyStandardEntity entity, UserWithAdditionalInfo user) throws MyStandardException, IOException, URISyntaxException {
        //Si richiama il metodo per vedere il dettaglio readonly
        Map<String, Object> dataProperty = entity.getDataProperty();

        String owlPrefix = getEntityOwlPrefix(entityType);

        Object codiceEntitaObject = dataProperty.get(owlPrefix + MyStandardConstants.CODICE_ENTITA_COLUMN_KEY);
        Object versioneObject = dataProperty.get(owlPrefix + MyStandardConstants.VERSIONE_COLUMN_KEY);
        String codice = codiceEntitaObject != null ? codiceEntitaObject.toString() : "";
        Integer versione = versioneObject != null ? Integer.parseInt(versioneObject.toString()) : 1;


        return findByCodiceAndVersione(dominio, entityType, codice, versione, true, user, false);//Return dettaglio readonly
    }

    @Override
    public Object getNewEntity(String dominio, String entityType, UserWithAdditionalInfo user) throws IOException, MyStandardException {

        LOGGER.debug("MyStandard - Si cerca la struttura per una nuova entità");

        if (checkOperationWithDomainAndRole(dominio, entityType, user)) {
            OModel oModel = entityStructureService.getStructureModel(entityType);

            return formDataService.getStructureWithEmptyData(oModel, entityType, getEntityOwlPrefix(entityType));

        } else {
            throw new MyStandardException("Impossibile ottenere la struttura per inserire una nuova entità");
        }
   }

    /**
     * Si verifica se l'utente autenticato può operare sul dominio
     * @param dominio, dominio dell'entità di cui si sta richiedendo di operare
     * @param user, utente autenticato
     * @param entityType, tipo entità
     * @return true se l'utente può operare sull'entità
     * @throws MyStandardException se non può operare sull'entità
     */
   private Boolean checkOperationWithDomainAndRole(String dominio, String entityType, UserWithAdditionalInfo user) throws MyStandardException {

       if (user != null) {

           if  (user.isOperatoreEnteLocale() && !isSubClassOfEntitaStrutturata(entityType)) {
               throw new MyStandardException("Utente di ente locale non può operare sull'entita " + entityType);
           } else if (user.isResponsabileDominio()) {

               ProfileUser profileUser = getProfileUserAuthenticated(user, true);
               String userDomain = profileUser.getDomain();
               List<String> classDomainList = profileUser.getClassDomain();

               if (!classDomainList.contains(entityType)) {
                   throw new MyStandardException("Entita " + entityType + " non presente tra le classi del Responsabile di Dominio dell'utente");
               } else if (!userDomain.equalsIgnoreCase(MyStandardConstants.DOMINIO_GENERALE) &&
                       !dominio.equalsIgnoreCase(userDomain)) {
                   throw new MyStandardException("Entita " + entityType + " non appartiene al dominio del Responsabile di Dominio dell'utente");
               }



           }

           return true;


       } else {
           throw new MyStandardException("Nessun utente autenticato. Operazione non permessa.");
       }
   }


    /**
     * Si verifica se l'entità è stata definita dall'ente a cui appartiene l'utente
     * @param idEntita, id entità da verificare
     * @param user, dati utente
     * @return true se è possibile operare sull'entità
     * @throws MyStandardException
     */
    private boolean checkEntitaStrutturataDefinitaDaEnteOperatore(String idEntita, UserWithAdditionalInfo user) throws MyStandardException {
        Boolean check = true;
        if (user != null) {

            if (user.isOperatoreEnteLocale()) {
                String enteDefinisceEntita = isEntityDefinedByAnotherEnte(idEntita);
                if (enteDefinisceEntita == null || !user.getIpa().equals(enteDefinisceEntita)) {
                    throw new MyStandardException("Entità strutturata definita da un ente diverso da " + user.getIpa());
                }
            } //else ok (operatore nazionale può operare su tutti)


        } else {
            throw new MyStandardException("Nessun utente autenticato.");
        }

        return check;
    }

   private Boolean isSubClassOfEntitaStrutturata(String entityType) throws MyStandardException {
       //Get prefix da oModel
       OModel oModel = myStandardConfig.getModel();
       Map<String, OClass> oModelClasses = oModel.getClasses();
       Optional<Entry<String, OClass>> oClass = oModelClasses.entrySet().stream()
               .filter(entry -> entityType.equals(entry.getValue().getLocalName()))
               .findFirst();

       if (oClass.isPresent() && oClass.get().getValue() != null) {//
           return oClass.get().getValue() .getShowIpaFilter();
       } else {
           throw new MyStandardException("Entity " + entityType + " non presente nel file di configurazione.");
       }
   }


   private Boolean isEntitaNazionale(String entityDefinitaDa) {
       if (StringUtils.hasText(entityDefinitaDa)) {
           return entityDefinitaDa.equalsIgnoreCase(myStandardProperties.getEnteNazionale());
       } else return true; //La definizione è presente in caso di ente locale
   }


    @Override
    @Transactional
    public void insertEntityByType(String domain, String entityType, MyStandardEntity entity, List<MultipartFile> allegati, UserWithAdditionalInfo user) throws MyStandardException, IOException, URISyntaxException {
        //Creazione hashmap per aggiunta entity Individual
        if (isValidNewEntity(entity, entityType)) {
            if (checkOperationWithDomainAndRole(domain, entityType, user)) {
                Map<String, InputStream> allegatiMap;
                if(allegati!=null) {
                    allegatiMap = allegati.stream()
                            .collect(Collectors.toMap(allegato -> allegato.getOriginalFilename(), allegato -> {
                                try {
                                    return allegato.getInputStream();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }));
                } else {
                    allegatiMap = new HashMap<>();
                }

                entityDataRepository.addEntityIndividual(domain, entityType, entity, allegatiMap, user);

                LOGGER.debug("MyStandard - Entità aggiunta correttamente");

                // Esecuzione indicizzazione nuova entità
                indicizzazioneEntita(domain, entityType, entity, true);

                LOGGER.debug("MyStandard - Indicizzazione entità avvenuta correttamente");

                //Si richiama il metodo per vedere il dettaglio readonly
            } else {
                throw new MyStandardException("Impossibile ottenere i dati dell'utente autenticato.");
            }
        }

    }


    /**
     * Si verifica che una nuova entità sia valida secondo le regole di validazione
     * @param entity, entità da aggiungere
     * @param entityType, tipo entità
     * @return true se entità valida
     * @throws MyStandardException se entità non valida
     */
    private boolean isValidNewEntity(MyStandardEntity entity, String entityType) throws MyStandardException {

        List<Map<String, String>> errors = new ArrayList<>();//Lista di errori

        //Dalla oClass si ricavano le regole di validazione della classe
        OClass oClass = getOClassFromModelByEntity(entityType);

        Map<String, OProperty> dataPropertyMap = oClass.getDataProperty();//Si prendono le data property dell'entità
        Map<String, OProperty> objectPropertyMap = oClass.getObjectProperty();///Si prendono le object property dell'entità

        MyStandardValidator.validateDataProperties(oClass.getPrefix(), dataPropertyMap, entity.getDataProperty(), errors);
        MyStandardValidator.validateObjectProperties(oClass.getPrefix(), objectPropertyMap, entity.getEntityProperty(), errors);

        if (errors.size() > 0) {
            throw new MyStandardException("Errore nella validazione nell'inserimento dell'entità", errors);
            //return true;
        } else {
            return true;
        }

    }

    /**
     * Si verifica che il delta di update di una entità sia valido secondo le regole di validazione
     * @param entity, entità da modificare
     * @param entityType, tipo entità
     * @return true se entità valida
     * @throws MyStandardException se entità non valida
     */
    private boolean isValidEntityUpdate(MyStandardEntity entity, String entityType) throws MyStandardException {

        List<Map<String, String>> errors = new ArrayList<>();//Lista di errori

        //Dalla oClass si ricavano le regole di validazione della classe
        OClass oClass = getOClassFromModelByEntity(entityType);

        Map<String, OProperty> dataPropertyMap = oClass.getDataProperty();//Si prendono le data property dell'entità
        Map<String, OProperty> objectPropertyMap = oClass.getObjectProperty();///Si prendono le object property dell'entità

        MyStandardValidator.validateDeltaDataProperties(oClass.getPrefix(), dataPropertyMap, entity.getDataProperty(), errors);
        MyStandardValidator.validateDeltaObjectProperties(oClass.getPrefix(), objectPropertyMap, entity.getEntityProperty(), errors);

        if (errors.size() > 0) {
            throw new MyStandardException("Errore nella validazione nell'aggiornamento dell'entità", errors);
        } else {
            return true;
        }

    }


    /**
     * Si ottiene la classe dal model dal tipo di entità
     * @param entityType, tipo entità
     * @return class of entità
     * @throws MyStandardException
     */
    private OClass getOClassFromModelByEntity(String entityType) throws MyStandardException {
        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        Optional<Entry<String, OClass>> optionalClass = oModelClasses.entrySet().stream()
                .filter(entry -> entityType.equals(entry.getValue().getLocalName()))
                .findFirst();

        if (optionalClass.isPresent() && optionalClass.get().getValue() != null) {
            return optionalClass.get().getValue();//Classe nel model per entity type
        } else {
            throw new MyStandardException("Entity " + entityType + " non presente nel file di configurazione.");

        }
    }


    @Override
    @Transactional
    public Object updateEntityByType(String dominio, String entityType, MyStandardEntity entity, List<MultipartFile> allegati, UserWithAdditionalInfo user) throws MyStandardException, IOException, URISyntaxException {



        String codice = getDataFromDataProperty(entity, getEntityOwlPrefix(entityType), MyStandardConstants.CODICE_ENTITA_COLUMN_KEY);
        String versione = getDataFromDataProperty(entity, getEntityOwlPrefix(entityType), MyStandardConstants.VERSIONE_COLUMN_KEY);
        String idEntita = codice + "_" + versione;

        List<JSONObject> operations = (List<JSONObject>) findAllOperations(entityType, codice, Integer.parseInt(versione), user);


        if (    isValidEntityUpdate(entity, entityType) &&
                checkOperationWithDomainAndRole(dominio, entityType, user) && //Si verifica che si possa operare sul dominio
                checkOperationAllowed(MyStandardEntityOperationEnum.MODIFICA.getOperation(), operations) && //Si verifica che l'eliminazione sia un'operazione ammessa
                checkEntitaStrutturataDefinitaDaEnteOperatore(idEntita, user)) {//Si verifica che l'entità sia stata definita dal mio ente

            Map<String, InputStream> allegatiMap;
            if(allegati!=null) {
                allegatiMap = allegati.stream()
                        .collect(Collectors.toMap(MultipartFile::getOriginalFilename, allegato -> {
                            try {
                                return allegato.getInputStream();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }));
            } else{
                allegatiMap = new HashMap<>();
            }
            entityDataRepository.updateEntityIndividual(dominio, entityType, entity, allegatiMap, user.getMystandardUsername());

            LOGGER.debug("MyStandard - Entità aggiornata correttamente");

            //Si richiama il metodo per vedere il dettaglio readonly
            Object detailUpdatedReadonly = getDetailUpdatedReadonly(dominio, entityType, entity, user);

            //Esecuzione indicizzazione entità aggiornata
            indicizzazioneEntita(null, entityType, entity, false);

            LOGGER.debug("MyStandard - Indicizzazione Entità aggiornata avvenuta correttamente");

            return detailUpdatedReadonly;
        } else {
            throw new MyStandardException("Impossibile eseguire l'operazione richiesta. Verificare i dati utente.");
        }

    }

    /**
     * Si cerca l'idEntita dall'oggetto MyStandardEntity
     * @param entity, oggetto contenete i dati dell'entità
     * @param entityOwlPrefix, owl prefix per id entita
     * @param propertyName, nome della proprietà da ritornare
     * @return idEntita
     */
    private String getDataFromDataProperty(MyStandardEntity entity, String entityOwlPrefix, String propertyName) throws MyStandardException{
        if (entity != null) {
            Map<String, Object> dataProperty = entity.getDataProperty();
            if (dataProperty.containsKey( entityOwlPrefix + propertyName)) {
                return (String) dataProperty.get( entityOwlPrefix + propertyName);
            } else {
                throw new MyStandardException("IdEntita non presente nell'oggetto che vcontiene i dati dell'entità");
            }

        } else {
            throw new MyStandardException("Oggetto contenente i dati dell'entità è nullo");
        }
    }

    /**
     * Indicizzazione entità
     * @param domain, tdomainda indicizzare
     * @param entityType, tipo entita da indicizzare
     * @param entity, dati entità
     */
    private void indicizzazioneEntita(String domain, String entityType, MyStandardEntity entity, Boolean isNew) {
        try {

            LOGGER.debug("Indicizzazione entità");

            String owlPrefix = getEntityOwlPrefix(entityType);

            Object codiceEntitaObject = entity.getDataProperty().get(owlPrefix + MyStandardConstants.CODICE_ENTITA_COLUMN_KEY);
            Object versioneObject = entity.getDataProperty()
            .get(owlPrefix + MyStandardConstants.VERSIONE_COLUMN_KEY);

            String codice = codiceEntitaObject != null ? codiceEntitaObject.toString() : "";
            int versione = versioneObject != null ? Integer.parseInt(versioneObject.toString()) : 1;

            if (isNew) {
                entitySearchService.indexNewEntity(codice, versione, entityType, domain, entity);
                LOGGER.debug("Nuova entità indicizzato con successo.");
            } else {
                entitySearchService.updateExistingEntity(codice, versione, entityType, entity);
                LOGGER.debug("Entità indicizzato con successo.");
            }

        } catch (Exception e) {
            LOGGER.error("Errore nell'indicizzazione dell'entità.", e);
        }
    }

    /**
     * Get prefix dell'entità dal model
     * @param entityType, tipo entita
     * @return prefix entita
     * @throws MyStandardException se non esiste nel model l'entità
     */
    private String getEntityOwlPrefix(String entityType) throws MyStandardException {

        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        OClass oClass = oModelClasses.entrySet().stream()
                .filter(entry -> entityType.equals(entry.getValue().getLocalName()))
                .findFirst().get().getValue();

        if (oClass != null) {//
            return oClass.getPrefix();
        } else {
            throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
        }
    }


    @Override
    @Transactional
    public void deleteEntity(String dominio, String entityType, String codice, Integer versione, UserWithAdditionalInfo user, MyStandardRequest myStandardRequest) throws MyStandardException, IOException {

        LOGGER.debug("MyStandard - Eliminazione entità");

            String idEntita = codice + "_" + versione;
            List<JSONObject> operations = (List<JSONObject>) findAllOperations(entityType, codice, versione, user);


            if (checkOperationWithDomainAndRole(dominio, entityType, user) && //Si verifica che si possa operare sul dominio
                    checkOperationAllowed(MyStandardEntityOperationEnum.ELIMINA.getOperation(), operations) && //Si verifica che l'eliminazione sia un'operazione ammessa
                    checkEntitaStrutturataDefinitaDaEnteOperatore(idEntita, user)) {//Si verifica che l'entità sia stata definita dal mio ente

                String note = myStandardRequest != null ? myStandardRequest.getNote() : null;
                entityDataRepository.deleteEntityIndividual(entityType, codice, versione, user.getMystandardUsername(), note);

                try {
                    entitySearchService.removeEntityFromIndex(codice, versione, entityType);
                    LOGGER.debug("MyStandard --> Ente de-indicizzato con successo.");

                } catch (Exception e) {
                    LOGGER.error("MyStandard --> Errore nella de-indicizzazione dell'Ente.", e);
                }
            } else {
                throw new MyStandardException("L'utente non può procedere con l'eliminazione dell'entità");
            }

        LOGGER.debug("MyStandard - Eliminazione entità avvenuta correttamente");
    }

    /**
     * Si controlla se l'operazione in input è tra quelle disponibili
     * @param operation, operazione in input
     * @param operations, operazioni disponibili
     * @return true se l'operazione in input è disponibile
     */
    private boolean checkOperationAllowed(String operation, List<JSONObject> operations) {
        Optional<JSONObject> optionalOperation = operations.stream().filter(element -> operation.equalsIgnoreCase(element.getString(CustomFSMReader.__ID_TAG))).findFirst();
        return optionalOperation.isPresent();
    }

    /**
     * Si controlla se l'operazione in input è tra quelle disponibili
     * @param operation, operazione in input
     * @param operations, operazioni disponibili
     * @return operazione disponibile
     */
    private JSONObject getOperationAllowed(String operation, List<JSONObject> operations) {
        Optional<JSONObject> optionalOperation = operations.stream().filter(element -> operation.equalsIgnoreCase(element.getString(CustomFSMReader.__ID_TAG))).findFirst();
        if (optionalOperation.isPresent()) {
            return optionalOperation.get();
        } else return null;
    }

    @Override
    public Object findRelazioniEntitaByCodiceAndVersione(String entita, String codice, Integer versione, MyStandardFilter filter) throws MyStandardException {

        LOGGER.debug("MyStandard - Si estraggono le relazioni associabili.");

        List<JSONObject> allRelationsFiltered = entityDataRepository.findRelazioniByEntitaCodiceVersione(entita, codice, versione, filter);

        MyStandardUtil.setPaginationFiltersAsNull(filter);
        Integer totalRecords = entityDataRepository.countAllRelazioniByEntitaCodiceVersione(entita, codice, versione, filter);//Numero totale, la lista jsonObject torna solo gli elementi paginati

        LOGGER.debug("MyStandard - Estratte {} relazioni", totalRecords);

        return formDataService.getPaginatedDatatable(allRelationsFiltered, totalRecords);

    }

    @Override
    public JSONObject findDatiVocabolario(String type) throws MyStandardException {

        LOGGER.debug("Mystandard -> Inizio metodo per ottenere i dati dal vocabolario ");

        MyStandardDetailProperties.MyStandardDetailField configElement = myStandardDetailProperties.getFields().stream()
                .filter(element -> type.equals(element.getKey()))
                .findAny()
                .orElse(null);

        if (configElement != null && configElement.getVocabulary() != null && configElement.getVocabulary().getPath() != null) {

            return entityDataRepository.findDatiDaVocabolario(configElement.getVocabulary().getPath(), configElement.getVocabulary().getCodeIRI(), configElement.getVocabulary().getDescIRI(),  configElement.getVocabulary().getObjPropIRI());

        } else {
            throw new MyStandardException("Nessun riferimento al vocabolario per type " + type);
        }



    }

    @Override
    public JSONObject findMaxVersioneByCodice(String entita, String codice) throws MyStandardException {
        return entityDataRepository.findMaxVersioneByCodice(entita, codice);
    }

    @Override
    public JSONObject getMenuInfo() throws MyStandardException {

        LOGGER.debug("MyStandard - Estrazione Menu");
        JSONObject menu = new JSONObject();
        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        if (oModelClasses == null || oModelClasses.size() == 0) {
            throw new MyStandardException("Impossibile tornare le info sul menu poichè nel model non c'è riferimento ad alcuna classe");
        } else {

            //Si cicla per le classi, raggruppando per valori di annotation properties nel file conf menuUri
            Map<String, List<OClass>> classesGroupedByDomain = getClassesGroupedByDomain(oModelClasses);

            LOGGER.debug("MyStandard - Estratti {} domini per il menu del catalogo", classesGroupedByDomain.size());

            JSONArray items = getMenuItems(classesGroupedByDomain);

            LOGGER.debug("MyStandard - Estratti {} elementi per il menu del catalogo", items.length());

            addStaticMenuItems(items);

            menu.put(MyStandardConstants.ITEMS_FIELD_KEY, items);

            LOGGER.debug("MyStandard - Estratti {} elementi totali per il menu", items.length());

            return menu;
        }


    }

    /**
     * Si aggiungono menu item all'array esistente
     * @param items array di menu items
     */
    private void addStaticMenuItems(JSONArray items) {
        List<MyStandardProperties.MyStandardStaticMenu> staticMenu = myStandardProperties.getStaticMenu();//get menu statici
        Map<String, List<MyStandardProperties.MyStandardStaticMenu>> staticMenuGrouped =
                staticMenu.stream().collect(Collectors.groupingBy(w -> w.getMain()));//Si raggruppano per la proprietà main

        for (Entry<String, List<MyStandardProperties.MyStandardStaticMenu>> mainMenu: staticMenuGrouped.entrySet()) {
            JSONObject mainMenuObject = new JSONObject();//Main Item che raggruppa sottomenu
            mainMenuObject.put(MyStandardConstants.LABEL_KEY, messageSource.getMessage(mainMenu.getKey(), null, mainMenu.getKey(), null));

            JSONArray mainItems = new JSONArray();
            for (MyStandardProperties.MyStandardStaticMenu staticItem: mainMenu.getValue()) {//sottomenu

                //Se voce di menu deve essere mostrata solo per utenti autenticati
                //se false si mostra sempre. Se true si mostra solo se utente autenticato
                Boolean visibleOnlyAuthenticated = staticItem.getVisibleOnlyAuthenticated();
                if (!visibleOnlyAuthenticated || MyStandardUtil.isUserAuthenticated()) {
                    JSONObject item = new JSONObject();
                    item.put(MyStandardConstants.LABEL_KEY, messageSource.getMessage(staticItem.getLabel(), null, staticItem.getLabel(), null));
                    item.put(MyStandardConstants.URL_KEY, staticItem.getUrl());
                    mainItems.put(item);
                }
            }

            if (!mainItems.isEmpty()) {
                mainMenuObject.put(MyStandardConstants.ITEMS_FIELD_KEY, mainItems);
                items.put(mainMenuObject);
            }


        }
    }

    @Override
    public JSONObject getDynamicEntitesByDomain() throws MyStandardException {
        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        if (oModelClasses == null || oModelClasses.size() == 0) {
            throw new MyStandardException("Impossibile tornare le info sul menu poichè nel model non c'è riferimento ad alcuna classe");
        } else {

            //Si cicla per le classi, raggruppando per valori di annotation properties nel file conf menuUri
            Map<String, List<OClass>> classesGroupedByDomain = getClassesGroupedByDomain(oModelClasses);

            JSONObject items = new JSONObject();
            items.put("items", classesGroupedByDomain);

            return items;
        }

    }

    @Override
    public String isEntityDefinedByAnotherEnte(String idEntita) throws MyStandardException {
        return entityDataRepository.findIpaCodeDefinitoDaIdEntita(idEntita);
    }

    /**
     * Si raggruppano le classi per dominio (annotation properties business definita in file conf)
     * @param oModelClasses, classi da raggruppare
     * @return classi raggruppate
     */
    private Map<String, List<OClass>> getClassesGroupedByDomain(Map<String, OClass> oModelClasses) {
        Map<String, List<OClass>> classesGroupedByDomain = new TreeMap<>();
        for (Entry<String, OClass> entry : oModelClasses.entrySet()) {
            Map<String, List<OIdentificable>> annotationProperties = entry.getValue().getAnnotationProperties();
            if (annotationProperties.size() > 0 && annotationProperties.containsKey(myStandardProperties.getOwl().getMenuUri())) {
                List<OIdentificable> oIdentificables = annotationProperties.get(myStandardProperties.getOwl().getMenuUri());
                for (OIdentificable identificable: oIdentificables) {
                    if (classesGroupedByDomain.containsKey(identificable.getIRI())) {
                        List<OClass> oClasses = classesGroupedByDomain.get(identificable.getIRI());
                        oClasses.add(entry.getValue());
                    } else {
                        classesGroupedByDomain.put(identificable.getIRI(), Stream.of(entry.getValue())
                                .collect(Collectors.toList()));
                    }
                }
            }
        }
        return classesGroupedByDomain;
    }

    /**
     * Creazione items voci di menu
     * @param classesGroupedByDomain, raggruppamenti per annotation business
     * @return menu
     */
    private JSONArray getMenuItems(Map<String, List<OClass>> classesGroupedByDomain) {

        JSONArray items = new JSONArray();
        for (Entry<String, List<OClass>> entry : classesGroupedByDomain.entrySet()) {

            String dominio = entry.getKey();
            Boolean dominioPrincipale = myStandardProperties.getOwl().getMainDomain().equalsIgnoreCase(dominio);


            JSONObject item = new JSONObject();//Macromenu

            item.put(MyStandardConstants.LABEL_KEY, messageSource.getMessage(dominio, null, dominio, null));
            item.put(MyStandardConstants.MENU_PRINCIPALE, dominioPrincipale);
            item.put(MyStandardConstants.DOMAIN_KEY, dominio);

            //Creazione array con voci di sottomenu
            JSONArray innerItems = new JSONArray();
            entry.getValue().stream().forEach(oClass -> innerItems.put(
                    new JSONObject().put(MyStandardConstants.LABEL_KEY, messageSource.getMessage(oClass.getLocalName(), null, oClass.getLocalName(), null))
                            .put(MyStandardConstants.VALUE_KEY, oClass.getLocalName())
                            .put(MyStandardConstants.URL_KEY, ENTITIES + entry.getKey() + "/" + oClass.getLocalName())
                            .put(MyStandardConstants.ENTITY_IRI, oClass.getIRI())
                            .put(MyStandardConstants.SHOW_IPA_FILTER, oClass.getShowIpaFilter())));

            item.put(MyStandardConstants.ITEMS_FIELD_KEY, innerItems);
            items.put(item);
        }
        return items;
    }


    @Override
    public Object findAllOperations(String entityType, String codice, Integer versione, UserWithAdditionalInfo user) throws MyStandardException, IOException {

        LOGGER.debug("MyStandard - Si estraggono le operazioni possibili per entità ed utente");

        List<JSONObject> operations = new ArrayList<>();

        try {
            //Si ottengono i dati dell'utente
            ProfileUser profileUser = getProfileUserAuthenticated(user, false);

            if (profileUser != null) {
                //Si ottengono stato e ente che ha definito l'entità. E' una lista solo perchè il dominio business può essere multiplo
                List<JSONObject> entityData = entityDataRepository.getStatoAndIpaSingleEntityData(entityType, codice, versione);
                if (entityData != null && entityData.size() > 0) {

                    String entityStato = entityData.get(0).getString(MyStandardConstants.STATO_COLUMN_KEY);
                    String entityDefinitaDa = entityData.get(0).getString(MyStandardConstants.DEFINITA_DA_COLUMN_KEY);
                    Boolean isEntitaSpecializzazione = StringUtils.hasText(entityData.get(0).getString(MyStandardConstants.SPECIALIZZAZIONE_COLUMN_KEY));
                    List<String> entityDomains = entityData.stream().map(entity -> entity.getString(MyStandardConstants.DOMINIO_BUSINESS_KEY).toLowerCase()).collect(Collectors.toList());
                    Boolean entitaNazionale = isEntitaNazionale(entityDefinitaDa);

                    if (validEntityWithProfile(entityType, profileUser.getRole(), profileUser.getIpa(), profileUser.getClassDomain(),
                            profileUser.getDomain() != null ? profileUser.getDomain().toLowerCase() : profileUser.getDomain(), entityDomains)) {
                        operations = getOperations(entityType, profileUser.getRole(), entityStato, entitaNazionale, profileUser.getIpa(), entityDefinitaDa, isEntitaSpecializzazione);
                    }
                } else {
                    throw new MyStandardException("Impossibile trovare lo stato dell'entità");
                }
            } else {
                LOGGER.debug("MyStandard - Nessuna operazione prevista in quanto l'utente non è autenticato.");
            }
        } catch (URISyntaxException e) {
            throw new MyStandardException("Errore nel recupero delle operazioni possibili sull'entità");
        }

        LOGGER.info("MyStandard - Estratte {} operazioni possibili", operations.size());

        return operations;
    }


    @Override
    public Object findAllStorico(String entityType, String codice, Integer versione) throws MyStandardException {
        return findAllByCodice(entityType, codice, versione);
    }

    /**
     * Si verifca se l'utente con il suo profilo può operare sull'entità
     * @param entityType, tipo entità
     * @param userRole, ruolo dell'utente
     * @param userIpa, ipa dell'utente
     * @param classDomainList, classi a cui eventualmente è associato l'utente
     * @param userDomain, dominio dell'utente
     * @param entityDomains, domini dell'entità
     * @return true se l'utente può operare sull'entità
     */
    private boolean validEntityWithProfile(String entityType, String userRole, String userIpa, List<String> classDomainList,
                                           String userDomain, List<String> entityDomains) {


        if (MyStandardRoleEnum.RESPONSABILE_DOMINIO.getRole().equalsIgnoreCase(userRole) &&
                !classDomainList.contains(entityType)) {//Se responsabile di dominio su classi, devo verificare che l'entità sia tra le classi previste
            LOGGER.debug("Entita {} non presente tra le classi del Responsabile di Dominio dell'utente per ipa {}", entityType, userIpa);
            return false;
        } else if (MyStandardRoleEnum.RESPONSABILE_DOMINIO.getRole().equalsIgnoreCase(userRole) &&
                !userDomain.equalsIgnoreCase(MyStandardConstants.DOMINIO_GENERALE) &&
                !entityDomains.contains(userDomain)) {//Se responsabile di dominio su classi, devo verificare che l'entità sia tra le classi previste
            LOGGER.debug("Entita {} non appartiene al dominio del Responsabile di Dominio dell'utente  per ipa {}", entityType, userIpa);
            return false;
        }

        return true;

    }


    /**
     * Si ritornano le operazioni previste su un'entità con stato definito da un utente con ruolo definito
     * @param entityType, tipo entità da cui ricavare la state machine
     * @param userRole, ruolo dell'utente
     * @param entityStato, stato dell'entità
     * @param entitaNazionale, se ipa dell'utente è un ente nazionale
     * @param ipa, ipa dell'utente
     * @param entityDefinitaDa, ente che definisce l'entità (opzionale)
     * @return lista operazioni
     * @throws MyStandardException
     */
    private List<JSONObject> getOperations(String entityType, String userRole, String entityStato, Boolean entitaNazionale,
                                           String ipa, String entityDefinitaDa, Boolean isEntitaSpecializzazione) throws MyStandardException {


        List<JSONObject> operations = new ArrayList<>();

        //StateMachine della classe
        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        Optional<Entry<String, OClass>> optionalClass = oModelClasses.entrySet().stream()
                .filter(entry -> entityType.equals(entry.getValue().getLocalName()))
                .findFirst();

        if (optionalClass.isPresent() && optionalClass.get().getValue() != null) {
            OClass oClass = optionalClass.get().getValue();//Classe nel model per entity type

            List<String> stateMachine = oClass.getStateMachine();
            if (stateMachine != null) {
                List<JSONObject> oClassStateMachine = stateMachine.stream().map(element -> new JSONObject(element)).collect(Collectors.toList());//State machine


                //Si cerca se c'è una state machine per lo stato dell'entità
                Optional<JSONObject> optionalState = oClassStateMachine.stream()
                        .filter(element -> element.toMap().entrySet().stream()
                                .filter((elem -> elem.getKey().equalsIgnoreCase(entityStato))).findFirst().isPresent())
                        .findFirst();

                if (optionalState.isPresent()) {
                    JSONArray stateData = optionalState.get().getJSONArray(entityStato);//Si recuperano le informazioni
                    for (int i = 0, size = stateData.length(); i < size; i++) {
                        try {
                            JSONObject data = stateData.getJSONObject(i);
                            if (isValidEntityWithStateMachine(entityType, userRole, entityStato, entitaNazionale, data, ipa, entityDefinitaDa, isEntitaSpecializzazione)) {
                                addAllowedOperation(operations, data, entityStato);
                            }

                        } catch (JSONException e) {
                            LOGGER.error("MyStandard - Object in JSONArray finding is not a JSONObject as expected");
                        }
                    }

                } //else stato con nessuna operazione possibile
            } else {
                throw new MyStandardException("Nessun flusso di stati configurato per entita " + entityType);
            }
        } else {
            throw new MyStandardException("Entity " + entityType + " non presente nel file di configurazione.");

        }

        return operations;
    }

    private Boolean isValidEntityWithStateMachine(String entityType, String userRole, String entityStato, Boolean entitaNazionale,
                                                  JSONObject data, String ipa, String entityDefinitaDa, Boolean isEntitaSpecializzazione) throws MyStandardException {

        Boolean validOperation = false;
        Boolean validOperationProprioEnte = true;
        Boolean validOperationEntitaNazionale = true;
        Boolean validOperationEntitaStrutturata =true;
        Boolean validOperationEntitaSpecializzazione =true;
        if (data.has(CustomFSMReader.__ROLE_TAG)) {//Si recupera l'info del ruolo
            List<String> roles = Arrays.asList(data.getString(CustomFSMReader.__ROLE_TAG).split(","));
            if (roles.contains(userRole)) {//Se il ruolo dell'utente è definito.



                String opeLocaleProprioEnteString = data.getString(CustomFSMReader.__OPERATORE_LOCALE_PROPRIO_ENTE_TAG);
                String stateEnteString = data.getString(CustomFSMReader.__ENTE_NAZIONALE_TAG);
                String stateEntitaStrutturataString = data.getString(CustomFSMReader.__ENTITA_STRUTTURATA_TAG);
                String specializzazioneString = data.getString(CustomFSMReader.__SPECIALIZZAZIONE_TAG);

                //Eventuale verifica se operatore locale deve agire solo sulle entità del proprio ente
                if (StringUtils.hasText(opeLocaleProprioEnteString)) {//Se nella state machine si richiede che uno stato sia solo per entità definita da ente locale / nazionale

                    Boolean opeLocaleProprioEnte = Boolean.parseBoolean(opeLocaleProprioEnteString);
                    if (opeLocaleProprioEnte) {
                        if ((MyStandardRoleEnum.OPERATORE_ENTE_LOCALE.getRole().equalsIgnoreCase(userRole))
                                 && (ipa.equalsIgnoreCase(entityDefinitaDa))) {
                            validOperationProprioEnte = true;
                        } else if (!MyStandardRoleEnum.OPERATORE_ENTE_LOCALE.getRole().equalsIgnoreCase(userRole)){
                            validOperationProprioEnte = true;//Per tutti gli altri ruoli diversi da operatore locale, il parametro non conta
                        } else {
                            //Else false perchè operatore locale non può operare su entità non definite da lui
                            validOperationProprioEnte = false;
                        }
                    }

                }

                //Eventuale verifica sull'ente nazionale
                if (StringUtils.hasText(stateEnteString)) {//Se nella state machine si richiede che uno stato sia solo per entità definita da ente locale / nazionale

                    Boolean stateEnte = Boolean.parseBoolean(stateEnteString);
                    if (stateEnte.equals(entitaNazionale)) {//Match tra tipologia ente che ha definito entità, e quello definito in state machina

                        validOperationEntitaNazionale = true;
                    } else {
                        validOperationEntitaNazionale = false;
                    }
                }

                if (StringUtils.hasText(stateEntitaStrutturataString)) {//Stato usabile solo per entità strutturata

                    Boolean stateEntitaStrutturata = Boolean.parseBoolean(stateEntitaStrutturataString);
                    Boolean isEntititaStrutturata = isSubClassOfEntitaStrutturata(entityType);
                    if (stateEntitaStrutturata.equals(isEntititaStrutturata)) {
                        validOperationEntitaStrutturata = true;
                    } else {
                        validOperationEntitaStrutturata = false;
                        LOGGER.debug("Lo stato {} prevede entità strutturata {} ma l'entità è {}", entityStato, stateEntitaStrutturata, isEntititaStrutturata);
                    }

                }

                if (StringUtils.hasText(specializzazioneString)) {//Stato usabile solo se è o no una specializzazione

                    Boolean specializzazione = Boolean.parseBoolean(specializzazioneString);
                    if (specializzazione.equals(isEntitaSpecializzazione)) {
                        validOperationEntitaSpecializzazione = true;
                    } else {
                        validOperationEntitaSpecializzazione = false;
                        LOGGER.debug("Lo stato {} prevede che l'entita abbia specializzazione a {} ma l'entità è {}", entityStato, specializzazione, isEntitaSpecializzazione);
                    }

                }



                if (validOperationProprioEnte  && validOperationEntitaNazionale
                        && validOperationEntitaStrutturata && validOperationEntitaSpecializzazione) {
                    validOperation = true;
                }

            } else {
                LOGGER.debug("Per lo stato {} non sono previste attività per il ruolo {}", entityStato, userRole);
            }
        } else {
            throw new MyStandardException("Nella state machine non esiste il ruolo per lo stato " + entityStato);
        }

        return validOperation;
    }

    /**
     * Si ritornano le informazioni necessarie per le operazioni possibili
     * @param operations, lista operazioni
     * @param data, dati da inserire
     * @param entityStato, stato dell'entità
     */
    private void addAllowedOperation(List<JSONObject> operations, JSONObject data, String entityStato) {

        LOGGER.debug("MyStandard - Aggiunta operazione {} come possibile", data.getString(CustomFSMReader.__ID_TAG));
        JSONObject operation = new JSONObject();
        operation.put(CustomFSMReader.__ID_TAG, data.getString(CustomFSMReader.__ID_TAG));
        operation.put(CustomFSMReader.__NEXT_STATE_TAG, data.getString(CustomFSMReader.__NEXT_STATE_TAG));
        operation.put(MyStandardConstants.ENTITY_STATE, entityStato);
        operations.add(operation);
    }


    @Override
    public Object genericUpdateEntityState(String operazione, String dominio, String entityType, String codice, Integer versione, UserWithAdditionalInfo user, MyStandardRequest myStandardRequest) throws MyStandardException, IOException, URISyntaxException {

        List<JSONObject> operations = (List<JSONObject>) findAllOperations(entityType, codice, versione, user);
        JSONObject operationAllowed = getOperationAllowed(operazione, operations);

        if (operationAllowed != null){ //Si verifica che l'eliminazione sia un'operazione ammessa

            String note = myStandardRequest != null ? myStandardRequest.getNote() : null;
            String nextState = operationAllowed.getString(CustomFSMReader.__NEXT_STATE_TAG);
            String entityStato = operationAllowed.getString(MyStandardConstants.ENTITY_STATE);

            entityDataRepository.updateEntityState(operazione, entityType, codice, versione, entityStato, nextState, user.getMystandardUsername(), note);

            LOGGER.debug("MyStandard - Cambio stato entità avvenuto correttamente");
            //Attualmente non vengono indicizzate le object property


        } else {
            throw new MyStandardException("L'utente non può procedere con l'operazione di " + MyStandardEntityOperationEnum.of(operazione).getDescription() + " dell'entità");
        }

        return findByCodiceAndVersione(dominio, entityType, codice, versione, true, user, false);
    }





    /**
     * Si ottengono le info della macchina a stato per tipo entità filtrata per stato entità
     * @param entityType, tipo entità
     * @param entityStato, stato entità
     * @return macchina a astati filtrata
     * @throws MyStandardException
     */
    private JSONObject getEntityStateMachineFilteredStato(String entityType, String entityStato) throws MyStandardException {
        //get state machine
        //StateMachine della classe
        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        Optional<Entry<String, OClass>> optionalClass = oModelClasses.entrySet().stream()
                .filter(entry -> entityType.equals(entry.getValue().getLocalName()))
                .findFirst();

        if (optionalClass.isPresent() && optionalClass.get().getValue() != null) {
            OClass oClass = optionalClass.get().getValue();//Classe nel model per entity type

            List<JSONObject> oClassStateMachine = oClass.getStateMachine().stream().map(element -> new JSONObject(element)).collect(Collectors.toList());//State machine


            //Si cerca se c'è una state machine per lo stato dell'entità
            Optional<JSONObject> optionalState = oClassStateMachine.stream()
                    .filter(element -> element.toMap().entrySet().stream()
                            .filter((elem -> elem.getKey().equals(entityStato))).findFirst().isPresent())
                    .findFirst();

            if (optionalState.isPresent()) {
                return optionalState.get();
            } else {
                throw new MyStandardException("Nella state machine non esiste lo stato " + entityStato);
            }
        } else {
            throw new MyStandardException("Entity " + entityType + " non presente nel file di configurazione.");
        }
    }

    /**
     * Si ottiene il ProfileUser di un utente autenticato
     * @param user, utente che sta eseguendo la richiesta
     * @param errorAuthentication, indica se lanciare un'eccezione in caso di errore, o se tornare null
     * @return profileUser
     * @throws MyStandardException
     */
    private ProfileUser getProfileUserAuthenticated(UserWithAdditionalInfo user, Boolean errorAuthentication) throws MyStandardException {
        if (user != null) {

            String userIpa = user.getIpa();
            //La lista contiene il ruolo e le classi per cui operare. Dovrebbe sempre contenere un elemento solo
            Optional<ProfileUser> optionalProfileUser = user.getUserProfiles().stream()
                        .filter(profUser -> userIpa.equalsIgnoreCase(profUser.getIpa()))
                        .findFirst();

            if (optionalProfileUser.isPresent()) {
                return optionalProfileUser.get();
            } else {
                throw new MyStandardException("Utente non possiede un profilo per ipa " + userIpa);
            }

        } else {
            if (errorAuthentication) throw new MyStandardException("Nessun utente autenticato.");
            else return null;
        }
    }

    @Override
    public Object findAllBacheca(MyStandardFilter filter, UserWithAdditionalInfo user, String operazione) throws MyStandardException {

        if (user == null) {
            throw new MyStandardException("Un utente non autenticato non ha accesso alla bacheca");

        } else {

            //Si ottiene la stateMachine di default
            List<JSONObject> stateMachine = stateMachineService.calculateStateMachine(myStandardProperties.getStateMachine())
                    .stream().map(element -> new JSONObject(element)).collect(Collectors.toList());

            //Si ottengono gli stati e le condizioni definite nella stateMachine a seconda del ruolo dell'utente, e dell'eventuale operazione richiesta
            List<JSONObject> statesByUserRoleAndOperation = getStatesByUserRoleAndOperation(user, stateMachine, operazione);

            //Si aggiungono nel filtro le informazioni ricavate dalla state machine
            createFilterByStateMachine(filter, statesByUserRoleAndOperation, user);

            //Esecuzione query
            List<JSONObject> allDataFiltered = entityDataRepository.findAllBacheca(filter);

            MyStandardUtil.setPaginationFiltersAsNull(filter);
            Integer totalRecords = entityDataRepository.countAllBacheca(filter);//Numero totale, la lista jsonObject torna solo gli elementi paginati

            LOGGER.info("MyStandard - Estratti {} elementi da mostrare in bacheca", totalRecords);

            return formDataService.getPaginatedDatatable(allDataFiltered, totalRecords);
        }
    }

    /**
     * Si aggiungono nel filtro le informazioni ricavate dalla state machine
     * @param filter, filtro a cui aggiungere le informazioni
     * @param statesByUserRoleAndOperation, stati e le condizioni definite nella stateMachine a seconda del ruolo dell'utente, e dell'eventuale operazione richiesta
     * @param user, utente autenticato
     * @throws MyStandardException in caso di errore nell'ottenimento del profilo utente
     */
    private void createFilterByStateMachine(MyStandardFilter filter, List<JSONObject> statesByUserRoleAndOperation, UserWithAdditionalInfo user) throws MyStandardException {

        Optional<ProfileUser> optionalProfileUser = user.getUserProfiles().stream().filter(profile -> profile.getIpa().equalsIgnoreCase(user.getIpa())).findFirst();

        if (!optionalProfileUser.isPresent()) {
            throw new MyStandardException("Errore nell'ottenimento del profileUser dell'utente con ipa " + user.getIpa());
        } else {

            for (JSONObject stateObject : statesByUserRoleAndOperation) {
                String stateId = stateObject.getString(CustomFSMReader.__ID_TAG);//Id stato
                String opeLocaleProprioEnteString = stateObject.getString(CustomFSMReader.__OPERATORE_LOCALE_PROPRIO_ENTE_TAG);//Informazione se operatore locale può operare sullo stato solo su entità definite dal proprio ente
                String enteNazionaleString = stateObject.getString(CustomFSMReader.__ENTE_NAZIONALE_TAG);//Informazione se utente può operare su entità definite dall'ente nazionale o no (qualsiasi ente locale)
                String userRole = optionalProfileUser.get().getRole();//Ruolo utente
                String specializzazioneFinalStateString = stateObject.getString(CustomFSMReader.__SPECIALIZZAZIONE_FINAL_STATE_TAG);//Se in caso di entità di specializzazione, allora lo stao è finale

                //Si aggiunge al filtro lo stato se non esiste già
                if (!filter.getStateList().contains(stateId)) {
                    filter.getStateList().add(stateId);
                }

                //Se presente la condizioni per cui un operatore locale  può operare sullo stato solo su entità definite dal proprio ente
                //Si aggiunge nel filtro anche l'ente di appartenenza
                if (StringUtils.hasText(opeLocaleProprioEnteString) && userRole.equalsIgnoreCase(MyStandardRoleEnum.OPERATORE_ENTE_LOCALE.getRole())) {
                    Boolean opeLocaleProprioEnte = Boolean.parseBoolean(opeLocaleProprioEnteString);
                    filter.setOpeLocaleEnte(opeLocaleProprioEnte);
                    filter.setUserIpa(user.getIpa());
                }

                //Si Informazione se utente può operare su entità definite dall'ente nazionale o no (qualsiasi ente locale)
                if (StringUtils.hasText(enteNazionaleString)) {
                    filter.setEnteNazionale(enteNazionaleString);
                }

                if (StringUtils.hasText(specializzazioneFinalStateString)) {
                    filter.setSpecializzazioneFinalState(specializzazioneFinalStateString);
                }


                //Responsabile di dominio ha nel profilo dominio e classi
                if (userRole.equalsIgnoreCase(MyStandardRoleEnum.RESPONSABILE_DOMINIO.getRole())) {
                    String userDomain = optionalProfileUser.get().getDomain();
                    List<String> userClassesDomain = optionalProfileUser.get().getClassDomain();

                    filter.setDomain(userDomain);
                    filter.setClassDomain(userClassesDomain);
                    filter.setResponsabileDominio(true);
                }

            }
        }

    }

    /**
     * Si ottengono gli stati e le condizioni definite nella stateMachine a seconda del ruolo dell'utente, e dell'eventuale operazione richiesta
     * @param user, utente autenticato
     * @param stateMachine, informazione sugli stati
     * @param operazione, eventuale operazione richiesta
     * @return stati e condizioni a seconda del ruolo utente
     */
    private List<JSONObject> getStatesByUserRoleAndOperation(UserWithAdditionalInfo user, List<JSONObject> stateMachine, String operazione) {

        List<JSONObject> statesByUserRoleAndOperation = new ArrayList<>();

        for (JSONObject stateMachineObject: stateMachine) {//Iterazione su tutti gli stati definiti
            Map<String, Object> stateMachineMap = stateMachineObject.toMap();//Mappa del singolo state

            for (Entry<String, Object> state: stateMachineMap.entrySet()) {
                List<Map<String, String>> messageList = (List<Map<String, String>>) state.getValue();
                for (Map<String, String> message: messageList) {//Si itera su tutti i message

                    //Si verifica se lo stato è finale (in bacheca vanno solo gli stati non finali)
                    String finalStateString = message.get(CustomFSMReader.__FINAL_STATE_TAG);
                    String operazioneID = message.get(CustomFSMReader.__ID_TAG);

                    if (!StringUtils.hasText(operazione) || operazione.equalsIgnoreCase(operazioneID)) {

                          if (StringUtils.hasText(finalStateString)) {
                            Boolean finalState = Boolean.parseBoolean(finalStateString);
                            if (!finalState) {//Stato non finale

                                //Si verifica se ruolo utente è presente tra i ruoli del message
                                String roles = message.get(CustomFSMReader.__ROLE_TAG);
                                Optional<ProfileUser> optionalProfileUser = user.getUserProfiles().stream().filter(profile -> profile.getIpa().equalsIgnoreCase(user.getIpa())).findFirst();

                                if (StringUtils.hasText(roles) && optionalProfileUser.isPresent()) {

                                    List<String> rolesList = Arrays.asList(roles.split(","));
                                    String userRole = optionalProfileUser.get().getRole();

                                    if (rolesList.contains(userRole)) {//Utente può eseguire l'operazione

                                        addStateForBacheca(statesByUserRoleAndOperation, state, message);

                                    } else {
                                        LOGGER.debug("Skip operazione {} in quanto l'utente non ha il ruolo per eseguirla", operazioneID);
                                    }

                                } else {
                                    LOGGER.debug("Skip operazione {} in quanto non sono stati definiti dei ruoli per l'operazione", operazioneID);
                                }
                            } else {
                                LOGGER.debug("Skip stato {} in quanto è uno stato finale non utilizzabile per la bacheca.", state.getKey());
                            }


                        } else {
                            LOGGER.debug("Nessuna informazione se lo stato {} è uno stato finale oppure no.", state.getKey());
                        }


                    } else {
                        LOGGER.debug("Skip operazione {} in quanto l'utente non ha il ruolo per eseguirla", operazioneID);
                    }




                }

            }
        }
        return statesByUserRoleAndOperation;
    }


    /**
     * Si aggiunge stato e condizioni se non è già presente
     * @param statesByUserRoleAndOperation, lista con stati e condizioni da tornare
     * @param state, oggetto state
     * @param message, oggetto message
     */
    private void addStateForBacheca(List<JSONObject> statesByUserRoleAndOperation, Entry<String, Object> state, Map<String, String> message) {
        boolean isStateAlreadyPresent = statesByUserRoleAndOperation.stream()
                .anyMatch(element ->
                        element.getString(CustomFSMReader.__ID_TAG).equalsIgnoreCase(state.getKey()) &&
                                element.getString(CustomFSMReader.__OPERATORE_LOCALE_PROPRIO_ENTE_TAG).equalsIgnoreCase(message.get(CustomFSMReader.__OPERATORE_LOCALE_PROPRIO_ENTE_TAG)));

        if (!isStateAlreadyPresent) {
            JSONObject obj = new JSONObject();
            obj.put(CustomFSMReader.__ID_TAG, state.getKey());
            obj.put(CustomFSMReader.__OPERATORE_LOCALE_PROPRIO_ENTE_TAG, message.get(CustomFSMReader.__OPERATORE_LOCALE_PROPRIO_ENTE_TAG));
            obj.put(CustomFSMReader.__ENTE_NAZIONALE_TAG, message.get(CustomFSMReader.__ENTE_NAZIONALE_TAG));
            obj.put(CustomFSMReader.__SPECIALIZZAZIONE_FINAL_STATE_TAG, message.get(CustomFSMReader.__SPECIALIZZAZIONE_FINAL_STATE_TAG));

            statesByUserRoleAndOperation.add(obj);

        }
    }
}



