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

import com.github.owlcs.ontapi.jena.model.OntDataRange;
import com.github.owlcs.ontapi.jena.vocabulary.XSD;
import it.regioneveneto.myp3.mystd.bean.enumeration.DataTypeEnum;
import it.regioneveneto.myp3.mystd.bean.owl.*;
import it.regioneveneto.myp3.mystd.config.MyStandardConfig;
import it.regioneveneto.myp3.mystd.config.OwlJenaConfig;
import it.regioneveneto.myp3.mystd.config.OwlOntApiConfig;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardDetailProperties;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.service.EntityStructureService;
import it.regioneveneto.myp3.mystd.service.StateMachineService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import it.regioneveneto.myp3.mystd.utils.MyStandardUtil;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OwlStructureServiceImpl implements EntityStructureService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OwlStructureServiceImpl.class);

    @Autowired
    private MyStandardProperties myStandardProperties;

    @Autowired
    private MyStandardDetailProperties myStandardDetailProperties;


    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MyStandardConfig myStandardConfig;

    @Autowired
    private OwlJenaConfig owlJenaConfig;

    @Autowired
    private OwlOntApiConfig owlOntApiConfig;


    @Autowired
    private StateMachineService stateMachineService;


    @Override
    public void getMenuStructureModel() {

    }

    /**
     * Si aggiungono le info dell'entityType se non presenti (fullParsed true o false)
     * @param entityType, entity di cui si richiedono le info
     * @return
     * @throws MyStandardException
     * @throws IOException
     */
    @Override
    public OModel getStructureModel(String entityType) throws MyStandardException {

        LOGGER.debug("MyStandard - Si ottiene il model di una entità di tipo {}", entityType);

        OModel oModel = myStandardConfig.getModel();

        MyStandardDetailProperties.MyStandardDetailField configEntityElement = myStandardDetailProperties.getContainer().stream()
                .filter(element -> entityType.equals(element.getKey()))
                .findAny()
                .orElse(null);

        if (configEntityElement != null) {

            String prefix = configEntityElement.getPrefix();
            String entityIRI = prefix + entityType;

            Map<String, OClass> oModelClasses = oModel.getClasses();
            if (oModelClasses.containsKey(entityIRI)) {
                OClass entityTypeClass = oModelClasses.get(entityIRI);
                if (!entityTypeClass.getFullParsed()) {//Classe non parserizzata
                    return parseEntityInModel(entityIRI, prefix);//Parsing classe
                } else {
                    return oModel;
                }
            } else {
                throw new MyStandardException("Entity " + entityType + " non prevista nel modello OWL.");
            }
        } else {
            throw new MyStandardException("Entity " + entityType + " non presente come container nel file di configurazione.");
        }
    }


    /**
     * Se parserizza owl per ottenere i dati della struttura
     * @param entityIRI, IRI di cui ottenere la struttura
     * @param prefix, usato per verificare annotation dell'entità
     * @return Modello con i dati della struttura
     * @throws MyStandardException
     * @throws IOException
     */
    private OModel parseEntityInModel(String entityIRI, String prefix) throws MyStandardException {

        LOGGER.info("Get entity structure for {}", entityIRI);

        OntModel base = owlJenaConfig.getOwlModel();
        com.github.owlcs.ontapi.jena.model.OntModel ontapiBase = owlOntApiConfig.getOntApiModel();

        OModel oModel = myStandardConfig.getModel();

        OntClass risorsaClass = base.getOntClass(entityIRI);//si prende la classe per vederne le proprietà
        if (risorsaClass == null) {
            throw new MyStandardException("No resource " + entityIRI + " present in OWL.");
        } else {
            //Si ottengono le info della classe e si mettono nel model
            OClass oClass = getClassInfo(base, ontapiBase, oModel, prefix, risorsaClass, entityIRI, entityIRI, null);
            mergeClassInModel(entityIRI, oModel, oClass);
        }
        return oModel;
    }

    /**
     * Eventualem merge del risultato del parsing di entityIRI in oClass
     * @param entityIRI, entità di cui fare parsing e ottenere info
     * @param oModel, struttura a cui aggiungere info nuovo parsing
     * @param oClass, classe su cui fare il merge
     */
    private void mergeClassInModel(String entityIRI, OModel oModel, OClass oClass) {
        Map<String, OClass> classes = oModel.getClasses();
        if (classes.containsKey(entityIRI)) {//Se esiste, si fa il merge delle classi
            OClass entityClass = classes.get(entityIRI);

            if (!entityClass.getFullParsed()) {//Non è ancora stata parserizzata, faccio merge
                Map<String, OProperty> entityClassDataProperty = entityClass.getDataProperty();
                Map<String, OProperty> entityClassObjectProperty = entityClass.getObjectProperty();
                List<String> entityClassRelations = entityClass.getRelations();

                entityClassDataProperty.putAll(oClass.getDataProperty());
                entityClassObjectProperty.putAll(oClass.getObjectProperty());
                entityClassRelations.addAll(oClass.getRelations());
                entityClass.setStateMachine(oClass.getStateMachine());

                entityClass.setFullParsed(true);
                classes.put(entityIRI, entityClass);
            } else {
                LOGGER.debug("Class " + entityIRI + " già parserizzata");
            }
        } else {
            classes.put(entityIRI, oClass);
        }
    }

    /**
     * Get class info
     * @param base
     * @param oModel
     * @param ns
     * @param risorsaClass
     * @param entityIRI
     * @param objectProperty
     * @return
     */
    private OClass getClassInfo(OntModel base, com.github.owlcs.ontapi.jena.model.OntModel ontapiBase, OModel oModel, String ns,
                                OntClass risorsaClass, String entityIRI, String originalEntityIRI, OntProperty objectProperty) throws MyStandardException {

        LOGGER.debug("MyStandard - Si ottiene le info della entità {}", entityIRI);

        OClass oClass = new OClass(entityIRI);
        List<String> superClassList = new ArrayList<>();
        for (Iterator it = risorsaClass.listSuperClasses(); it.hasNext(); ) {
            OntClass classItem = (OntClass)it.next();
            superClassList.add(classItem.getURI());
        }

        LOGGER.debug("MyStandard - Aggiunte le superclassi della entità {}", entityIRI);

        oClass.setParentClasses(superClassList);//Set parent classes

        for (Iterator it = risorsaClass.listDeclaredProperties(); it.hasNext(); ) {
            OntProperty p = (OntProperty) it.next();
            String propertyName = p.getLocalName();
            OntResource propertyDomain = p.getDomain();

            if (p.isObjectProperty()) {//Object property => Ripetibile o relazione o enumeration
                String uri = ns + MyStandardConstants.RELATION_TYPE;//Si cerca relation type per capire se ripetibile o relazione
                RDFNode relation_type = p.getPropertyValue(ResourceFactory.createProperty(uri));

                //Non si considera object property se relation type nullo
                if (relation_type != null) {

                    String relationType = relation_type.toString();

                    //Da aggiungere nei tab
                    if (relationType.equals(MyStandardConstants.RELATION_TYPE_FUNCTIONAL)) {
                        createObjectPropertyFunctionalRelation(oModel, oClass, p);

                    } else if (relationType.equals(MyStandardConstants.RELATION_TYPE_ENUMERATION) || relationType.equals(MyStandardConstants.RELATION_TYPE_MULTIPLE_ENUMERATION)){
                        createObjectPropertyEnumerationProp(oModel, oClass, base, ontapiBase, p, ns, originalEntityIRI, relationType);

                    } else if (relationType.equals(MyStandardConstants.RELATION_TYPE_ENTITY_PROPERTY)) {
                        createObjectPropertyEntityProp(oModel, oClass, base, ontapiBase, p, ns, originalEntityIRI);

                    } else {
                        LOGGER.error("MyStandard - Relation type {} non prevista: ", relationType);
                    }
                } /*else {
                    LOGGER.debug("MyStandard - Object Property {} da scartare in quanto relation_type è nullo", propertyName);
                }*/
            } else if (p.isDatatypeProperty() && propertyDomain != null) {
                createDataProperty(base, ontapiBase, oModel, oClass, p, ns, originalEntityIRI, objectProperty);

            } else {
                LOGGER.debug("MyStandard - Property {} non è object o data property", propertyName);
            }
        }

        oClass.setStateMachine(stateMachineService.calculateStateMachine(oClass.getStateMachineConfig()));

        return oClass;
    }



    private void createObjectPropertyEntityProp(OModel oModel, OClass oClass, OntModel base, com.github.owlcs.ontapi.jena.model.OntModel ontapiBase, OntProperty objectProperty, String namespace, String originalEntityIRI) throws MyStandardException {

        String propertyName = objectProperty.getLocalName();

        MyStandardDetailProperties.MyStandardDetailField fieldElement = myStandardDetailProperties.getFields().stream()
                .filter(element -> propertyName.equals(element.getKey()))
                .findAny()
                .orElse(null);

        if (fieldElement != null) {//La proprietà deve essere definita in white list

            LOGGER.info("MyStandard - Object objectProperty " + propertyName + " is entity objectProperty");
            OntResource propertyDomain = objectProperty.getDomain();

            if (propertyDomain == null) {
                LOGGER.error("MyStandard - Object Property " + propertyName + "is entity objectProperty with  null domain");
            } else {

                ExtendedIterator<? extends OntResource> listRange = objectProperty.listRange();
                while (listRange.hasNext()) {

                    OntResource range = listRange.next();//Dal range per ottenere info classe
                    if (range != null) {
                        OntClass rangeClass = base.getOntClass(range.getURI());
                        if (rangeClass != null) {

                            String objectPropertyIRI = objectProperty.getURI();
                            Map<String, OProperty> objectPropertyMap = oClass.getObjectProperty();

                            String label = messageSource.getMessage(propertyName, null, propertyName, null);
                            String type = MyStandardConstants.ENTITY_PROPERTY_TYPE;
                            Boolean hidden = fieldElement.getHidden();
                            Boolean isVisibleOnlyAuthenticated = fieldElement.getVisibleOnlyAuthenticated();
                            Integer order = fieldElement.getOrder();
                            String rangeClassIRI = rangeClass.getURI();
                            String domain = fieldElement.getContainer() != null ? fieldElement.getContainer() : propertyDomain.getURI();
                            String domainLocalName = fieldElement.getContainerLocalName() != null ? fieldElement.getContainerLocalName() : propertyDomain.getLocalName();

                            OProperty objectEntityProperty = new OProperty(objectPropertyIRI);
                            objectEntityProperty.setLabel(label);
                            objectEntityProperty.setType(type);
                            objectEntityProperty.setHidden(hidden);
                            objectEntityProperty.setVisibleOnlyAuthenticated(isVisibleOnlyAuthenticated);
                            objectEntityProperty.setOrder(order);
                            objectEntityProperty.setRangeClassIRI(rangeClassIRI);
                            objectEntityProperty.setRangeClassLocal(messageSource.getMessage(range.getLocalName(), null, range.getLocalName(), null));
                            objectEntityProperty.setDomainIRI(domain);
                            objectEntityProperty.setDomainLocalName(messageSource.getMessage(domainLocalName, null, domainLocalName, null));
                            objectEntityProperty.setLocalName(propertyName);

                            if (StringUtils.hasText(fieldElement.getVocabulary().getPath())) {
                                objectEntityProperty.setType(MyStandardConstants.VOCABULARY_TYPE);
                            } else {
                                objectEntityProperty.setType(MyStandardConstants.ENTITY_PROPERTY_TYPE);
                            }

                            OValidation validation = getOValidationObject(base, ontapiBase, objectProperty, namespace, type, propertyDomain.getURI(), objectProperty);

                            objectEntityProperty.setValidation(validation);


                            OClass rangeClassObject = getClassInfo(base, ontapiBase, oModel, namespace, rangeClass, rangeClassIRI, originalEntityIRI, objectProperty);
                            mergeClassInModel(rangeClassIRI, oModel, rangeClassObject);


                            objectPropertyMap.put(objectPropertyIRI, objectEntityProperty);


                        } else {
                            LOGGER.error("MyStandard - Property " + propertyName + "is entity objectProperty with  null range class");
                        }
                    } else {
                        LOGGER.error("MyStandard - Property " + propertyName + "is entity objectProperty with  null range");
                    }
                }
            }
        } /*else {
            LOGGER.debug("MyStandard - Object objectProperty {} non prevista", propertyName);
        }*/

        LOGGER.debug("MyStandard - Terminata elaborazione property");
    }

    /**
     * Si cerca un'eventuale regex dall'enumeration
     * @param type da ricercare in enum
     */
    private String getValidationRegexFromType(String type) {
        String regex = null;
        try {
            DataTypeEnum dataTypeEnum = DataTypeEnum.of(type);//Può rilanciare eccezione se non trova
            if (dataTypeEnum != null && dataTypeEnum.getRegex() != null) {
                regex = dataTypeEnum.getRegex();
            }
        } catch (MyStandardException e) {
            //Null non ha trovato niente
        }

        return regex;
    }


    /**
     * Si valorizza oggetto Ovalidation per visualizzazione condizionata
     * @param model, modello da cui cercare eventualmente altre proprietà
     * @param namespace, namespace su cui è presente annotazione
     * @param property, property su cui cercare annotazione
     * @param validation, oggetto validazione
     * @param objectPropertyIRI, iri object property
     * @return oggetto Ovalidation per visualizzazione condizionata
     * @throws MyStandardException
     */
    private OValidation getValidationConditionalShow(OntModel model, String namespace, OntProperty property, OValidation validation, OntProperty objectPropertyIRI) throws MyStandardException {

        String conditionalShow = getAnnotationValue(model, namespace + MyStandardConstants.CONDITIONAL_SHOW, property, false, objectPropertyIRI);
        if (StringUtils.hasText(conditionalShow)) {

            if (validation == null) validation = new OValidation();

            validation.setConditionalShow(Boolean.parseBoolean(conditionalShow));
            validation.setRequired(null);//Se c'è il conditionalShow, non c'è il required

            String conditionalWhen = getAnnotationValue(model, namespace + MyStandardConstants.CONDITIONAL_SHOW_WHEN, property, true, objectPropertyIRI);
            if (StringUtils.hasText(conditionalWhen)) {
                validation.setConditionalWhen(conditionalWhen);
            } else {
                throw new MyStandardException("Campo conditionalWhen nullo per la proprieta " + property.getURI() + " non permesso se esiste un campo conditionalShow valorizzato");
            }

            String conditionalValue = getAnnotationValue(model, namespace + MyStandardConstants.CONDITIONAL_SHOW_VALUE, property, false, objectPropertyIRI);
            if (conditionalValue != null && ("true".equalsIgnoreCase(conditionalValue) || "false".equalsIgnoreCase(conditionalValue)) ) {
                validation.setConditionalValue(Boolean.parseBoolean(conditionalValue));//Store as object
            } else {
                validation.setConditionalValue(conditionalValue);//Null value ammesso
            }
        }

        return validation;

    }

    /**
     * Si ottiene il valore di una annotazione
     * @param model, modello da cui cercare eventualmente altre proprietà
     * @param uri, uri annotazione
     * @param property, property su cui cercare annotazione
     * @param localName, true se cercare localname, false altrimenti
     * @param objectProperty, object property
     * @return valore annotazione
     * @throws MyStandardException
     */
    private String getAnnotationValue(OntModel model, String uri, OntProperty property, Boolean localName, OntProperty objectProperty) throws MyStandardException {
        String nodeValue = null;
        RDFNode node = property.getPropertyValue(ResourceFactory.createProperty(uri));
        if (node != null) {

            if (localName) {//Si indica se è una proprietà di cui ci serve localName e non URI

                String propertyValue = node.toString();
                OntProperty propertyFullName = model.getOntProperty(propertyValue);//Si cerca nel modello la proprietà per avere il localName
                if (propertyFullName != null) {

                    nodeValue = MyStandardUtil.mystandardPrefixForMorfeo((objectProperty != null ? (objectProperty.getLocalName()) : ""), propertyFullName.getLocalName()) ;
                } else {
                    throw new MyStandardException("Proprietà " + propertyValue + " referenziata ma non esiste.");
                }

            } else {
                nodeValue = node.toString();
            }
        }//Else si lascia il valore null
        return nodeValue;
    }


    /**
     * Si crea oggetto ovalidation per obbligatorietà che dipende da un altro campo
     * @param model, modello da cui cercare eventualmente altre proprietà
     * @param namespace, namespace su cui è presente annotazione
     * @param property, property su cui cercare annotazione
     * @param validation, oggetto validazione
     * @param objectProperty, object property che contiene data property
     * @return oggetto Ovalidation per visualizzazione condizionata
     * @throws MyStandardException
     */
    private OValidation getValidationConditionalRequired(OntModel model, String namespace, OntProperty property, OValidation validation, OntProperty objectProperty) throws MyStandardException {


        String conditionalRequired = getAnnotationValue(model, namespace + MyStandardConstants.CONDITIONAL_REQUIRED, property, true, objectProperty);
        if (StringUtils.hasText(conditionalRequired)) {

            if (validation == null) {
                validation = new OValidation();
            }

            validation.setConditionalRequired(conditionalRequired);
            validation.setRequired(null);//Se c'è il conditionalRequired, non c'è il required

            String conditionalRequiredOperator = getAnnotationValue(model, namespace + MyStandardConstants.CONDITIONAL_REQUIRED_OPERATOR, property, false, objectProperty);
            if (StringUtils.hasText(conditionalRequiredOperator)) {
                validation.setConditionalOperator(conditionalRequiredOperator);//Null value ammesso
            } else {
                throw new MyStandardException("Conditional Operator nullo per property " + property.getURI());
            }

            String conditionalRequiredValues = getAnnotationValue(model, namespace + MyStandardConstants.CONDITIONAL_REQUIRED_VALUES, property, false, objectProperty);
            if (StringUtils.hasText(conditionalRequiredValues)) {
                List<String> conditionalRequiredValuesList = Stream.of(conditionalRequiredValues.split(",", -1))
                        .collect(Collectors.toList());
                validation.setConditionalValues(conditionalRequiredValuesList);//Null value ammesso
            } else {
                throw new MyStandardException("Conditional values nullo per property " + property.getURI());
            }

        }

        return validation;
    }

    /**
     * Si aggiunge object property enumeration al model
     * @param oModel, struttura a cui aggiungere i dati
     * @param oClass, class che sis ta parserizzando
     * @param base, modello jena da cui prendere i dati
     * @param property, proprietà che si parserizza
     * @param namespace, utilizzato per parsing della rangeclass
     * @param originalEntityIRI, entity originale della richiesta
     * @param relationType, relation type
     */
    private void createObjectPropertyEnumerationProp(OModel oModel, OClass oClass, OntModel base, com.github.owlcs.ontapi.jena.model.OntModel ontapiBase, OntProperty property,
                                                     String namespace, String originalEntityIRI, String relationType) throws MyStandardException {

        String propertyName = property.getLocalName();

        MyStandardDetailProperties.MyStandardDetailField fieldElement = myStandardDetailProperties.getFields().stream()
                .filter(element -> propertyName.equals(element.getKey()))
                .findAny()
                .orElse(null);

        if (fieldElement != null) {//Se in white list

            LOGGER.info("MyStandard - Property " + propertyName + " is enumeration prop");

            OntResource propertyDomain = property.getDomain();

            if (propertyDomain == null) {
                LOGGER.error("MyStandard - Property " + propertyName + "is enumeration property with  null domain");
            } else {

                OntResource range = property.getRange();//Dal range per ottenere info classe
                if (range != null) {
                    OntClass rangeClass = base.getOntClass(range.getURI());
                    if (rangeClass != null) {
                        String rangeClassIRI = rangeClass.getURI();
                        Map<String, OProperty> objectPropertyMap = oClass.getObjectProperty();
                        String objectPropertyIRI = property.getURI();
                        if (!objectPropertyMap.containsKey(objectPropertyIRI)) {

                            String label = messageSource.getMessage(propertyName, null, propertyName, null);
                            Boolean hidden = fieldElement.getHidden();
                            Boolean isVisibleOnlyAuthenticated = fieldElement.getVisibleOnlyAuthenticated();
                            Integer order = fieldElement.getOrder();
                            Map<String, Object> values = getEnumerationObjectPropertyValues(base, property, true);

                            OProperty objectProperty = new OProperty(objectPropertyIRI);
                            if (relationType.equals(MyStandardConstants.RELATION_TYPE_MULTIPLE_ENUMERATION)) {
                                objectProperty.setType(MyStandardConstants.MULTIPLE_SELECT_TYPE);
                                objectProperty.setValues(getEnumerationObjectPropertyValues(base, property, false));
                            } else {
                                objectProperty.setType(MyStandardConstants.SELECT_TYPE);
                                objectProperty.setValues(getEnumerationObjectPropertyValues(base, property, true));
                            }

                            objectProperty.setLabel(label);
                            objectProperty.setHidden(hidden);
                            objectProperty.setVisibleOnlyAuthenticated(isVisibleOnlyAuthenticated);
                            objectProperty.setOrder(order);
                            objectProperty.setDomainIRI(propertyDomain.getURI());
                            objectProperty.setDomainLocalName(messageSource.getMessage(propertyDomain.getLocalName(), null, propertyDomain.getLocalName(),null));
                            objectProperty.setRangeClassIRI(rangeClassIRI);
                            objectProperty.setRangeClassLocal(messageSource.getMessage(range.getLocalName(), null, range.getLocalName(),null));
                            objectProperty.setLocalName(propertyName);

                            OValidation validation = getOValidationObject(base, ontapiBase, property, namespace, objectProperty.getType(), propertyDomain.getURI(), property);

                            objectProperty.setValidation(validation);
                            OClass rangeClassObject = getClassInfo(base, ontapiBase, oModel, namespace, rangeClass, rangeClassIRI, originalEntityIRI, property);
                            mergeClassInModel(rangeClassIRI, oModel, rangeClassObject);

                            objectPropertyMap.put(objectPropertyIRI, objectProperty);



                        }
                    }
                }




            }
        } /*else {
            LOGGER.debug("MyStandard - Object property functional {} non prevista.", propertyName);
        }*/

        LOGGER.debug("MyStandard - Terminata elaborazione proprietà ");
    }


    /**
     * Si ottiene la lista valori di una Object property definita come enumeration
     * @param base, modello base jena
     * @param property, proprietà da cui ricavare la lista valori
     * @param addEmptyValue, true if add empty value
     * @return lista valori
     */
    private Map<String, Object> getEnumerationObjectPropertyValues(OntModel base, OntProperty property, boolean addEmptyValue) {

        Map<String, Object> value = new HashMap<>();

        OntResource range = property.getRange();//Dal range per ottenere info classe
        if (range != null) {
            OntClass rangeClass = base.getOntClass(range.getURI());
            if (rangeClass != null) {//Si estraggono individuals della classe
                ExtendedIterator<? extends OntResource> extendedIterator = rangeClass.listInstances();

                if (addEmptyValue) value.put(MyStandardConstants.EMPTY_SELECT_KEY, MyStandardConstants.EMPTY_SELECT_VALUE);

                while (extendedIterator.hasNext()) {//Creazione lista enumeration
                    OntResource next = extendedIterator.next();

                    //Skip se proprietà è dominio di business e localname = mainDomain. Aggiungere funzione se capitano altre condizioni
                    if (!myStandardProperties.getOwl().getRelazioneMenu().equalsIgnoreCase(property.getURI())
                            || !next.getLocalName().equalsIgnoreCase(myStandardProperties.getOwl().getMainDomain())) {
                        value.put(next.getURI(), messageSource.getMessage(next.getLocalName(), null, next.getLocalName(), null));
                    }




                }
            } else {
                LOGGER.error("Property {} è una enumeration ma il rangeclass è nullo", property.getURI());
            }
        } else {
            LOGGER.error("Property {} è una enumeration ma il range è nullo", property.getNameSpace() + property.getLocalName());
        }

        return value;
    }

    /**
     * Creazione di una data property
     * @param oModel, modello con i dati per cercare data property esistenti
     * @param oClass, classe a cui aggiungere la data property
     * @param property, property
     * @param namespace, namespace per verificare se la property è una enumeration
     * @param originalEntityIRI, entità originale della richiesta
     * @param base, ontModel
     * @param objectProperty, object property
     */
    private void createDataProperty(OntModel base, com.github.owlcs.ontapi.jena.model.OntModel ontapiBase, OModel oModel, OClass oClass,
                                    OntProperty property, String namespace, String originalEntityIRI, OntProperty objectProperty) throws MyStandardException {

        String propertyName = property.getLocalName();

        //Se mostrare elemento
        MyStandardDetailProperties.MyStandardDetailField configElement = myStandardDetailProperties.getFields().stream()
                .filter(element -> propertyName.equals(element.getKey()))
                .findAny()
                .orElse(null);

        if (configElement != null) {//Elemento in while list

            LOGGER.info("MyStandard - Property " + propertyName + " is data property");

            OntResource propertyDomain = property.getDomain();
            String container = configElement.getContainer();
            String containerLocalName = configElement.getContainerLocalName();

           if (container == null) {
                addDataProperty(base, ontapiBase, oClass, property, namespace, propertyName, configElement, propertyDomain, container, containerLocalName, objectProperty);
            } else {//Nel container si scrive l'entità dove deve essere mostrato il campo (i campi da Thing sono in ogni ggetto, ma voglio mostrarlo in un punto solo)
                Map<String, OClass> modelClasses = oModel.getClasses();
                if (modelClasses.containsKey(originalEntityIRI)) {
                    OClass containerClass = modelClasses.get(originalEntityIRI);
                    Map<String, OProperty> classDataProperty = containerClass.getDataProperty();
                    //Se la proprietà c'è la aggiungo, altrimenti no
                    if (!classDataProperty.containsKey(property.getURI())) {
                        addDataProperty(base, ontapiBase, containerClass, property, namespace, propertyName, configElement, propertyDomain, container, containerLocalName, objectProperty);
                    } else {
                        LOGGER.debug("MyStandard - Property {} già impostata ", propertyName);
                    }
                } else {
                    OClass aClass = new OClass(originalEntityIRI);
                    addDataProperty(base, ontapiBase, aClass, property, namespace, propertyName, configElement, propertyDomain, container, containerLocalName, objectProperty);
                    modelClasses.put(originalEntityIRI, aClass);
                }

            }


        } /*else {
            LOGGER.debug("MyStandard - Data Property {} non prevista", propertyName);
        }*/

        LOGGER.debug("MyStandard - Terminata elaborazione property");
    }

    /**
     * Si aggiunge una data property
     * @param oClass, classe a cui aggiungere la data property
     * @param property, proprietà
     * @param namespace, namespace su cui cercare se proprietà è relazione
     * @param propertyName, nome della proprietà
     * @param configElement, info dal file di configurazione
     * @param propertyDomain, domain su cui mostrare la proprietà
     * @param container, domain su cui mostrare la proprietà eventualmente presente nel file di configurazione
     * @param base, ontModel
     * @param objectProperty, object property
     */
    private void addDataProperty(OntModel base, com.github.owlcs.ontapi.jena.model.OntModel ontapiBase, OClass oClass, OntProperty property, String namespace, String propertyName, MyStandardDetailProperties.MyStandardDetailField configElement,
                                 OntResource propertyDomain, String container, String containerLocalName, OntProperty objectProperty) throws MyStandardException {
        String uri = namespace + MyStandardConstants.ENUMERATION;//Si cerca relation typer per capire se ripetibile o relazione
        RDFNode relation_type = property.getPropertyValue(ResourceFactory.createProperty(uri));

        //Si valuta relation type
        if (relation_type != null) {

            String relationType = relation_type.toString();
            if (relationType.equals(MyStandardConstants.YES_LABEL)) {//Se enumeration, mostro data property come select
                LOGGER.debug("Data property {} è una enumeration", propertyName);

                Map<String, OProperty> dataPropertyMap = oClass.getDataProperty();
                String dataPropertyIRI = property.getNameSpace() + property.getLocalName();
                if (!dataPropertyMap.containsKey(dataPropertyIRI)) {

                    String label = messageSource.getMessage(propertyName, null, propertyName, null);
                    String type = MyStandardConstants.SELECT_TYPE;
                    Boolean hidden = configElement.getHidden();
                    Boolean isVisibleOnlyAuthenticated = configElement.getVisibleOnlyAuthenticated();
                    Integer order = configElement.getOrder();

                    ODataProperty dataProperty = new ODataProperty(dataPropertyIRI);
                    dataProperty.setLabel(label);
                    dataProperty.setType(type);
                    dataProperty.setHidden(hidden);
                    dataProperty.setVisibleOnlyAuthenticated(isVisibleOnlyAuthenticated);
                    dataProperty.setOrder(order);
                    dataProperty.setValues(getEnumerationValues(property));
                    dataProperty.setDomainIRI(container != null ? propertyDomain.getURI() : container);
                    dataProperty.setDomainLocalName(messageSource.getMessage(propertyDomain.getLocalName(), null, propertyDomain.getLocalName(),null));
                    dataProperty.setLocalName(propertyName);


                    OValidation validation = getOValidationObject(base, ontapiBase, property, namespace, type, propertyDomain.getURI(), objectProperty);

                    dataProperty.setValidation(validation);

                    dataPropertyMap.put(dataPropertyIRI, dataProperty);
                }
            }
        } else {

            LOGGER.debug("Data property {} non è una enumeration", propertyName);

            Map<String, OProperty> dataPropertyMap = oClass.getDataProperty();
            String dataPropertyIRI = property.getNameSpace() + property.getLocalName();
            if (!dataPropertyMap.containsKey(dataPropertyIRI)) {

                String label =  messageSource.getMessage(propertyName, null, propertyName, null);
                String type = getDataPropertyTypeByRange(property);
                Boolean hidden = configElement.getHidden();
                Boolean isVisibleOnlyAuthenticated = configElement.getVisibleOnlyAuthenticated();
                Integer order = configElement.getOrder();
                String domain = container != null ? container : propertyDomain.getURI();
                String domainLocalName = containerLocalName != null ? containerLocalName : propertyDomain.getLocalName();

                ODataProperty dataProperty = new ODataProperty(dataPropertyIRI);
                dataProperty.setLabel(label);
                dataProperty.setType(type);
                dataProperty.setHidden(hidden);
                dataProperty.setVisibleOnlyAuthenticated(isVisibleOnlyAuthenticated);
                dataProperty.setOrder(order);
                dataProperty.setDomainIRI(domain);
                dataProperty.setDomainLocalName(messageSource.getMessage(domainLocalName, null, domainLocalName,null));
                dataProperty.setLocalName(propertyName);

                OValidation validation = getOValidationObject(base, ontapiBase, property, namespace, type, domain, objectProperty);

                dataProperty.setValidation(validation);

                dataPropertyMap.put(dataPropertyIRI, dataProperty);
            }
        }

        LOGGER.debug("MyStandard - Terminata aggiunta data property");
    }

    private OValidation getOValidationObject(OntModel base, com.github.owlcs.ontapi.jena.model.OntModel ontapiBase, OntProperty property,
                                             String namespace, String type, String domain, OntProperty objectProperty) throws MyStandardException {

        LOGGER.debug("MyStandard - Calcolo validazione entita {}", property.getURI());

        OValidation validation = getOntApiValidationRestrictions(ontapiBase, domain, property.getURI(), objectProperty);//Get validazion
        validation = getValidationConditionalRequired(base, namespace, property, validation, objectProperty);
        validation = getValidationConditionalShow(base, namespace, property, validation, objectProperty);

        validation = getValidationRegex(type, validation);

        LOGGER.debug("MyStandard - Terminata validazione proprietà");
        return validation;
    }


    /**
     * Si ottiene oggetto validation con regex dal type
     * @param type, si cerca in enum se per il type c'è una regex
     * @param validation, oggetto validation su cui impostare una regex
     * @return oggetto validation
     */
    private OValidation getValidationRegex(String type, OValidation validation) {
        String validationRegex = getValidationRegexFromType(type);

        if (StringUtils.hasText(validationRegex)) {
            if (validation == null) {
                validation = new OValidation();
            }
            validation.setRegex(validationRegex);
        }
        return validation;
    }


    /**
     * Si ottiene oggetto validation da OWL
     * @param ontapiBase, modello ontAPI
     * @param propertyDomain, dominio della proprietà
     * @param propertyUri, uri proprietà
     * @param objectProperty
     * @return oggetto ovalidation
     */
    private OValidation getOntApiValidationRestrictions(com.github.owlcs.ontapi.jena.model.OntModel ontapiBase, String propertyDomain, String propertyUri, OntProperty objectProperty) {
        OValidation validation = new OValidation();

        if (objectProperty != null) {//Si verificano prima le restriction di una eventuale objProperty
            ExtendedIterator<? extends OntResource> listDomains = objectProperty.listDomain();
            while (listDomains.hasNext()) {
                OntResource objectDomain = listDomains.next();
                com.github.owlcs.ontapi.jena.model.OntClass ontApiObjectPropertyClass = ontapiBase.getOntClass(objectDomain.getURI());
                getOValidationWithRestrictions(ontapiBase, objectProperty.getURI(), validation, ontApiObjectPropertyClass);
            }

        }

        //Si aggiungono eventuali validazioni sulle dataproperty
        com.github.owlcs.ontapi.jena.model.OntClass ontApiClass = ontapiBase.getOntClass(propertyDomain);
        getOValidationWithRestrictions(ontapiBase, propertyUri, validation, ontApiClass);


        return validation.isEmpty() ? null : validation;
    }

    /**
     * Si impostano le validazioni sulle restriction
     * @param ontapiBase, modello ontapi
     * @param propertyUri, uri della proprietà di cui cercare le restrictions
     * @param validation, oggetto validation da ritornare
     * @param ontApiClass, classe ontapi
     */
    private void getOValidationWithRestrictions(com.github.owlcs.ontapi.jena.model.OntModel ontapiBase, String propertyUri, OValidation validation, com.github.owlcs.ontapi.jena.model.OntClass ontApiClass) {
        ontApiClass.superClasses().forEach(osuperclass -> {//Le restriction sono sulle superclass
            if (osuperclass instanceof com.github.owlcs.ontapi.jena.model.OntClass.ObjectCardinality){//Object property cardinality
                com.github.owlcs.ontapi.jena.model.OntClass.ObjectCardinality objectCardinality = (com.github.owlcs.ontapi.jena.model.OntClass.ObjectCardinality)osuperclass;
                if (propertyUri.equals(objectCardinality.getProperty().getURI())) {
                    int cardinality = objectCardinality.getCardinality();
                    LOGGER.debug("OBJECT PROPERTY {} WITH CARDINALITY {}", propertyUri, cardinality);

                    if (cardinality != 0) {
                        validation.setRequired(true);
                        validation.setCardinality(cardinality);
                    }
                }

            }


            if (osuperclass instanceof com.github.owlcs.ontapi.jena.model.OntClass.ObjectMinCardinality) {//Cardinalità minima object property
                com.github.owlcs.ontapi.jena.model.OntClass.ObjectMinCardinality objectCardinality = (com.github.owlcs.ontapi.jena.model.OntClass.ObjectMinCardinality) osuperclass;
                if (propertyUri.equals(objectCardinality.getProperty().getURI())) {
                    int cardinality = objectCardinality.getCardinality();
                    LOGGER.debug("OBJECT PROPERTY {} WITH MIN CARDINALITY {}", propertyUri, cardinality);

                    if (cardinality != 0) {
                        validation.setRequired(true);
                        validation.setCardinality(cardinality);
                    }
                }
            }

            if (osuperclass instanceof com.github.owlcs.ontapi.jena.model.OntClass.ObjectMaxCardinality) {
                com.github.owlcs.ontapi.jena.model.OntClass.ObjectMaxCardinality objectCardinality = (com.github.owlcs.ontapi.jena.model.OntClass.ObjectMaxCardinality)osuperclass;
                if (propertyUri.equals(objectCardinality.getProperty().getURI())) {
                    int cardinality = objectCardinality.getCardinality();
                    LOGGER.debug("OBJECT PROPERTY {} WITH MAX CARDINALITY {}", propertyUri, cardinality);

                    if (cardinality != 0) {
                        validation.setRequired(true);
                        validation.setCardinality(cardinality);
                    }
                }           }

            if (osuperclass instanceof com.github.owlcs.ontapi.jena.model.OntClass.DataCardinality) {//Cardinalità data property
                com.github.owlcs.ontapi.jena.model.OntClass.DataCardinality dc = (com.github.owlcs.ontapi.jena.model.OntClass.DataCardinality) osuperclass;
                if (propertyUri.equals(dc.getProperty().getURI())) {
                    int cardinality = dc.getCardinality();
                    LOGGER.debug("DATA PROPERTY {} WITH CARDINALITY {}", propertyUri, cardinality);

                    if (cardinality != 0) {
                        validation.setRequired(true);
                        validation.setCardinality(cardinality);
                    }

                    //Si verifica se il datarange è un datatype
                    OntDataRange dataRange = dc.getValue();
                    if (dataRange != null) {
                        String uri = dataRange.getURI();

                        OntDataRange.Named datatype = ontapiBase.getDatatype(uri);
                        if (datatype != null) {
                            //Lista equivalent classes => dovrebbe essercene solo 1
                            validation.setRegex(getRegexRestriction(datatype));
                        }
                    }
                }

           }

            if (osuperclass instanceof com.github.owlcs.ontapi.jena.model.OntClass.DataSomeValuesFrom ) {
                com.github.owlcs.ontapi.jena.model.OntClass.DataSomeValuesFrom svf = (com.github.owlcs.ontapi.jena.model.OntClass.DataSomeValuesFrom) osuperclass;
                if (propertyUri.equals(svf.getProperty().getURI())) {
                    //Si verifica se il datarange è un datatype

                    LOGGER.debug("OBJECT PROPERTY SAME VALUE FROMS - SOME VALUES FROM - ON PROPERTY [{}] -> " + svf.getValue().getURI(), svf.getProperty().getURI());

                    OntDataRange dataRange = svf.getValue();
                    if (dataRange != null) {
                        String uri = dataRange.getURI();

                        OntDataRange.Named datatype = ontapiBase.getDatatype(uri);
                        if (datatype != null) {

                            //Lista equivalent classes => dovrebbe essercene solo 1
                            validation.setRegex(getRegexRestriction(datatype));
                        }


                    }
                }

            }
            if (osuperclass instanceof com.github.owlcs.ontapi.jena.model.OntClass.DataMinCardinality ) {//Min cardinality data property
                com.github.owlcs.ontapi.jena.model.OntClass.DataMinCardinality dmc = (com.github.owlcs.ontapi.jena.model.OntClass.DataMinCardinality) osuperclass;

                if (propertyUri.equals(dmc.getProperty().getURI())) {
                    int cardinality = dmc.getCardinality();
                    LOGGER.debug("PROPERTY {} WITH MIN CARDINALITY {}", propertyUri, cardinality);

                    if (cardinality != 0) {
                        validation.setRequired(true);
                        validation.setCardinality(cardinality);
                    }

                    //Si verifica se il datarange è un datatype
                    OntDataRange dataRange = dmc.getValue();
                    if (dataRange != null) {
                        String uri = dataRange.getURI();

                        OntDataRange.Named datatype = ontapiBase.getDatatype(uri);
                        if (datatype != null) {

                                //Lista equivalent classes => dovrebbe essercene solo 1
                            validation.setRegex(getRegexRestriction(datatype));
                        }


                    }
                }

            }
        });
    }

    private String getRegexRestriction(OntDataRange.Named datatype) {

        String regex = null;
        Iterator<OntDataRange> ontClassExtendedIterator = datatype.equivalentClasses().iterator();
        while(ontClassExtendedIterator.hasNext() && !StringUtils.hasText(regex)) {
            OntDataRange next = ontClassExtendedIterator.next();

            //Si prende il campo owl:withRestrictions
            Resource resource = next.getPropertyResourceValue(OWL2.withRestrictions);
            for (;;) {
                Resource restr = resource.getPropertyResourceValue(RDF.first);
                if (restr == null || StringUtils.hasText(regex))
                    break;

                StmtIterator pi = restr.listProperties(XSD.pattern);
                while (pi.hasNext() && !StringUtils.hasText(regex)) {
                    Statement restrStmt = pi.next();
                    Literal value = restrStmt.getObject().asLiteral();
                    regex = value.getString();
                }
                // go to the next element of collection
                resource = resource.getPropertyResourceValue(RDF.rest);
            }
        }
        return regex;
    }


    /**
     * Get lista valori di una proprietà tramite datarange
     * @param property, proprietà
     * @return lista valori
     */
    private Map<String, Object> getEnumerationValues(OntProperty property) {
        Map<String, Object> value = new HashMap<>();
        OntResource range = property.getRange();//Dal range per ottenere info classe
        if (range != null) {
            DataRange dataRange = range.asDataRange();
            if (dataRange != null) {
                ExtendedIterator<Literal> literalExtendedIterator = dataRange.listOneOf();

                value.put(MyStandardConstants.EMPTY_SELECT_KEY, MyStandardConstants.EMPTY_SELECT_VALUE);//AddEmptyValue

                //Si ottengono i valori della select
                while (literalExtendedIterator.hasNext()) {
                    Literal next = literalExtendedIterator.next();
                    value.put(next.getString(), next.getString());

                }
            } else {
                LOGGER.error("Datarange per property {} è nullo", property.getNameSpace() + property.getLocalName());
            }
        } else {
            LOGGER.error("Property {} è una enumeration ma il range è nullo", property.getNameSpace() + property.getLocalName());
        }

        return value;
    }

    /**
     * Si memorizza una object property di tipo functional, sia nella classe, sia nel modello come ORelation
     * @param oModel, modello su cui aggiungere la relazione e la classe
     * @param oClass, classe in cui si aggiunge riferimento alla relazione
     * @param property, property
     */
    private void createObjectPropertyFunctionalRelation(OModel oModel, OClass oClass, OntProperty property) {
        String propertyName = property.getLocalName();

        //Si verifica che object property sia in white list
        MyStandardDetailProperties.MyStandardDetailField relationElement = myStandardDetailProperties.getRelations().stream()
                .filter(element -> propertyName.equals(element.getKey()))
                .findAny()
                .orElse(null);

        if (relationElement != null) {//se in white list, aggiungo in struttura come tabs

            LOGGER.info("MyStandard - Property " + propertyName + " is functional");

            Map<String, ORelation> relations = oModel.getRelations();

            //Create relations
            OntResource range = property.getRange();
            OntResource domain = property.getDomain();
            OntProperty propertyInverseOf = property.getInverseOf();

            String relationIRI = property.getNameSpace() + property.getLocalName();
            String rangeIRI = range != null ? range.getURI(): "";
            String rangeLocalName = range != null ? range.getLocalName(): "";
            String domainIRI = domain != null ? domain.getNameSpace() + domain.getLocalName() : "";
            String inverseOfIRI = propertyInverseOf != null ?  propertyInverseOf.getURI(): "";

            ORelation relation = new ORelation(relationIRI, domainIRI, rangeIRI);

            relation.setRangeLocalName(rangeLocalName);
            relation.setLabel(messageSource.getMessage(propertyName, null, propertyName, null));
            relation.setInverseOf(inverseOfIRI);
            relation.setOrder(relationElement.getOrder());

            //Aggiungi relazione al modello
            relations.put(relationIRI, relation);

            //Aggiungi relazione alla classe
            List<String> classRelations = oClass.getRelations();
            if (!classRelations.contains(relationIRI)) {
                classRelations.add(relationIRI);
            }

            if (MyStandardUtil.relationshipToBeShowedOnSummary(myStandardProperties, relationIRI)) {
                addRelationAsDataPropertyOnSummary(oClass, propertyName, relationIRI, domainIRI);

            }

        }/* else {
            LOGGER.debug("MyStandard - Object property {} non prevista.", propertyName);
        }*/

        LOGGER.debug("MyStandard - Terminata elaborazione proprietà ");
    }

    /**
     * Si aggiunge relazione come data property da mostrare in riepilogo
     * @param oClass, classe a cui aggiungere la data property
     * @param propertyName, nome property
     * @param relationIRI, iri relazione
     * @param domainIRI, iri dominio
     */
    private void addRelationAsDataPropertyOnSummary(OClass oClass, String propertyName, String relationIRI, String domainIRI) {
        //Se mostrare elemento
        MyStandardDetailProperties.MyStandardDetailField configElement = myStandardDetailProperties.getFields().stream()
                .filter(element -> propertyName.equals(element.getKey()))
                .findAny()
                .orElse(null);

        if (configElement != null) {

            Boolean hidden = configElement.getHidden();
            Boolean isVisibleOnlyAuthenticated = configElement.getVisibleOnlyAuthenticated();
            Integer order = configElement.getOrder();

            ODataProperty dataProperty = new ODataProperty(relationIRI);
            dataProperty.setLabel(messageSource.getMessage(propertyName, null, propertyName, null));
            dataProperty.setType(DataTypeEnum.STRING.getMorfeoType());
            dataProperty.setHidden(hidden);
            dataProperty.setVisibleOnlyAuthenticated(isVisibleOnlyAuthenticated);
            dataProperty.setOrder(order);
            dataProperty.setDomainIRI(domainIRI);
            dataProperty.setDomainLocalName(messageSource.getMessage(MyStandardConstants.RIEPILOGO_CONTAINER, null, MyStandardConstants.RIEPILOGO_CONTAINER, null));
            dataProperty.setLocalName(propertyName);

            Map<String, OProperty> dataPropertyMap = oClass.getDataProperty();
            dataPropertyMap.put(relationIRI, dataProperty);

        } else {
            LOGGER.debug("Relazione {} da non mostrare nel riepilogo.", propertyName);
        }
    }





    /**
     * Get datatype for range
     * @param p, property
     * @return morfeo datatype
     */
    private String getDataPropertyTypeByRange(OntProperty p) {

        String type = DataTypeEnum.STRING.getType();
        OntResource range = p.getRange();
        if (range != null) {
            type = range.getLocalName();
        }
        return type;
    }
}
