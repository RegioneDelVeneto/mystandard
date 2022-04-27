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
package it.regioneveneto.myp3.mystd.validator;

import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardIndividualOperationEnum;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntityPropertyIndividual;
import it.regioneveneto.myp3.mystd.bean.owl.OClass;
import it.regioneveneto.myp3.mystd.bean.owl.OModel;
import it.regioneveneto.myp3.mystd.bean.owl.OProperty;
import it.regioneveneto.myp3.mystd.bean.owl.OValidation;
import it.regioneveneto.myp3.mystd.config.MyStandardConfig;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import it.regioneveneto.myp3.mystd.utils.MyStandardUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MyStandardValidator {

    private final static Logger LOGGER = LoggerFactory.getLogger(MyStandardValidator.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MyStandardConfig myStandardConfig;

    static MessageSource staticMessageSource;

    static MyStandardConfig staticMyStandardConfig;

    @Autowired
    public void setStaticMessageSource(MessageSource messageSource) {
        MyStandardValidator.staticMessageSource = messageSource;
    }

    @Autowired
    public void setStaticMyStandardConfig(MyStandardConfig myStandardConfig) {
        MyStandardValidator.staticMyStandardConfig = myStandardConfig;
    }


    /**
     * Metodo per la validazione delle data properties di un'entità
     * @param entityPrefix, owl prefix dell'entità
     * @param classDataProperties, data properties definite dalla classe in oModel
     * @param entityDataProperties, data properties della nuova entità in input
     * @param errors, lista di eventuali errori
     */
    public static void validateDataProperties(String entityPrefix, Map<String, OProperty> classDataProperties,
                                                                   Map<String, Object> entityDataProperties, List<Map<String, String>> errors) {

        //Iterate over all class dataproperty
        for (Map.Entry<String, OProperty> dataPropertyEntry: classDataProperties.entrySet()) {
            OProperty dataProperty = dataPropertyEntry.getValue();

            //IRI data property
            String dataPropertyIRI = dataProperty.getIRI();

            //Key per identificare il campo in caso di errore
            String dataPropertyKey = MyStandardUtil.mystandardPrefixForMorfeo(null, dataProperty.getLocalName());

            //Oggetto validazione dataproperty
            OValidation dataPropertyValidation = dataProperty.getValidation();

            //Validazione dataProperty
            validateDataProperty(entityPrefix, entityDataProperties, errors, dataPropertyValidation, dataPropertyIRI, dataPropertyKey);
        }

    }

    /**
     * Si procede alla validazione di una dataProperty
     * @param entityPrefix, prefisso entità
     * @param entityDataProperties, data properties inviate con la nuova entità
     * @param errors, list di eventuali errori
     * @param dataPropertyValidation, info su regole di validazione della dataproperty
     * @param dataPropertyIRI, iri data property
     * @param dataPropertyKey, key data property da usare come chiave in caso di errore
     */
    private static void validateDataProperty(String entityPrefix, Map<String, Object> entityDataProperties, List<Map<String, String>> errors,
                                             OValidation dataPropertyValidation, String dataPropertyIRI, String dataPropertyKey) {

        if (dataPropertyValidation!= null && !dataPropertyValidation.isEmpty()) {//Se per la dataProperty è presente una regola di validazion


            if (dataPropertyValidation.getRequired() != null && dataPropertyValidation.getRequired()) {//Campo è obbligatorio. Verifica la presenza
                validateDataPropertyRequired(entityDataProperties, errors, dataPropertyIRI, MyStandardConstants.ERROR_REQUIRED, null, dataPropertyKey);
            }

            if (StringUtils.hasText(dataPropertyValidation.getRegex())) {//Il valore deve rispettare una regex
                validateDataPropertyRegex(entityDataProperties, errors, dataPropertyValidation, dataPropertyIRI, dataPropertyKey);
            }

            if (StringUtils.hasText(dataPropertyValidation.getConditionalRequired())
                    && StringUtils.hasText(dataPropertyValidation.getConditionalOperator())
                    && dataPropertyValidation.getConditionalValues() != null
                    && dataPropertyValidation.getConditionalValues().size() > 0) {


                validateDataPropertyConditionalRequired(entityPrefix, entityDataProperties, errors, dataPropertyValidation, dataPropertyIRI, dataPropertyKey);

            }

        } else {
            LOGGER.debug("Nessuna regola di validazione per data property {}", dataPropertyIRI);
        }
    }

    /**
     * Validazione di una data property definita come required
     * @param entityDataProperties, data properties inviate con la nuova entità
     * @param errors, list di eventuali errori
     * @param dataPropertyIRI, iri data property
     * @param errorCode, codice di errore da tradurre
     * @param errorMessageArg, eventuali arguments nel messaggio d'errore
     * @param dataPropertyKey, key data property da usare come chiave in caso di errore
     */
    private static void validateDataPropertyRequired(Map<String, Object> entityDataProperties, List<Map<String, String>> errors, String dataPropertyIRI, String errorCode, String errorMessageArg, String dataPropertyKey) {

        //Se l'oggetto ricevuto in input non contiene la proprietà, o il valore è nullo, si segnala l'errore
        if ( entityDataProperties == null || !entityDataProperties.containsKey(dataPropertyIRI)
                || !StringUtils.hasText(Objects.toString(entityDataProperties.get(dataPropertyIRI), ""))) {

            LOGGER.error("Il campo {} obbligatorio, è stato inviato vuoto", dataPropertyKey);
            errors.add(Collections.singletonMap(dataPropertyKey, staticMessageSource.getMessage(errorCode, new String[]{errorMessageArg}, errorCode, null)));
        }
    }


    /**
     * Si valida la dataproperty verificando che sia conforme alla regex
     * @param entityDataProperties, data properties inviate con la nuova entità
     * @param errors, list di eventuali errori
     * @param dataPropertyValidation, info su regole di validazione della dataproperty
     * @param dataPropertyIRI, iri data property
     * @param dataPropertyKey, key data property da usare come chiave in caso di errore
     */
    private static void validateDataPropertyRegex(Map<String, Object> entityDataProperties, List<Map<String, String>> errors, OValidation dataPropertyValidation, String dataPropertyIRI, String dataPropertyKey) {

        String regexDataProperty = entityDataProperties != null ? Objects.toString(entityDataProperties.get(dataPropertyIRI), "") : "";

        if (StringUtils.hasText(regexDataProperty)) {//Se non è valorizzato ed è required, ho già segnalato l'errore prima
            String regex = dataPropertyValidation.getRegex();
            Pattern pattern = Pattern.compile(regex.replaceAll("\\\\", ""));

            Matcher matcher = pattern.matcher(regexDataProperty);

            if (entityDataProperties == null
                    || !matcher.find()) {
                LOGGER.error("Il campo {} è stato inviato con un formato errato rispetto la regular expression. Regex: {} Valore: {}", dataPropertyKey, regex, regexDataProperty);
                errors.add(Collections.singletonMap(dataPropertyKey, staticMessageSource.getMessage(MyStandardConstants.ERROR_REGEX, null, MyStandardConstants.ERROR_REGEX, null)));
            }
        }

    }


    /**
     * Si effettua la validazione di un campo che potrebbe essere obbligatorio in relazione al valore di un altro campo
     * @param entityPrefix, prefisso entità
     * @param entityDataProperties, data properties inviate con la nuova entità
     * @param errors, list di eventuali errori
     * @param dataPropertyValidation, info su regole di validazione della dataproperty
     * @param dataPropertyIRI, iri data property
     * @param dataPropertyKey, key data property da usare come chiave in caso di errore
     */
    private static void validateDataPropertyConditionalRequired(String entityPrefix, Map<String, Object> entityDataProperties, List<Map<String, String>> errors, OValidation dataPropertyValidation,
                                                                String dataPropertyIRI, String dataPropertyKey) {

        String conditionalRequired = dataPropertyValidation.getConditionalRequired();//Nome del campo di cui verificare il valore
        String conditionalRequiredLocalName = conditionalRequired.replace(MyStandardUtil.mystandardPrefixForMorfeo(),"");//LocalName  del campo di cui verificare il valore
        String conditionalRequiredIRI = entityPrefix + conditionalRequiredLocalName;//IRI del campo di cui verificare il valore

        //Nella nuova entità, il valore del campo conditionalRequiredIRI
        String entityConditionalRequired =  entityDataProperties != null ? Objects.toString(entityDataProperties.get(conditionalRequiredIRI), "") : "";

        String conditionalOperator = dataPropertyValidation.getConditionalOperator();//Nella regola è presente anche l'operatore di confronto

        //Il replace per gestire il caso vuoto con doppie virgolette, richiesto da morfeo
        List<String> conditionalValues = dataPropertyValidation.getConditionalValues().stream().map(element -> element.replaceAll("\\\\\"", "")).collect(Collectors.toList());


        if (MyStandardConstants.NOT_EQUALS_OP.equals(conditionalOperator)) {//Operatore diverso
            if (!conditionalValues.contains(entityConditionalRequired)) {//Se il campo indicato nel required è diverso dai valori indicati, allora il campo che sto controllando è obbligatorio
                validateDataPropertyRequired(entityDataProperties, errors, dataPropertyIRI, MyStandardConstants.ERROR_CONDITIONAL_REQUIRED, conditionalRequiredLocalName, dataPropertyKey);
            }
        } else if (MyStandardConstants.EQUALS_OP.equals(conditionalOperator)) {//Operatore uguale
            if (conditionalValues.contains(entityConditionalRequired)) {//Se il campo indicato nel required è uguale a uno dei valori indicati, allora il campo che sto controllando è obbligatorio
                validateDataPropertyRequired(entityDataProperties, errors, dataPropertyIRI, MyStandardConstants.ERROR_CONDITIONAL_REQUIRED, conditionalRequiredLocalName, dataPropertyKey);
            }
        }
    }

    /**
     * Metodo per le validazioni delle ObjectProperties
     * @param entityPrefix, prefisso entità
     * @param classObjectProperties, object properties della classe
     * @param entityPropertyList, lista entity property inviate nella nuova entità
     * @param errors, list di eventuali errori
     */
    public static void validateObjectProperties(String entityPrefix, Map<String, OProperty> classObjectProperties,
                                                  List<MyStandardEntityPropertyIndividual> entityPropertyList, List<Map<String, String>> errors) throws MyStandardException {

        // Si cicla sulla lista di object property definite nel model di una class
        for (Map.Entry<String, OProperty> objectPropertyEntry: classObjectProperties.entrySet()) {
            OProperty objectProperty = objectPropertyEntry.getValue();

            String rangeClassIRI = objectProperty.getRangeClassIRI();//Eventuale classRange da cui prendere le subproperty
            OClass oRangeClass = getOClassFromIRI(rangeClassIRI);//ClassRange
            Map<String, OProperty> subProperties = oRangeClass.getDataProperty();
            if (subProperties != null && subProperties.size() > 0) {
                //Se nella classRange sono state definite subProperty, si cerca il valore nelle entity property arrivate in input
                Optional<MyStandardEntityPropertyIndividual> entityPropertyOptional = entityPropertyList.stream().filter(entity -> entity.get_entityPropertyIRI().equalsIgnoreCase(objectProperty.getIRI())).findFirst();

                //Entity property specifica dell'bject property che si sta analizzando
                MyStandardEntityPropertyIndividual entityProperty = entityPropertyOptional.isPresent() ? entityPropertyOptional.get() : null;

                //Validazione delle subproperties dell'object properties
                validateSubDataProperties(entityPrefix, subProperties, entityProperty, errors, objectProperty.getLocalName());
            } else {

                //Validazione come object property
                validateObjectProperty(entityPropertyList, objectProperty, errors);

            }
        }
    }

    /**
     * Validazione object property senza subproperty
     * @param entityPropertyList, lista entity property inviate nella nuova entità
     * @param objectProperty, object property
     * @param errors, list di eventuali errori
     */
    private static void validateObjectProperty(List<MyStandardEntityPropertyIndividual> entityPropertyList, OProperty objectProperty,
                                        List<Map<String, String>> errors) {
        OValidation objectPropertyValidation = objectProperty.getValidation();
        if (objectPropertyValidation!= null && !objectPropertyValidation.isEmpty()) {

            String objectPropertyIRI = objectProperty.getIRI();
            if (objectPropertyValidation.getRequired() != null && objectPropertyValidation.getRequired()) {//Campo è obbligatorio. Verifica la presenza
                validateObjectPropertyRequired(entityPropertyList, errors, objectPropertyIRI, MyStandardConstants.ERROR_REQUIRED);
            }
        } else {
            LOGGER.debug("Nessuna regola di validazione per object property {}", objectProperty.getIRI());
        }
    }

    /**
     * Si verifica se la object property senza data property required ha il valore
     * @param entityPropertyList, lista entity property inviate nella nuova entità
     * @param errors, list di eventuali errori
     * @param objectPropertyIRI, IRI object property
     * @param errorCode, error code
     */
    private static void validateObjectPropertyRequired(List<MyStandardEntityPropertyIndividual> entityPropertyList, List<Map<String, String>> errors, String objectPropertyIRI, String errorCode) {
        Optional<MyStandardEntityPropertyIndividual> optionalEntityProperty = entityPropertyList.stream().filter(entity -> objectPropertyIRI.equalsIgnoreCase(entity.get_entityPropertyIRI())).findFirst();
        if (optionalEntityProperty.isPresent()) {
            MyStandardEntityPropertyIndividual entityProperty = optionalEntityProperty.get();
            Map<String, Object> entityPropertyDataProperty = entityProperty.getDataProperty();
            if (entityPropertyDataProperty.containsKey(objectPropertyIRI)) {
                String objectPropertyValue = Objects.toString(entityPropertyDataProperty.get(objectPropertyIRI), "");
                if (!StringUtils.hasText(objectPropertyValue)) {
                    errors.add(Collections.singletonMap(objectPropertyIRI, staticMessageSource.getMessage(errorCode, null, errorCode, null)));
                }
            } else {
                errors.add(Collections.singletonMap(objectPropertyIRI, staticMessageSource.getMessage(errorCode, null, errorCode, null)));
            }
        } else {
            errors.add(Collections.singletonMap(objectPropertyIRI, staticMessageSource.getMessage(errorCode, null, errorCode, null)));
        }
    }


    /**
     * Validazione delle data property che sono state trovate dalle object property
     * @param entityPrefix, prefisso entità
     * @param classDataProperties, data properties della classe
     * @param entityDataProperties, data properties ricevute in input da verificare
     * @param errors, lista di eventuali errori
     * @param objectPropertyLocalName, local name della object property
     * @return
     */
    private static List<Map<String, String>> validateSubDataProperties(String entityPrefix, Map<String, OProperty> classDataProperties,
                                                   MyStandardEntityPropertyIndividual entityDataProperties, List<Map<String, String>> errors,
                                                   String objectPropertyLocalName) {


        //Si verifica se data property dell'object property ci sono oppure no
        Map<String, Object> entityDataProperty = entityDataProperties != null ? entityDataProperties.getDataProperty() : null;

        //Iterate over all class dataproperty
        for (Map.Entry<String, OProperty> dataPropertyEntry: classDataProperties.entrySet()) {
            OProperty dataProperty = dataPropertyEntry.getValue();
            String dataPropertyIRI = dataProperty.getIRI();
            String dataPropertyKey = MyStandardUtil.mystandardPrefixForMorfeo((objectPropertyLocalName != null ? objectPropertyLocalName : "") , dataProperty.getLocalName()) ;

            //Validazione della data property
            validateDataProperty(entityPrefix, entityDataProperty, errors, dataProperty.getValidation(), dataPropertyIRI, dataPropertyKey);
        }

        return errors;
    }


    /**
     * Si ottiene la classe a partire da un entityIRI sul Model
     * @param entityIRI, entityIRI
     * @return
     * @throws MyStandardException
     */
    private static OClass getOClassFromIRI(String entityIRI) throws MyStandardException {
        OModel oModel = staticMyStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        Optional<Map.Entry<String, OClass>> optionalClass = oModelClasses.entrySet().stream()
                .filter(entry -> entityIRI.equals(entry.getValue().getIRI()))
                .findFirst();

        if (optionalClass.isPresent() && optionalClass.get().getValue() != null) {
            return optionalClass.get().getValue();//Classe nel model per entity type
        } else {
            throw new MyStandardException("Entity " + entityIRI + " non presente nel file di configurazione.");

        }
    }

    /**
     * Metodo per la validazione delle data properties di un'entità
     * @param entityPrefix, owl prefix dell'entità
     * @param classDataProperties, data properties definite dalla classe in oModel
     * @param entityDataProperties, data properties della nuova entità in input
     * @param errors, lista di eventuali errori
     */
    public static void validateDeltaDataProperties(String entityPrefix, Map<String, OProperty> classDataProperties,
                                              Map<String, Object> entityDataProperties, List<Map<String, String>> errors) {

        //Iterate over all entity data properties to verify delta is ok
        for (Map.Entry<String, Object> entityDataPropertyEntry: entityDataProperties.entrySet()) {

            String entityDataPropertyIRI = entityDataPropertyEntry.getKey();
            //Cerca tra le data property della classe se c'è quella ricevuta nel delta
            if (!classDataProperties.containsKey(entityDataPropertyIRI)) {
                LOGGER.error("Il campo {} non è presente tra quelli previsti per la classe", entityDataPropertyIRI);
                errors.add(Collections.singletonMap(entityDataPropertyIRI, staticMessageSource.getMessage(MyStandardConstants.ERROR_FIELD_NOT_EXISTS, null, MyStandardConstants.ERROR_FIELD_NOT_EXISTS, null)));

            } else {
                OProperty classDataProperty = classDataProperties.get(entityDataPropertyIRI);

                //IRI data property
                String classDataPropertyIRI = classDataProperty.getIRI();

                //Key per identificare il campo in caso di errore
                String classDataPropertyKey = MyStandardUtil.mystandardPrefixForMorfeo(null, classDataProperty.getLocalName());

                //Oggetto validazione dataproperty
                OValidation classDataPropertyValidation = classDataProperty.getValidation();

                //Validazione dataProperty
                validateDataProperty(entityPrefix, entityDataProperties, errors, classDataPropertyValidation, classDataPropertyIRI, classDataPropertyKey);

            }

        }
    }


    /**
     * Metodo per le validazioni delle ObjectProperties
     * @param entityPrefix, prefisso entità
     * @param classObjectProperties, object properties della classe
     * @param entityPropertyList, lista entity property inviate nella nuova entità
     * @param errors, list di eventuali errori
     */
    public static void validateDeltaObjectProperties(String entityPrefix, Map<String, OProperty> classObjectProperties,
                                                List<MyStandardEntityPropertyIndividual> entityPropertyList,
                                                    List<Map<String, String>> errors) throws MyStandardException {

        if (entityPropertyList != null) {
            for (MyStandardEntityPropertyIndividual entityProperty: entityPropertyList) {

                if (!classObjectProperties.containsKey(entityProperty.get_entityPropertyIRI())) {
                    LOGGER.error("Il campo {} non è presente tra quelli previsti per la classe", entityProperty.get_entityPropertyIRI());
                    errors.add(Collections.singletonMap(entityProperty.get_entityPropertyIRI(), staticMessageSource.getMessage(MyStandardConstants.ERROR_FIELD_NOT_EXISTS, null, MyStandardConstants.ERROR_FIELD_NOT_EXISTS, null)));

                } else {

                    //Creazione entity property
                    MyStandardIndividualOperationEnum entityPropertyOperation = entityProperty.get_operation();
                    if (MyStandardIndividualOperationEnum.ADD.equals(entityPropertyOperation) || MyStandardIndividualOperationEnum.MODIFY.equals(entityPropertyOperation) ) {
                        OProperty objectProperty = classObjectProperties.get(entityProperty.get_entityPropertyIRI());
                        String rangeClassIRI = entityProperty.get_entityRangeIRI();
                        OClass oRangeClass = getOClassFromIRI(rangeClassIRI);//ClassRange


                        Map<String, OProperty> subProperties = oRangeClass.getDataProperty();
                        if (subProperties != null && subProperties.size() > 0) {

                            Map<String, Object> entitySubProperties = entityProperty.getDataProperty();

                            //Si filtrano le subproperties della classe range con le sole dataproperty arrivate nel delta
                            Map<String, OProperty> subPropertiesFiltered = subProperties.entrySet().stream()
                                    .filter(subProp -> entitySubProperties.containsKey(subProp.getKey()))
                                    .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

                            //Validazioni properties arrivate nel delta
                            validateSubDataProperties(entityPrefix, subPropertiesFiltered, entityProperty, errors, objectProperty.getLocalName());


                        } else {

                            //Validazione come object property
                            validateObjectProperty(entityPropertyList, objectProperty, errors);

                        }

                    }

                }

            }
        }

    }



}
