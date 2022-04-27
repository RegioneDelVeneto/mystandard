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

import it.regioneveneto.myp3.mystd.bean.enumeration.DataTypeEnum;
import it.regioneveneto.myp3.mystd.bean.enumeration.MorfeoTypeEnum;
import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardStatoEnum;
import it.regioneveneto.myp3.mystd.bean.morfeo.*;
import it.regioneveneto.myp3.mystd.bean.owl.*;
import it.regioneveneto.myp3.mystd.bean.pagination.DatatablePaginated;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardDetailProperties;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.service.FormDataService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import it.regioneveneto.myp3.mystd.utils.MyStandardUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MorfeoDataServiceImpl implements FormDataService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MorfeoDataServiceImpl.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MyStandardDetailProperties myStandardDetailProperties;

    @Autowired
    private MyStandardProperties myStandardProperties;


    /**
     * Merging owl structure with data for Morfeo
     * @param oModel, modello con la struttura dei dati
     * @param entityData, contenuto dei dati
     * @param historicEntityData, lista informazioni storico
     * @param entityType, tipo entità
     * @param owlPrefix, prefisso usato per tipo entita
     * @param readOnly indica se readonly oppure no
     * @param isUserAuthenticated
     * @return
     * @throws MyStandardException
     */
    @Override
    public Object mergeStructureAndData(OModel oModel, JSONObject entityData, List<JSONObject> historicEntityData, String entityType, String owlPrefix,
                                        Boolean readOnly, Boolean isUserAuthenticated, Boolean skipDataInInverseTabs) throws MyStandardException {

        LOGGER.debug("MyStandard - Si esegue il merge tra la struttura e i dati");

        MyStandardMorfeoDetail myStandardMorfeoDetail = new MyStandardMorfeoDetail();

        //MasterFields
        MorfeoElement masterFields = getMasterFieldsFromStructure(oModel, entityType, owlPrefix, readOnly);//Si ottiene struttura masterFields
        setDataIntoMasterFields(masterFields, entityData, owlPrefix, readOnly);//Si settano i dati all'interno della struttura
        myStandardMorfeoDetail.setMasterFields(masterFields);

        //TabsFields
        MorfeoElement tabsFields = getTabsFieldsFromStructure(oModel, entityType, owlPrefix);//Si ottiene struttura dei tabs
        setDataIntoTabsFields(tabsFields, entityData, historicEntityData, readOnly, isUserAuthenticated, skipDataInInverseTabs);//Si settano nei tabs i dati delle relazioni
        myStandardMorfeoDetail.setTabsFields(tabsFields);

        //TableAttachmentFields
        MorfeoElement attachmentTableFields = getAttachmentFieldsFromStructure_new(oModel, entityType, owlPrefix, readOnly);//Si ottiene struttura degli allegati
        setDataIntoTableAttachmentFields(attachmentTableFields, entityData, owlPrefix, readOnly);//Si settano i dati degli allegati

        myStandardMorfeoDetail.setAttachments(attachmentTableFields);

        setMyStandardMorfeoDetailReadOnlyProp(myStandardMorfeoDetail, readOnly);

        LOGGER.debug("MyStandard - Creazione di oggetto con struttura e dati effettuata con successo.");

        return myStandardMorfeoDetail;
    }

    /**
     * Si ritorna la struttura per la creazione di una nuova entità
     * @param oModel, dati struttura
     * @param entityType , tipo entità
     * @param owlPrefix , prefisso entità
     * @return struttura per nuova entità
     */
    @Override
    public Object getStructureWithEmptyData(OModel oModel, String entityType, String owlPrefix) throws MyStandardException {

        LOGGER.debug("MyStandard - Si crea la struttura vuota");
        MyStandardMorfeoDetail myStandardMorfeoDetail = new MyStandardMorfeoDetail();

        //MasterFields
        MorfeoElement masterFields = getMasterFieldsFromStructure(oModel, entityType, owlPrefix,false);//Si ottiene struttura masterFields
        JSONObject entityData = new JSONObject();

        //Set versione con valore di default a 1
        entityData.put(owlPrefix + MyStandardConstants.VERSIONE_COLUMN_KEY, new JSONObject(){{
            this.put(MyStandardConstants.DATA_PROPERTY_IRI, owlPrefix + MyStandardConstants.VERSIONE_COLUMN_KEY);
            this.put(MyStandardConstants.DATA_PROPERTY_VALUE, "1");
            this.put(MyStandardConstants.DATA_PROPERTY_LOCAL_NAME, MyStandardConstants.VERSIONE_COLUMN_KEY);
        }});


        setDataIntoMasterFields(masterFields, entityData, owlPrefix,false);
        setInsertedValueOnState(masterFields, owlPrefix);
        setReadOnlyOnNewMasterFieldsDataProperties(masterFields, owlPrefix);
        myStandardMorfeoDetail.setMasterFields(masterFields);

        //TabsFields
        MorfeoElement tabsFields = getTabsFieldsFromStructure(oModel, entityType, owlPrefix);//Si ottiene struttura dei tabs
        setDataTablesInTabs(tabsFields);
        myStandardMorfeoDetail.setTabsFields(tabsFields);

        //TableAttachmentFields
        MorfeoElement attachmentTableFields = getAttachmentFieldsFromStructure_new(oModel, entityType, owlPrefix, false);//Si ottiene struttura degli allegati
        setColumnsIntoAttachmentTableFields(attachmentTableFields);
        myStandardMorfeoDetail.setAttachments(attachmentTableFields);
        
        setMyStandardMorfeoDetailReadOnlyProp(myStandardMorfeoDetail, false);

        LOGGER.debug("MyStandard - Terminata la creazione della struttura vuota");

        return myStandardMorfeoDetail;
    }

    public void setColumnsIntoAttachmentTableFields(MorfeoElement attachments) {
        MorfeoComponent tableComponent = attachments.getComponents().get(0);
        List<Map<String, Object>> values = new ArrayList<>();
        MorfeoComponentData datatableData = new MorfeoComponentData(
            createAttachmentsTableColumns(false, false),
            values,
            createTablePagination()
        );
        tableComponent.setData(datatableData);
    }

    @Override
    public Object getPaginatedDatatable(List<JSONObject> allDataFiltered, Integer totalRecords) {

        LOGGER.debug("MyStandard - Si ritornano le entità paginate");

        List<Map<String, Object>> mapData = allDataFiltered.stream().map(record -> record.toMap()).collect(Collectors.toList());
        return new DatatablePaginated(mapData, totalRecords);
    }

    /**
     * Si creano i componenti morfeo a partire dalla struttura owl
     * @param oModel, struttura owl
     * @param entityType, tipo entita
     * @param owlPrefix, prefisso entita
     * @param readOnly, indica se readonly oppure no
     * @return oggetto morfeo con i dati di dettaglio
     */
    private MorfeoElement getMasterFieldsFromStructure(OModel oModel, String entityType, String owlPrefix, Boolean readOnly) {
        MorfeoElement masterFields = new MorfeoElement();

        List<MorfeoComponent> containers = new ArrayList<>();//Si creano i containers


        Map<String, OClass> classes = oModel.getClasses();
        if (classes.containsKey(owlPrefix + entityType)) {

            //Si raggruppano e ordinano tutte le object e data property della classe
            Map<String, List<OProperty>> masterFieldsComponentSorted = getAllPropertiesGroupedAndSorted(entityType, owlPrefix, classes);

            //Si creano i container e component per morfeo
            for (Map.Entry<String, List<OProperty>> entry : masterFieldsComponentSorted.entrySet()) {
                String domain = entry.getKey();
                List<OProperty> propertyList = entry.getValue();

                propertyList.sort(Comparator.comparing(prop -> prop.getOrder()));//Si ordinano i component
                List<MorfeoComponent> components = getMorfeoComponentsColumnsProperty(oModel, domain, owlPrefix, propertyList, readOnly);

                if (hasEntityProperty(propertyList) ||  hasVocabulary(propertyList)) {
                    components = insertTitleComponentsProperty(components, propertyList);
                }
                MorfeoComponent container = getMorfeoComponentContainer(domain, components, readOnly);
                containers.add(container);

            }

        }


        setReadOnlyProprertyOnContainers(containers, readOnly);

        masterFields.setComponents(containers);

        return masterFields;
    }

    /**
     * Si prende la classe dal model, e si raggruppano e ordinano i container per tutte le property
     * @param entityType, chiave per ottenere la classe
     * @param owlPrefix, prefisso usato per la classe
     * @param classes lista di classi
     * @return, informazioni raggruppate e ordinate
     */
    private Map<String, List<OProperty>> getAllPropertiesGroupedAndSorted(String entityType, String owlPrefix, Map<String, OClass> classes) {
        OClass entityClass = classes.get(owlPrefix + entityType);
        Map<String, OProperty> dataProperty = entityClass.getDataProperty();
        Map<String, OProperty> objectProperty = entityClass.getObjectProperty();

        Map<String, OProperty> properties = new HashMap<>();//Si crea map con tutte le data e object property
        properties.putAll(dataProperty);
        for (String objectPropertyKey: objectProperty.keySet()) {

            OProperty prop = objectProperty.get(objectPropertyKey);
            if (!(owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN).equals(prop.getIRI())) {
                properties.put(objectPropertyKey, prop);
            }

        }

        //Si raggruppano le proprietà per dominio
        Map<String, List<OProperty>> dataPropertyGrouped = properties.keySet().stream()
                .collect(Collectors.groupingBy(k -> properties.get(k).getDomainLocalName(), Collectors.mapping(d->properties.get(d),Collectors.toList())));

        //Si utilizza comparator per ordinare i container
        Map<String, List<OProperty>> masterFieldsComponentSorted = new TreeMap<>(new DomainComparator());
        masterFieldsComponentSorted.putAll(dataPropertyGrouped);
        return masterFieldsComponentSorted;
    }

    /**
     * Set data for masterfields
     * @param masterFields, masterfields
     * @param entityData, data to set
     */

    /**
     * Si settano i dati RDF dentro la struttura dati
     * @param masterFields, struttura dati
     * @param entityData, dati RDF
     * @param owlPrefix, prefisso usato per l'entità
     * @param readOnly, true se da mostrare in readonly, false altrimenti
     */
    private void setDataIntoMasterFields(MorfeoElement masterFields, JSONObject entityData, String owlPrefix, Boolean readOnly) {
        //Si deve ciclare su masterFields finchè non si arriva all'oggetto su cui settare i dati
        //La struttura owl indica i campi possibili. Nel caso di ripetibili, la struttura mi indica le info di un solo elemento, ma io potre averne N
        //Nel caso, bisogna clonare elemento per ognuno degli N "records" restituiti da rdf
        List<MorfeoComponent> masterFieldsComponents = masterFields.getComponents();
        for (MorfeoComponent masterFieldComponent: masterFieldsComponents) {//Si cicla sui componenti

            List<MorfeoComponent> masterFieldSubComponents = masterFieldComponent.getComponents();
            for (MorfeoComponent masterFieldSubComponent : masterFieldSubComponents) {//Si cicla sui componenti
                List<MorfeoComponent> masterFieldSubComponentColumns = masterFieldSubComponent.getColumns();

                for (MorfeoComponent masterFieldSubComponentColumn : masterFieldSubComponentColumns) {//Si cicla sui componenti
                    List<MorfeoComponent> masterFieldSubComponentColumnsComponents = masterFieldSubComponentColumn.getComponents();

                    //Ciclo for con indice perchè si usa per aggiungere
                    for (int index = 0; index < masterFieldSubComponentColumnsComponents.size(); index++) {//Si cicla sui componenti
                        MorfeoComponent masterFieldSubComponentColumnsComponent = masterFieldSubComponentColumnsComponents.get(index);

                        String key = masterFieldSubComponentColumnsComponent.getOriginalKey();

                        try {

                            Object value = entityData.get(key);//Si cerca la key nei dati RDF

                            List<MorfeoComponent> fieldColumns = masterFieldSubComponentColumnsComponent.getColumns();
                            if (fieldColumns == null || fieldColumns.size() == 0) {//Componente non ha subfields, ma ha solo un campo
                                if (value instanceof JSONArray) {//Se oggetto è un array
                                    JSONArray array = (JSONArray) value;
                                    for (int arrayIndex = 0; arrayIndex < array.length() && masterFieldSubComponentColumnsComponent.getDefaultValue() == null; arrayIndex++) {
                                        try {
                                            JSONObject obj = array.getJSONObject(arrayIndex);
                                            if (obj.has(key)) {//Se trovo il valore lo setto come defaultValue
                                                masterFieldSubComponentColumnsComponent.setDefaultValue(obj.getString(key));
                                            } else if (obj.has(MyStandardConstants.FOAF_NAME)) {//Il valore potrebbe non essere definito come key, ma come rdf:type
                                                masterFieldSubComponentColumnsComponent.setDefaultValue(obj.getString(MyStandardConstants.FOAF_NAME));
                                            }
                                        } catch (JSONException e) {
                                            LOGGER.error("Elemento in JSONARRAY indice {} non è un JSONObject", arrayIndex);
                                        }
                                    }

                                } else if (value instanceof JSONObject) {//{

                                    //RDF non ha tornato un JSONArray, si fa il set del value se non nullo
                                    JSONObject object = (JSONObject) value;
                                    if (object.has(MyStandardConstants.DATA_PROPERTY_IRI)) {
                                        String dataPropertyIRI = String.valueOf(object.get(MyStandardConstants.DATA_PROPERTY_IRI));
                                        String dataPropertyValue = String.valueOf(object.get(MyStandardConstants.DATA_PROPERTY_VALUE));
                                        String dataPropertyLocalName = String.valueOf(object.get(MyStandardConstants.DATA_PROPERTY_LOCAL_NAME));

                                        masterFieldSubComponentColumnsComponent.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null,  dataPropertyLocalName));
                                        masterFieldSubComponentColumnsComponent.setOriginalKey(dataPropertyIRI);
                                        masterFieldSubComponentColumnsComponent.setDefaultValue(dataPropertyValue);
                                    } else if (object.has(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_IRI)) {
                                        String entityPropertyIRI = String.valueOf(object.get(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_IRI));
                                        String entityPropertyLocalName = String.valueOf(object.get(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_LOCAL_NAME));

                                        masterFieldSubComponentColumnsComponent.setKey(MyStandardConstants.PREFIX_MYSTD + "_" + entityPropertyLocalName);
                                        masterFieldSubComponentColumnsComponent.setOriginalKey(entityPropertyIRI);
                                        if (object.has(MyStandardConstants.COMPONENTS_KEY)) {
                                            JSONArray componentArray = object.getJSONArray(MyStandardConstants.COMPONENTS_KEY);
                                            if (componentArray.length() > 0) {
                                                JSONObject component = componentArray.getJSONObject(0);
                                                if (component.has(MyStandardConstants.OBJ_PROPERTY_TARGET_INDIVIDUAL_IRI)) {
                                                    String entityPropertyValue = String.valueOf(component.get(MyStandardConstants.OBJ_PROPERTY_TARGET_INDIVIDUAL_IRI));
                                                    masterFieldSubComponentColumnsComponent.setDefaultValue(entityPropertyValue);
                                                }
                                            }

                                        }


                                    } else {
                                        LOGGER.error("Property deve contenere data property IRI o entity property IRI.");
                                    }

                                    //Id entita sempre nascosto, versione stato sempre readonly
                                    if ((owlPrefix + MyStandardConstants.ID_ENTITA_COLUMN_KEY).equals(masterFieldSubComponentColumnsComponent.getOriginalKey())) {
                                        masterFieldSubComponentColumnsComponent.setHidden(true);
                                    }
                                    if ((owlPrefix + MyStandardConstants.VERSIONE_COLUMN_KEY).equals(masterFieldSubComponentColumnsComponent.getOriginalKey())) {
                                        masterFieldSubComponentColumnsComponent.setReadOnly(true);
                                    }
                                    if ((owlPrefix + MyStandardConstants.STATO_OBJPROP_KEY).equals(masterFieldSubComponentColumnsComponent.getOriginalKey())) {
                                        masterFieldSubComponentColumnsComponent.setReadOnly(true);
                                    }


                                } else {
                                    LOGGER.error("Campo non è ne un JSONArray e nemmeno un JSONObject");
                                }
                            } else {//Il componente tornato da OWL ha dei subfields

                                if (value instanceof JSONObject) {

                                    JSONObject objectValue = (JSONObject)value;
                                    Object componentsValue = objectValue.getJSONArray(MyStandardConstants.COMPONENTS_KEY);

                                    if (componentsValue instanceof JSONArray) {//Se oggetto è un array

                                        JSONArray array = (JSONArray) componentsValue;
                                        for (int arrayIndex = 0; arrayIndex < array.length(); arrayIndex++) {//Ciclo sull'array di elementi tornati nei dati
                                            JSONObject obj = array.getJSONObject(arrayIndex);
                                            setSubFieldsDataForObject(masterFieldSubComponentColumnsComponents, index, masterFieldSubComponentColumnsComponent, arrayIndex, obj, readOnly);
                                        }

                                    } else if (componentsValue instanceof JSONObject) {

                                        JSONObject jsonObject = (JSONObject) componentsValue;
                                        setSubFieldsDataForObject(masterFieldSubComponentColumnsComponents, index, masterFieldSubComponentColumnsComponent, 0, jsonObject, readOnly);

                                    } else {
                                        LOGGER.error("Tipologia campo non prevista");
                                    }
                                }

                            }
                        } catch (JSONException e) {
                            LOGGER.debug("Key {} non presente nei dati RDF ", key);
                        }
                    }

                }
            }
        }
    }

    /**
     * Si raggruppano nell'oggetto tabsFields le relazioni da far vedere nel tab
     * @param oModel, struttura owl
     * @return oggetto morfeo con i dati dei tab
     */
    private MorfeoElement getTabsFieldsFromStructure(OModel oModel, String entityType, String owlPrefix) throws MyStandardException {
        MorfeoElement tabsFields = new MorfeoElement();
        List<MorfeoComponent> tabsComponents = new ArrayList<>();

        //Lista components tabs
        List<MorfeoComponent> componentsTabs = getMorfeoTabsComponents(oModel, entityType, owlPrefix);

        //Componente contenitore per tabs
        MorfeoComponent morfeoTabsWrapperComponent = new MorfeoComponent();
        morfeoTabsWrapperComponent.setKey(MyStandardConstants.TABS_WRAPPER_KEY);
        morfeoTabsWrapperComponent.setOriginalKey(MyStandardConstants.TABS_WRAPPER_KEY);
        morfeoTabsWrapperComponent.setLabel(MyStandardConstants.TABS_WRAPPER_LABEL);
        morfeoTabsWrapperComponent.setType(MorfeoTypeEnum.TABS.getCode());
        morfeoTabsWrapperComponent.setComponents(componentsTabs);

        tabsComponents.add(morfeoTabsWrapperComponent);

        tabsFields.setComponents(tabsComponents);

        return tabsFields;
    }

    private void setDataIntoTableAttachmentFields(MorfeoElement morfeoFormAttachmentFields, JSONObject entityData, String owlPrefix, Boolean readOnly) {
        MorfeoComponent tableComponent = morfeoFormAttachmentFields.getComponents().get(0);
        List<Map<String, Object>> values = new ArrayList<>();

        Iterator<String> entityDataKeys = entityData.keys();
        while (entityDataKeys.hasNext()) {
            String key = entityDataKeys.next();
            if (key.equals(owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN)) {
                JSONObject attachments = entityData.getJSONObject(key);
                JSONArray components = attachments.getJSONArray("components");

                for (int i = 0; i < components.length(); i++) {
                    JSONObject attachment = components.getJSONObject(i);

                    try {
                        Iterator<String> keys = attachment.keys();
                        Map<String, Object> jsonMap = new HashMap<>();
                        while (keys.hasNext()) {
                            String k = keys.next();
                            if (attachment.get(k) instanceof String) {
                                jsonMap.put(k, attachment.get(k));
                            } else {
                                JSONObject map = (JSONObject) attachment.get(k);
                                String thisKey = (String) map.get("_dataPropertyIRI");
                                String thisValue = (String) map.get("_dataPropertyValue");
                                jsonMap.put(thisKey, thisValue);
                            }
                        }
                        values.add(jsonMap);
        

        
                    } catch (JSONException e) {
                        LOGGER.error("Oggetto con i dati non contiene proprietà allegati {}", key);
                    } 
                }
            }
            // Creazione datatable con paginazione
            MorfeoComponentData datatableData = new MorfeoComponentData(
                createAttachmentsTableColumns(readOnly, false),
                values,
                createTablePagination()
            );
            tableComponent.setData(datatableData);
        }
    }

    private void setHistoricTableData(MorfeoComponent tableComponent, List<JSONObject> historicEntityData, Boolean isUserAuthenticated, Boolean readOnly) {
        tableComponent.setType(MorfeoTypeEnum.DATATABLE.getCode());
        tableComponent.setKey(MyStandardConstants.HISTORIC_DATATABLE_KEY);
        tableComponent.setOriginalKey(MyStandardConstants.HISTORIC_DATATABLE_KEY);
        String key = tableComponent.getOriginalKey();
        tableComponent.setDataSrc(MyStandardConstants.MORFEO_DATASRC_VALUES);
        tableComponent.setHidden(false);
        List<Map<String, Object>> values = new ArrayList<>();


        for (JSONObject historicRow: historicEntityData) {

            try {
                Iterator<String> keys = historicRow.keys();
                Map<String, Object> translated = new HashMap<>();
                while (keys.hasNext()) {
                    String k = keys.next();
                    translated.put(k, historicRow.get(k));
                }
                values.add(translated);

                //Creazione datatable con paginazione
                MorfeoComponentData datatableData = new MorfeoComponentData(
                    createDatatableColumns(readOnly, false, isUserAuthenticated),
                    values,
                    createTablePagination()
                );
                tableComponent.setData(datatableData);

            } catch (JSONException e) {
                LOGGER.error("Oggetto con i dati non contiene proprietà components contenente le relazioni per relazione {}", key);
            }

        }
    }

    /**
     * Si impostano i dati dei tabs
     * @param structure, struttura per tabs per morfeo
     * @param entityData, entità di cui prendere gli tabs da mostrare al dettaglio
     * @param historicEntityData, lista dati da mostrare nello storico
     * @param isUserAuthenticated
     */
    private void setDataIntoTabsFields(MorfeoElement structure, JSONObject entityData, List<JSONObject> historicEntityData,
                                       Boolean readOnly, Boolean isUserAuthenticated, Boolean skipDataInInverseTabs) {

        //Iterazione all'interno della struttura
        List<MorfeoComponent> firstLevelComps = structure.getComponents();

        for (MorfeoComponent firstLevelComp: firstLevelComps) {

            List<MorfeoComponent> secondLevelComps = firstLevelComp.getComponents();

            for (MorfeoComponent secondLevelComp: secondLevelComps) {

                if (MyStandardConstants.HISTORICAL_TAB_KEY.equals(secondLevelComp.getOriginalKey())) {
                    setHistoricTableData(secondLevelComp.getComponents().get(0), historicEntityData, isUserAuthenticated, readOnly);
                }

                if (MyStandardConstants.TAB_RELATIONS_KEY.equals(secondLevelComp.getOriginalKey())) {

                    List<MorfeoComponent> relationsTabs = secondLevelComp.getComponents();

                    for (MorfeoComponent relationsTab: relationsTabs) {

                        List<MorfeoComponent> relationsTabComps = relationsTab.getComponents();
                        List<MorfeoComponent> components = new ArrayList<>();

                        for (MorfeoComponent relationsTabComp: MyStandardUtil.emptyIfNull(relationsTabComps)) {//Si cicla sulle relazioni definite nella struttura se presenti

                            MorfeoComponent wrapperTab = new MorfeoComponent();
                            String key = relationsTabComp.getOriginalKey();
                            String label = relationsTabComp.getLabel();
                            String range = relationsTabComp.getRange();
                            String functionalPropertyIRI = relationsTabComp.getFuntionalProperty();

                            MorfeoComponent datatable = new MorfeoComponent();//Creazione datatable con i dati
                            datatable.setKey(MyStandardConstants.DATATABLE_KEY_PREFIX + label);
                            datatable.setOriginalKey(MyStandardConstants.DATATABLE_KEY_PREFIX + label);
                            datatable.setType(MorfeoTypeEnum.DATATABLE.getCode());
                            datatable.setReadOnly(true);
                            datatable.setDataSrc(MyStandardConstants.MORFEO_DATASRC_VALUES);

                            MorfeoComponent containerComp = new MorfeoComponent();
                            containerComp.setKey(MyStandardConstants.TAB_RELATION_PREFIX_KEY + key);
                            containerComp.setOriginalKey(MyStandardConstants.TAB_RELATION_PREFIX_KEY + key);
                            containerComp.setLabel(label);
                            containerComp.setRange(range);
                            containerComp.setFunctionalProperty(functionalPropertyIRI);
                            List<MorfeoComponent> childComps = new ArrayList<>();
                            containerComp.setComponents(childComps);

                            // Bottone Nuova Relazione
                            if (!readOnly && StringUtils.hasText(relationsTabComp.getInverse()) && !activeSpecialRelationship(key)) {
                                MorfeoComponent addRelationshipButton = new MorfeoComponent();
                                addRelationshipButton.setType(MorfeoTypeEnum.BUTTON.getCode());
                                addRelationshipButton.setLabel(MyStandardConstants.NEW_RELATIONSHIP);
                                addRelationshipButton.setKey(MyStandardConstants.NEW_RELATIONSHIP_BUTTON_PREFIX + key);
                                addRelationshipButton.setOriginalKey(MyStandardConstants.NEW_RELATIONSHIP_BUTTON_PREFIX + key);
                                addRelationshipButton.setDataSrc("values");
                                addRelationshipButton.setReadOnly(false);
                                childComps.add(addRelationshipButton);
                            }

                            MorfeoComponent tmpComponent = createMorfeoComponent(MyStandardConstants.TAB_RELATION_PREFIX_KEY + key, label, Collections.singletonList(datatable));

                            childComps.add(tmpComponent.getComponents().get(0));
                            components.add(containerComp);

                            //Creazione del tab contenente il datatable

                            relationsTabComp.setComponents(components);
                            wrapperTab.setComponents(components);

                            //Setting dei dati iterando le relazioni ricevute da rdf
                            List<Map<String, Object>> values = new ArrayList<>();

                            try {
                                JSONArray entityDataRelationshipTabs = entityData.getJSONArray(MyStandardConstants.TABS_WRAPPER_LABEL);//Si ottengono la lista di tabs dai dati

                                for (int i = 0; i < entityDataRelationshipTabs.length(); i++) {
                                    JSONObject entityDataRelationshipTab = entityDataRelationshipTabs.getJSONObject(i);
                                    //Custom tab name è un campo custom con il nome del tab.
                                    if (entityDataRelationshipTab.has(MyStandardConstants.FUNCTIONAL_PROPERTY_IRI) && key.equals(entityDataRelationshipTab.getString(MyStandardConstants.FUNCTIONAL_PROPERTY_IRI))) {
                                        //Se il json contiene i dati del tab
                                        if (entityDataRelationshipTab.has(MyStandardConstants.FUNCTIONAL_PROPERTY_IRI) ) {//Set Key with functional property IRI
                                            relationsTabComp.setKey(entityDataRelationshipTab.get(MyStandardConstants.CUSTOM_TAB_NAME).toString());
                                            relationsTabComp.setOriginalKey(entityDataRelationshipTab.get(MyStandardConstants.FUNCTIONAL_PROPERTY_IRI).toString());
                                        }

                                        try {

                                            //Se true, significa non inserire dati dentro alcuni tabs (passivi o speciali)
                                            if (!skipDataInInverseTabs || (StringUtils.hasText(relationsTabComp.getInverse()) && !activeSpecialRelationship(key))) {
                                                JSONArray rows = entityDataRelationshipTab.getJSONArray(MyStandardConstants.COMPONENTS_KEY);
                                                for (int j = 0; j < rows.length(); j++) {
                                                    Iterator<String> keys = rows.getJSONObject(j).keys();
                                                    Map<String, Object> translated = new HashMap<>();
                                                    while (keys.hasNext()) {
                                                        String k = keys.next();
                                                        translated.put(k, rows.getJSONObject(j).get(k));
                                                    }
                                                    values.add(translated);
                                                }
                                            }

                                        } catch (JSONException e) {
                                            LOGGER.error("Oggetto con i dati non contiene proprietà components contenente le relazioni per relazione {}", key);
                                        }
                                    }
                                }

                            } catch (JSONException e) {
                                LOGGER.error("Tabs non trovati in dati json.");
                            }

                            //Creazione datatable con paginazione
                            MorfeoComponentData datatableData = new MorfeoComponentData(
                                    createDatatableColumns(readOnly, true, null),
                                    values,
                                    createTablePagination()
                            );
                            datatable.setData(datatableData);
                        }
                        relationsTab.setComponents(components);
                    }
                }

            }
        }
    }

    /**
     * Si elencano relazioni speciali non modificabili dall'utente (solo per le relazioni attive, non per le inverse)
     * @param key, chiave della relazione da verificare
     * @return true se relazione speciale, false altrimenti
     */
    private boolean activeSpecialRelationship(String key) {
       return myStandardProperties.getOwl().getDefinisceUri().equalsIgnoreCase(key)
               || myStandardProperties.getOwl().getSpecializzaUri().equalsIgnoreCase(key);
    }


    /**
     * Si ottiene attachmentFields che è la struttura degli allegati
     * @param oModel, struttura dei dati
     * @param entityType, tipo entità
     * @param owlPrefix, prefisso usato per la struttura dei dati
     * @param readOnly, indica se mostrare gli allegati in modalità readonly o no
     * @return struttura degli allegati
     * @throws MyStandardException se nel modello non è presente la classe
     */
    private MorfeoElement getAttachmentFieldsFromStructure(OModel oModel, String entityType, String owlPrefix, Boolean readOnly) throws MyStandardException {

        MorfeoElement attachments = new MorfeoElement();
        List<MorfeoComponent> allegatoComponents = new ArrayList<>();


        Map<String, OClass> classes = oModel.getClasses();
        if (classes.containsKey(owlPrefix + entityType)) {//La classe esiste nel model

            OClass entityClass = classes.get(owlPrefix + entityType);
            Map<String, OProperty> objectProperty = entityClass.getObjectProperty();

            //Si controlla se per la classe è prevista la presenza di allegati
            if (objectProperty.containsKey(owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN)) {
                OProperty allegati = objectProperty.get(owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN);
                //Creazione struttura allegati
                MorfeoComponent allegato = getMorfeoComponentFromObjectProperty(oModel, allegati, owlPrefix, readOnly);
                allegatoComponents.add(allegato);

            }

            attachments.setComponents(allegatoComponents);

            return attachments;

        } else {
            throw new MyStandardException("Nel modello ricavato non è presente la classe " + owlPrefix + entityType);
        }

    }

    /**
     * Si ottiene attachmentFields che è la struttura degli allegati
     * @param oModel, struttura dei dati
     * @param entityType, tipo entità
     * @param owlPrefix, prefisso usato per la struttura dei dati
     * @param readOnly, indica se mostrare gli allegati in modalità readonly o no
     * @return struttura degli allegati
     * @throws MyStandardException se nel modello non è presente la classe
     */
    private MorfeoElement getAttachmentFieldsFromStructure_new(OModel oModel, String entityType, String owlPrefix, Boolean readOnly) throws MyStandardException {

        MorfeoElement attachments = new MorfeoElement();
        List<MorfeoComponent> allegatoComponents = new ArrayList<>();


        Map<String, OClass> classes = oModel.getClasses();
        if (classes.containsKey(owlPrefix + entityType)) {//La classe esiste nel model

            OClass entityClass = classes.get(owlPrefix + entityType);
            Map<String, OProperty> objectProperty = entityClass.getObjectProperty();

            //Si controlla se per la classe è prevista la presenza di allegati
            if (objectProperty.containsKey(owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN)) {
                OProperty allegati = objectProperty.get(owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN);
                //Creazione struttura allegati
                MorfeoComponent allegato = getMorfeoComponentFromObjectProperty_new(oModel, allegati, owlPrefix, readOnly);
                allegatoComponents.add(allegato);

            }

            attachments.setComponents(allegatoComponents);

            return attachments;

        } else {
            throw new MyStandardException("Nel modello ricavato non è presente la classe " + owlPrefix + entityType);
        }

    }

    /**
     * Si crea un MorfeoComponent di una dataProperty
     * @param dataProp, data property
     * @param owlPrefix, prefisso owl per entità
     * @param readonly, se impostare il componente come readonlu
     * @return MorfeoComponent della data property
     */
    private MorfeoComponent getMorfeoComponentFromDataProperty(OProperty dataProp, String owlPrefix, boolean readonly) {

        MorfeoComponent component = new MorfeoComponent();
        component.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null, dataProp.getLocalName()));
        component.setOriginalKey(dataProp.getIRI());
        component.setLabel(dataProp.getLabel());
        component.setDomain(dataProp.getDomainLocalName());
        component.setRange(dataProp.getRangeClassIRI());

        try {
            String type = dataProp.getType();
            if (MyStandardConstants.SELECT_TYPE.equals(type)) {
                setMorfeoComponentAsSelect(dataProp, component);
            } else if (MyStandardConstants.MULTIPLE_SELECT_TYPE.equals(type)) {
                setMorfeoComponentAsMultipleSelect(dataProp, component);
            } else {
                component.setType(DataTypeEnum.of(dataProp.getType()).getMorfeoType());//Può rilanciare eccezione se non trova
            }
        } catch (MyStandardException e) {//Default value
            component.setType(DataTypeEnum.STRING.getMorfeoType());
        }
        component.setOrder(dataProp.getOrder());


        //Id entita sempre hidden
        if ((owlPrefix + MyStandardConstants.ID_ENTITA_COLUMN_KEY).equals(dataProp.getIRI())) {
            component.setHidden(true);
        } else {
            component.setHidden(fieldMustBeHidden(dataProp.getHidden(), dataProp.getVisibleOnlyAuthenticated()));
        }

        //Codice e versione sempre readonly
        component.setReadOnly(readonly);
        component.setValidate(getMorfeoValidationFromProperty(dataProp.getValidation(), MyStandardUtil.mystandardPrefixForMorfeo(null, dataProp.getLocalName()) ));
        component.setConditional(getMorfeoConditionalValidation(dataProp.getValidation()));



        return component;

    }

    /**
     * Creazione morfeo component per mostrare o nascondere elementi
     * @param validation, oggetto validazione
     * @return morfeo component per mostrare o nascondere elementi
     */
    private MorfeoConditionalValidate getMorfeoConditionalValidation(OValidation validation) {
        MorfeoConditionalValidate conditional = null;
        if (validation != null && validation.getConditionalShow() != null) {

            conditional = new MorfeoConditionalValidate();
            conditional.setShow(validation.getConditionalShow());
            conditional.setWhen(validation.getConditionalWhen());
            conditional.setEq(validation.getConditionalValue());
        }
        return conditional;
    }

    /**
     * Creazione morfeo component per validazione da OValidation
     * @param validation, oggetto validazione
     * @param keyName, nome proprietà
     * @return morfeo component per validazione
     */
    private MorfeoValidate getMorfeoValidationFromProperty(OValidation validation, String keyName) {
        MorfeoValidate validate = null;
        if (validation != null) {

            validate = new MorfeoValidate();

            if (validation.getCardinality() != null && validation.getCardinality() > 1) {//Cardinalità fissa i max e min length
                validate.setMinLength(validation.getCardinality());
                validate.setMaxLength(validation.getCardinality());

            } else {
                validate.setMin(validation.getMin());
                validate.setMax(validation.getMax());
                validate.setMinLength(validation.getMinLength());
                validate.setMaxLength(validation.getMaxLength());
            }

            if (StringUtils.hasText(validation.getRegex())) {
                validate.setCustom(createJsonLogicStringRegex(MyStandardConstants.REGEX_OP, validation.getRegex(), keyName));
            }
            if (StringUtils.hasText(validation.getConditionalRequired())) {
                validate.setRequired(createJsonLogicStringConditional(validation.getConditionalRequired(), validation.getConditionalOperator(), validation.getConditionalValues()));
             } else {
                validate.setRequired(validation.getRequired() != null ? String.valueOf(validation.getRequired()) : null);
            }

        }
        return validate;
    }

    private String createJsonLogicStringConditional(String keyName, String operator, List<String> values) {

        if (values.size() == 1) {
            return "[" + getJsonLogicSingleConditional(operator, keyName, values.get(0)) + "]";
        } else if (values.size() > 1) {
            String initialJsonLogic = "[{\"" + MyStandardConstants.AND_OP + "\":   [";
            for (int index = 0; index< values.size(); index++) {
                String conditional = getJsonLogicSingleConditional(operator, keyName, values.get(index));
                initialJsonLogic += (index != 0) ? "," + conditional : conditional;
            }
            return initialJsonLogic + "]} ]";
        } else {
            return null;
        }

    }

    private String getJsonLogicSingleConditional(String operation, String keyName, String value) {
        String valueReplaced = value.replaceAll("\\\\", "");
        return "{\"" + operation+ "\":[{\"var\":\"" + keyName + "\"}," + valueReplaced + "]}";
    }

    private String createJsonLogicStringRegex(String operation, String value, String keyName) {
        return "[{\"" + operation + "\": [\"" + value + "\", {\"var\":\"" + keyName + "\"}]}]";

    }

    /**
     * Si ottiene un morfeo component adatto alle object property
     * @param oModel, modello con la struttura dati
     * @param objProp, object property
     * @param owlPrefix, prefisso owl entità
     * @param readonly, indica se deve essere visualizzato in readonly oppure no
     * @return
     */
    private MorfeoComponent getMorfeoComponentFromObjectProperty(OModel oModel, OProperty objProp, String owlPrefix, Boolean readonly)  {
        MorfeoComponent objectPropertyComponent = new MorfeoComponent();
        objectPropertyComponent.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null, objProp.getLocalName()));
        objectPropertyComponent.setOriginalKey(objProp.getIRI());
        objectPropertyComponent.setLabel(objProp.getLabel());
        objectPropertyComponent.setDomain(objProp.getDomainLocalName());
        objectPropertyComponent.setRange(objProp.getRangeClassIRI());
        objectPropertyComponent.setHidden(fieldMustBeHidden(objProp.getHidden(), objProp.getVisibleOnlyAuthenticated()));


        if ((owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN).equals(objProp.getIRI()) && !readonly) {

            //Si parla di allegati non readonly ma repeatable
            objectPropertyComponent.setType(MorfeoTypeEnum.REPEATABLE.getCode());
            objectPropertyComponent.setDataSrc(MyStandardConstants.MORFEO_DATASRC_VALUES);
            objectPropertyComponent.setReadOnly(readonly);

            List<MorfeoComponent> attachmentComponents = getAllegatoRepeatableComponents(oModel, objProp, readonly);
            objectPropertyComponent.setComponents(attachmentComponents);

        } else {
            objectPropertyComponent.setType(MorfeoTypeEnum.COLUMNS.getCode());
            List<MorfeoComponent> columnsComponents = getReadonlyColumnsComponents(oModel, objProp, readonly);

            objectPropertyComponent.setColumns(columnsComponents);
        }


        return objectPropertyComponent;

    }



    /**
     * Si ottiene un morfeo component adatto alle object property
     * @param oModel, modello con la struttura dati
     * @param objProp, object property
     * @param owlPrefix, prefisso owl entità
     * @param readonly, indica se deve essere visualizzato in readonly oppure no
     * @return
     */
    private MorfeoComponent getMorfeoComponentFromObjectProperty_new(OModel oModel, OProperty objProp, String owlPrefix, Boolean readonly)  {
        MorfeoComponent objectPropertyComponent = new MorfeoComponent();
        objectPropertyComponent.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null, objProp.getLocalName()));
        objectPropertyComponent.setOriginalKey(objProp.getIRI());
        objectPropertyComponent.setLabel(objProp.getLabel());
        objectPropertyComponent.setDomain(objProp.getDomainLocalName());
        objectPropertyComponent.setRange(objProp.getRangeClassIRI());

        if ((owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN).equals(objProp.getIRI())) {

            if ((owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN).equals(objProp.getIRI())) {
                objectPropertyComponent.setType(MorfeoTypeEnum.DATATABLE.getCode());
                objectPropertyComponent.setDataSrc(MyStandardConstants.MORFEO_DATASRC_VALUES);
                objectPropertyComponent.setReadOnly(readonly);
            }

        } else {
            objectPropertyComponent.setType(MorfeoTypeEnum.COLUMNS.getCode());
            List<MorfeoComponent> columnsComponents = getReadonlyColumnsComponents(oModel, objProp, readonly);

            objectPropertyComponent.setColumns(columnsComponents);
        }

        return objectPropertyComponent;

    }

    private List<MorfeoComponentColumn> createAttachmentsTableColumns(Boolean readOnly, Boolean isUserAuthenticated) {
        List<MorfeoComponentColumn> columns = new ArrayList<>();

        columns.add(new MorfeoComponentColumn("Tipo file", MyStandardConstants.PREFIX_MYSTD + MyStandardConstants.ATTACHMENT_FILE_TYPE_COLUMN_KEY, null));
        columns.add(new MorfeoComponentColumn("Nome Allegato", MyStandardConstants.PREFIX_MYSTD + MyStandardConstants.ATTACHMENT_NAME_COLUMN_KEY, null));
        columns.add(new MorfeoComponentColumn("Descrizione", MyStandardConstants.PREFIX_MYSTD + MyStandardConstants.ATTACHMENT_DESCRIPTION_COLUMN_KEY, null));
        columns.add(new MorfeoComponentColumn("Data caricamento", "https://w3id.org/italia/onto/TI/" + MyStandardConstants.ATTACHMENT_DATE_COLUMN_KEY, null));

        List<MorfeoComponentButton> button = new ArrayList<>();
        if (readOnly) {
            button.add(new MorfeoComponentButton("Azioni", "file_download", "link", "primary", "icon"));
            columns.add(new MorfeoComponentColumn("Azioni", "link", button));
        } else {
            button.add(new MorfeoComponentButton("Azioni", "edit", "edit", "primary", "icon"));
            button.add(new MorfeoComponentButton("Azioni", "delete", "delete", "primary", "icon"));
            columns.add(new MorfeoComponentColumn("Azioni", "action", button));
        }

        columns.add(new MorfeoComponentColumn("custom_ind_name", "custom_ind_name", null));
        columns.add(new MorfeoComponentColumn("_entityPropertyLocalName", "_entityPropertyLocalName", null));
        columns.add(new MorfeoComponentColumn("_entityPropertyIRI", "_entityPropertyIRI", null));
        columns.add(new MorfeoComponentColumn("_targetIndividualIRI", "_targetIndividualIRI", null));
        columns.add(new MorfeoComponentColumn("id_documento", MyStandardConstants.PREFIX_MYSTD + MyStandardConstants.ATTACHMENT_ID_DOCUMENTO_COLUMN_KEY, null));
        columns.add(new MorfeoComponentColumn("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", null));

        return columns;
    }


    /**
     * Metodo per la creazione di column components (versione repeatable ma readonly)
     * @param oModel, struttura dei dati
     * @param objProp, struttura della object property
     * @return
     */
    private List<MorfeoComponent> getReadonlyColumnsComponents(OModel oModel, OProperty objProp, Boolean readonly)  {
        List<MorfeoComponent> columnsComponents = new ArrayList<>();
        String rangeClassIRI = objProp.getRangeClassIRI();
        if (StringUtils.hasText(rangeClassIRI)) {
            Map<String, OClass> classes = oModel.getClasses();
            if (classes.containsKey(rangeClassIRI)) {//Si cerca la classe nel modello
                OClass rangeClass = oModel.getClass(rangeClassIRI);
                Map<String, OProperty> rangeDataProperties = rangeClass.getDataProperty();
                if (rangeDataProperties != null && rangeDataProperties.size() > 0) {//Si estraggono le data property della classe
                    for (Map.Entry<String, OProperty> entry : rangeDataProperties.entrySet()) {

                        //Si aggiunge component per data property
                        OProperty dataProp = entry.getValue();
                        MorfeoComponent newComponent = new MorfeoComponent();
                        MorfeoComponent newChildComponent = new MorfeoComponent();


                        newChildComponent.setDomain(objProp.getLocalName());
                        newChildComponent.setKey(MyStandardUtil.mystandardPrefixForMorfeo(objProp.getLocalName(), dataProp.getLocalName()) );
                        newChildComponent.setOriginalKey(dataProp.getIRI());
                        newChildComponent.setLabel(dataProp.getLabel());
                        if (MyStandardConstants.SELECT_TYPE.equals(dataProp.getType())) {
                            setMorfeoComponentAsSelect(dataProp, newChildComponent);
                        } else if (MyStandardConstants.MULTIPLE_SELECT_TYPE.equals(dataProp.getType())) {
                            setMorfeoComponentAsMultipleSelect(dataProp, newChildComponent);
                        } else  {
                            try {
                                newChildComponent.setType(DataTypeEnum.of(dataProp.getType()).getMorfeoType());
                            } catch (MyStandardException e) {
                                newChildComponent.setType(DataTypeEnum.STRING.getMorfeoType());
                            }

                        }
                        newChildComponent.setHidden(fieldMustBeHidden(dataProp.getHidden(), dataProp.getVisibleOnlyAuthenticated()));
                        newChildComponent.setOrder(dataProp.getOrder());
                        newChildComponent.setValidate(getMorfeoValidationFromProperty(dataProp.getValidation(), MyStandardUtil.mystandardPrefixForMorfeo(objProp.getLocalName(), dataProp.getLocalName()) ));
                        newChildComponent.setConditional(getMorfeoConditionalValidation(dataProp.getValidation()));

                        newChildComponent.setReadOnly(readonly);

                        //Se sono proprietà, dove la obj prop prende info da vocabolario, le data prop devono essere readonly
                        if (MyStandardConstants.VOCABULARY_TYPE.equals(objProp.getType())) {
                            newChildComponent.setAlwaysReadOnly(true);
                        }

                        newComponent.setHidden(fieldMustBeHidden(dataProp.getHidden(), dataProp.getVisibleOnlyAuthenticated()));

                        List<MorfeoComponent> tmpArray = new ArrayList<>();
                        tmpArray.add(newChildComponent);
                        newComponent.setComponents(tmpArray);
                        columnsComponents.add(newComponent);
                    }

                    try {
                        columnsComponents.sort(Comparator.comparing(a -> a.getComponents().get(0).getOrder()));//Si ordinano
                    } catch (Exception e) {
                        LOGGER.info("Errore nel sorting delle colonne per allegato. Errore non bloccante");
                    }

                    if (MyStandardConstants.VOCABULARY_TYPE.equals(objProp.getType()) && !readonly) {
                        //Bisogna aggiungere un pulsante che apre una modale per visualizzare dati vocabolari

                        MorfeoComponent newComponent = new MorfeoComponent();
                        MorfeoComponent vocabularyButton = createMorfeoButton(objProp.getLocalName(), null, "vocabulary", MyStandardConstants.VISUALIZZA_DATI_BUTTON_LABEL);
                        addComponentInComponentList(newComponent, vocabularyButton, columnsComponents);
                    }


                    //Componente per gestire il nome dell'individual
                    MorfeoComponent customIndName = new MorfeoComponent();
                    customIndName.setKey(MyStandardConstants.CUSTOM_IND_NAME);
                    customIndName.setOriginalKey(MyStandardConstants.CUSTOM_IND_NAME);
                    customIndName.setType(DataTypeEnum.STRING.getMorfeoType());
                    customIndName.setReadOnly(true);
                    customIndName.setHidden(true);


                    customIndName.setDomain(objProp.getLocalName());
                    MorfeoComponent newComponent = new MorfeoComponent();
                    newComponent.setHidden(true);
                    addComponentInComponentList(newComponent, customIndName, columnsComponents);

                } else {//Nessun subfield
                    MorfeoComponent newComponent = new MorfeoComponent();
                    MorfeoComponent newChildComponent = new MorfeoComponent();
                    newChildComponent.setDomain(objProp.getLabel());//Set label perchè dominio image logo da problemi
                    newChildComponent.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null, objProp.getLocalName()));
                    newChildComponent.setOriginalKey(objProp.getIRI());
                    newChildComponent.setLabel(objProp.getLabel());
                    if (MyStandardConstants.SELECT_TYPE.equals(objProp.getType())) {
                        setMorfeoComponentAsSelect(objProp, newChildComponent);
                    } else if (MyStandardConstants.MULTIPLE_SELECT_TYPE.equals(objProp.getType())) {
                        setMorfeoComponentAsMultipleSelect(objProp, newChildComponent);
                    } else  {
                        try {
                            newChildComponent.setType(DataTypeEnum.of(objProp.getType()).getMorfeoType());
                        } catch (MyStandardException e) {
                            newChildComponent.setType(DataTypeEnum.STRING.getMorfeoType());
                        }
                    }

                    newChildComponent.setHidden(fieldMustBeHidden(objProp.getHidden(), objProp.getVisibleOnlyAuthenticated()));

                    newChildComponent.setOrder(objProp.getOrder());
                    newChildComponent.setValidate(getMorfeoValidationFromProperty(objProp.getValidation(), MyStandardUtil.mystandardPrefixForMorfeo(null, objProp.getLocalName()) ));
                    newChildComponent.setConditional(getMorfeoConditionalValidation(objProp.getValidation()));

                    newChildComponent.setReadOnly(readonly);
                    newComponent.setHidden(fieldMustBeHidden(objProp.getHidden(), objProp.getVisibleOnlyAuthenticated()));


                    List<MorfeoComponent> tmpArray = new ArrayList<>();
                    tmpArray.add(newChildComponent);
                    newComponent.setComponents(tmpArray);
                    columnsComponents.add(newComponent);

                    MorfeoComponent customIndName = new MorfeoComponent();
                    customIndName.setKey(MyStandardConstants.CUSTOM_IND_NAME);
                    customIndName.setOriginalKey(MyStandardConstants.CUSTOM_IND_NAME);
                    customIndName.setType(DataTypeEnum.STRING.getMorfeoType());
                    customIndName.setReadOnly(true);
                    customIndName.setHidden(true);
                    customIndName.setDomain(objProp.getLabel());

                    MorfeoComponent customNewComponent = new MorfeoComponent();
                    customNewComponent.setHidden(true);
                    addComponentInComponentList(customNewComponent, customIndName, columnsComponents);
                }



            } else {
                LOGGER.error("Il modello non contiene informazioni sulla classe " + rangeClassIRI);
            }
        }

        return columnsComponents;
    }

    /**
     * Aggingi componente facente parte nella component list di un componente vuoto
     * @param component, component da aggiungere
     * @param columnsComponents, lista components
     */
    private void addComponentInComponentList(MorfeoComponent newComponent, MorfeoComponent component, List<MorfeoComponent> columnsComponents) {
        List<MorfeoComponent> tmpArray = new ArrayList<>();
        tmpArray.add(component);
        newComponent.setComponents(tmpArray);
        columnsComponents.add(newComponent);
    }

    /**
     * Si crea una lista di componenti adatti per repeatable con alcune specificità per allegato
     * @param oModel, model con la struttura dati
     * @param objProp, object property
     * @param readonly, indica se readonly
     * @return
     */
    private List<MorfeoComponent> getAllegatoRepeatableComponents(OModel oModel, OProperty objProp, Boolean readonly) {
        List<MorfeoComponent> attachmentComponents = new ArrayList<>();
        MorfeoComponent attachmentComponent = new MorfeoComponent();//Creazione componente padre repeatable
        attachmentComponent.setDomain(MyStandardConstants.ALLEGATI_DOMAIN);
        String attachmentKey = objProp.getIRI() + "_attachemtnComponent";
        attachmentComponent.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null, objProp.getLocalName()));
        attachmentComponent.setOriginalKey(attachmentKey);
        attachmentComponent.setType(MorfeoTypeEnum.COLUMNS.getCode());
        attachmentComponent.setReadOnly(readonly);
        attachmentComponent.setDataSrc(MyStandardConstants.MORFEO_DATASRC_VALUES);


        String rangeClassIRI = objProp.getRangeClassIRI();
        Map<String, OClass> classes = oModel.getClasses();
        if (classes.containsKey(rangeClassIRI)) {//Si cerca se nel modello c'è la classe da cui ricavare le proprietà
            OClass rangeClass = oModel.getClass(rangeClassIRI);
            Map<String, OProperty> rangeDataProperties = rangeClass.getDataProperty();
            if (rangeDataProperties != null) {//Se la classe ha data property
                List<MorfeoComponent> columnsComponents = new ArrayList<>();
                for (Map.Entry<String, OProperty> entry : rangeDataProperties.entrySet()) {
                    //Creazione componente per dataproperty
                    OProperty dataProp = entry.getValue();

                    MorfeoComponent newComponent = new MorfeoComponent();
                    newComponent.setReadOnly(readonly);
                    MorfeoComponent newChildComponent = new MorfeoComponent();
                    newChildComponent.setReadOnly(readonly);
                    newChildComponent.setDomain(MyStandardConstants.ALLEGATI_DOMAIN);
                    newChildComponent.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null, dataProp.getLocalName()));
                    newChildComponent.setOriginalKey(dataProp.getIRI());
                    newChildComponent.setLabel(dataProp.getLabel());
                    newChildComponent.setType(DataTypeEnum.LITERAL.getMorfeoType());
                    newChildComponent.setHidden(fieldMustBeHidden(dataProp.getHidden(), dataProp.getVisibleOnlyAuthenticated()));

                    newChildComponent.setOrder(dataProp.getOrder());

                    addComponentInComponentList(newComponent, newChildComponent, columnsComponents);

                }

                try {
                    columnsComponents.sort(Comparator.comparing(a -> a.getComponents().get(0).getOrder()));//Si ordinano
                } catch (Exception e) {
                    LOGGER.info("Errore nel sorting delle colonne per allegato. Errore non bloccante");
                }

                MorfeoComponent customIndName = new MorfeoComponent();
                customIndName.setKey(MyStandardConstants.CUSTOM_IND_NAME);
                customIndName.setOriginalKey(MyStandardConstants.CUSTOM_IND_NAME);
                customIndName.setType(DataTypeEnum.STRING.getMorfeoType());
                customIndName.setReadOnly(true);
                customIndName.setHidden(true);
                customIndName.setDomain(MyStandardConstants.ALLEGATI_DOMAIN);

                MorfeoComponent newComponent = new MorfeoComponent();
                newComponent.setHidden(true);
                addComponentInComponentList(newComponent, customIndName, columnsComponents);

                attachmentComponent.setColumns(columnsComponents);
                attachmentComponents.add(attachmentComponent);


            }
        }
        return attachmentComponents;
    }


    /**
     * Si impostano i dati degli allegati
     * @param morfeoFormAttachmentFields, struttura per allegati per morfeo
     * @param entityData, entità di cui prendere gli allegati da mostrare al dettaglio
     * @param owlPrefix, prefisso entità
     * @param readOnly , indica se la struttura è in modalità readonly
     */
    private void setDataIntoAttachmentFields(MorfeoElement morfeoFormAttachmentFields, JSONObject entityData, String owlPrefix, Boolean readOnly) {
        List<MorfeoComponent> newAttachmentFieldsComponentsList = new ArrayList<>();
        List<MorfeoComponent> attachmentsComponents = morfeoFormAttachmentFields.getComponents();

        Set<String> propsToFilter = new HashSet<>();
        MorfeoComponent component = morfeoFormAttachmentFields.getComponents().get(0);
        List<MorfeoComponent> entitiesWithProperties = readOnly ? component.getColumns() : component.getComponents().get(0).getColumns();

        for (MorfeoComponent entity: entitiesWithProperties) {
            String key = entity.getComponents().get(0).getOriginalKey();
            propsToFilter.add(key);
        }

        Iterator<String> entityDataKeys = entityData.keys();
        while (entityDataKeys.hasNext()) {
            String key = entityDataKeys.next();
            if (key.equals(owlPrefix + MyStandardConstants.ALLEGATI_DOMAIN)) {
                try {
                    JSONObject allegatiObject = entityData.getJSONObject(key);
                    JSONArray attachmentsList = allegatiObject.getJSONArray(MyStandardConstants.COMPONENTS_KEY);
                    for (int i = 0; i < attachmentsList.length(); i++) {
                        JSONObject attachment = attachmentsList.getJSONObject(i);
                        Iterator<String> attachmentKeys = attachment.keys();
                        List<String> propsToDelete = new ArrayList<>();
                        while (attachmentKeys.hasNext()) {
                            String attachmentKey = attachmentKeys.next();
                            if (!propsToFilter.contains(attachmentKey)) {
                                propsToDelete.add(attachmentKey);
                            }
                        }
                        for (String prop : propsToDelete) {
                            attachment.remove(prop);
                        }
                    }
                } catch (JSONException e) {
                    LOGGER.error("Errore nel parsing di oggetti allegati", e);
                }
            }
        }

        if (readOnly) {

            if (attachmentsComponents != null && attachmentsComponents.size() > 0) {//Ci sono allegati

                MorfeoComponent attachmentStructure = attachmentsComponents.get(0);//Prendo la struttura che la clono per ogni allegato

                String allegatiKey = attachmentStructure.getOriginalKey();

                try {
                    JSONObject allegatiObject = entityData.getJSONObject(allegatiKey);

                    if (allegatiObject.has(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_IRI) ) {//Set Key with object property IRI
                        attachmentStructure.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null, allegatiObject.get(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_LOCAL_NAME).toString()));
                        attachmentStructure.setOriginalKey(allegatiObject.get(MyStandardConstants.OBJ_PROPERTY_ENTITY_PROPERTY_IRI).toString());

                    }

                    JSONArray allegatiList = allegatiObject.getJSONArray(MyStandardConstants.COMPONENTS_KEY);

                    for (int i = 0, size = allegatiList.length(); i < size; i++) {//Creazione di ogni singolo allegato
                        JSONObject allegato = allegatiList.getJSONObject(i);
                        JSONObject allegatoObj = allegato.getJSONObject(owlPrefix + MyStandardConstants.ID_DOCUMENTO_COLUMN_KEY);
                        String idDoc = allegatoObj.getString(MyStandardConstants.DATA_PROPERTY_VALUE);

                        MorfeoComponent attachmentComponent = new MorfeoComponent(attachmentStructure);//Clonazione
                        attachmentComponent.setKey(MyStandardConstants.ALLEGATI_KEY_PREFIX + idDoc);
                        attachmentComponent.setOriginalKey(MyStandardConstants.ALLEGATI_KEY_PREFIX + idDoc);

                        Iterator<String> keys = allegato.keys();
                        Integer counter = 1;
                        String tipoFile = null;

                        while (keys.hasNext()) {//Ciclo su tutti gli elementi contenenti i dati dell'allegato

                            String key = keys.next();
                            List<MorfeoComponent> attachmentColumns = attachmentComponent.getColumns();

                            for (int j = 0; j <  attachmentColumns.size(); j++) {//Si crea column per ogni key
                                MorfeoComponent column = attachmentColumns.get(j).getComponents().get(0);
                                if (column.getOriginalKey().equals(key)) {
                                    Object allegatoObject = allegato.get(key);
                                    String allegatoKeyValue;
                                    if (allegatoObject instanceof JSONObject) {
                                        JSONObject allegatoKey = allegato.getJSONObject(key);
                                        allegatoKeyValue = allegatoKey.getString(MyStandardConstants.DATA_PROPERTY_VALUE);
                                    } else {
                                        allegatoKeyValue = allegatoObject != null ? allegatoObject.toString() : "";
                                    }


                                    column.setDefaultValue(allegatoKeyValue);
                                    column.setKey(MyStandardConstants.ALLEGATI_KEY_PREFIX + idDoc + ":" + counter.toString());
                                    column.setOriginalKey(MyStandardConstants.ALLEGATI_KEY_PREFIX + idDoc + ":" + counter.toString());
                                    if (key.equals(owlPrefix + MyStandardConstants.TIPO_FILE_COLUMN_KEY)) {

                                        tipoFile = allegatoKeyValue;
                                    }
                                }
                            }
                            counter++;
                        }

                        if (tipoFile != null) {
                            attachmentComponent.getColumns().add(createMorfeoFileTypeButton(idDoc, getIconName(tipoFile)));
                        }

                        newAttachmentFieldsComponentsList.add(attachmentComponent);

                    }
                } catch (JSONException e) {
                    LOGGER.debug("Nessun allegato per l'entità", e);
                } catch (Exception e) {
                    LOGGER.debug("Errore generico:Nessun allegato per l'entità", e);
                }
                morfeoFormAttachmentFields.setComponents(newAttachmentFieldsComponentsList);
            }

            // Morfeo repetable component
        } else {
            String allegatiKey = attachmentsComponents.get(0).getOriginalKey();

            MorfeoComponentData attachmentsData = new MorfeoComponentData();
            List<Map<String, Object>> attachmentsValues = new ArrayList<>();
            attachmentsData.setColumns(null);
            attachmentsData.setValues(attachmentsValues);
            attachmentsData.setPagination(null);

            try {
                JSONObject allegatiObject = entityData.getJSONObject(allegatiKey);
                JSONArray allegatiList = allegatiObject.getJSONArray(MyStandardConstants.COMPONENTS_KEY);

                Integer counter = 1;

                for (int i = 0, size = allegatiList.length(); i < size; i++) {

                    Map<String, Object> dataMap = new HashMap<>();
                    Iterator<String> keys = allegatiList.getJSONObject(i).keys();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value;
                        String originalKey;

                        if (MyStandardConstants.CUSTOM_IND_NAME.equals(key)) {
                            originalKey = key;
                            value = (String) allegatiList.getJSONObject(i).get(key);
                        } else {
                            JSONObject json = (JSONObject) allegatiList.getJSONObject(i).get(key);
                            value = json.getString(MyStandardConstants.DATA_PROPERTY_VALUE);
                            originalKey = MyStandardUtil.mystandardPrefixForMorfeo(null, json.getString(MyStandardConstants.DATA_PROPERTY_LOCAL_NAME) );
                        }

                        if (value != null) {
                            dataMap.put(originalKey, value);
                        }

                    }

                    counter++;
                    attachmentsValues.add(dataMap);

                }

                morfeoFormAttachmentFields.getComponents().get(0).setData(attachmentsData);

            } catch (JSONException e) {
                LOGGER.debug("Nessun allegato per l'entità");
            }

        }

    }

    /**
     * Set morfeo detail as readonly
     * @param myStandardMorfeoDetail, morfeo detail
     * @param readOnly, radonly value
     */
    private void setMyStandardMorfeoDetailReadOnlyProp(MyStandardMorfeoDetail myStandardMorfeoDetail, Boolean readOnly) {

        List<MorfeoElement> elements = myStandardMorfeoDetail.getAllElements();

        for (MorfeoElement element: elements) {
            if (element != null) {
                for (MorfeoComponent component : element.getComponents()) {
                    if (component != null) {
                        component.setReadOnly(component.getAlwaysReadOnly() ? true : readOnly);//Se always readonly, il Boolean readonly non deve essere considerato
                    }
                }
            }
        }
    }

    /**
     * Set stato inserito
     * @param masterFields
     */
    private void setInsertedValueOnState(MorfeoElement masterFields, String owlPrefix) {
        List<MorfeoComponent> components = masterFields.getComponents();
        for (MorfeoComponent component : components) {
            List<MorfeoComponent> subComponents = component.getComponents();
            for (MorfeoComponent subComponent : subComponents) {
                List<MorfeoComponent> columns = subComponent.getColumns();
                for (MorfeoComponent column : columns) {
                    List<MorfeoComponent> columnComponents = column.getComponents();
                    for (MorfeoComponent columnComponent : columnComponents) {
                        List<MorfeoComponent> subColumns = columnComponent.getColumns();
                        if (subColumns != null) {
                            for (MorfeoComponent subColumn : subColumns) {
                                List<MorfeoComponent> leafs = subColumn.getComponents();
                                for (MorfeoComponent leaf : leafs) {
                                    if ((owlPrefix + MyStandardConstants.STATO_OBJPROP_KEY).equals(leaf.getOriginalKey())) {
                                        leaf.setDefaultValue(owlPrefix + MyStandardStatoEnum.INSERITO.getCode());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Set alcune proprietà come readonly
     * @param masterFields
     */
    private void setReadOnlyOnNewMasterFieldsDataProperties(MorfeoElement masterFields, String owlPrefix) {
        // Set hidden e readOnly per IdEntita, Versione, Stato
        List<MorfeoComponent> components = masterFields.getComponents();
        for (MorfeoComponent component : components) {
            List<MorfeoComponent> subComponents = component.getComponents();
            for (MorfeoComponent subComponent : subComponents) {
                List<MorfeoComponent> columns = subComponent.getColumns();
                for (MorfeoComponent column : columns) {
                    List<MorfeoComponent> columnComponents = column.getComponents();
                    for (MorfeoComponent columnComponent : columnComponents) {
                        if (columnComponent.getOriginalKey() != null) {
                            if (columnComponent.getOriginalKey().equals(owlPrefix + MyStandardConstants.ID_ENTITA_COLUMN_KEY)) {
                                columnComponent.setHidden(true);
                            }
                            if (columnComponent.getOriginalKey().equals(owlPrefix + MyStandardConstants.VERSIONE_COLUMN_KEY)) {
                                columnComponent.setReadOnly(true);
                            }
                            if (columnComponent.getOriginalKey().equals(owlPrefix + MyStandardConstants.STATO_OBJPROP_KEY)) {
                                columnComponent.setReadOnly(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Setting datatable dentro i tabs
     * @param tabsFields
     */
    private void setDataTablesInTabs(MorfeoElement tabsFields) {

        if (
                tabsFields.getComponents() != null &&
                        tabsFields.getComponents().get(0) != null &&
                        tabsFields.getComponents().get(0).getComponents() != null &&
                        tabsFields.getComponents().get(0).getComponents().get(1) != null &&
                        tabsFields.getComponents().get(0).getComponents().get(1).getComponents() != null &&
                        tabsFields.getComponents().get(0).getComponents().get(1).getComponents().get(0) != null &&
                        tabsFields.getComponents().get(0).getComponents().get(1).getComponents().get(0).getComponents() != null
        ) {

            List<MorfeoComponent> relationsTabsA = tabsFields.getComponents().get(0).getComponents().get(1).getComponents();
            MorfeoComponent relationsTabsB = tabsFields.getComponents().get(0).getComponents().get(1).getComponents().get(0);
            List<MorfeoComponent> relationsTabs = tabsFields.getComponents().get(0).getComponents().get(1).getComponents().get(0).getComponents();
            //Si cicla sulle relazioni
            for (MorfeoComponent relationsTab: relationsTabs) {

                List<MorfeoComponent> components = new ArrayList<>();

                String key = relationsTab.getOriginalKey();
                String label = relationsTab.getLabel();
                String range = relationsTab.getRange();

                //Creazione component datatable
                MorfeoComponent datatable = new MorfeoComponent();
                datatable.setKey(MyStandardConstants.DATATABLE_KEY_PREFIX + label);
                datatable.setOriginalKey(MyStandardConstants.DATATABLE_KEY_PREFIX + label);
                datatable.setType(MorfeoTypeEnum.DATATABLE.getCode());
                datatable.setReadOnly(true);
                datatable.setDataSrc(MyStandardConstants.MORFEO_DATASRC_VALUES);

                //Creazione di un component container che contiene datatable e pulsante nuova relazione
                MorfeoComponent containerComp = new MorfeoComponent();
                containerComp.setKey(MyStandardConstants.TAB_RELATION_PREFIX_KEY + key);
                containerComp.setOriginalKey(MyStandardConstants.TAB_RELATION_PREFIX_KEY + key);
                containerComp.setLabel(label);
                containerComp.setRange(range);
                List<MorfeoComponent> childComps = new ArrayList<>();
                containerComp.setComponents(childComps);

                //Pulsante nuova relazione
                if (StringUtils.hasText(relationsTab.getInverse()) && !activeSpecialRelationship(key)) {//Solo se esiste una inversa lo mostriamo
                    MorfeoComponent addRelationshipButton = new MorfeoComponent();
                    addRelationshipButton.setType(MorfeoTypeEnum.BUTTON.getCode());
                    addRelationshipButton.setLabel(MyStandardConstants.NEW_RELATIONSHIP);
                    addRelationshipButton.setKey(MyStandardConstants.NEW_RELATIONSHIP_BUTTON_PREFIX + key);
                    addRelationshipButton.setOriginalKey(MyStandardConstants.NEW_RELATIONSHIP_BUTTON_PREFIX + key);
                    addRelationshipButton.setDataSrc("values");
                    addRelationshipButton.setReadOnly(false);
                    childComps.add(addRelationshipButton);
                }

                MorfeoComponent tmpComponent = createMorfeoComponent(MyStandardConstants.TAB_RELATION_PREFIX_KEY + key, label, Collections.singletonList(datatable));

                childComps.add(tmpComponent.getComponents().get(0));
                components.add(containerComp);
                relationsTab.setComponents(components);

                //Creazione datatable vuoto
                MorfeoComponentData datatableData = new MorfeoComponentData(
                        createDatatableColumns(false, true, null),
                        null,
                        createTablePagination()
                );
                datatable.setData(datatableData);
            }
        }

    }


    /**
     * Si verifica se il componente è stato definito come un custom_repeatable
     * @param columnsDetail, struttura owl
     * @return true se custom ripetibile, false altrimenti
     */
    private Boolean hasEntityProperty(List<OProperty> columnsDetail) {
        Boolean result = false;
        for (OProperty columnDetail : columnsDetail) {
            if (columnDetail.getType() != null && columnDetail.getType().equals(MyStandardConstants.ENTITY_PROPERTY_TYPE)) {
                return true;
            }
        }
        return result;
    };

    /**
     * Si verifica se il componente è stato definito come un custom_repeatable
     * @param columnsDetail, struttura owl
     * @return true se custom ripetibile, false altrimenti
     */
    private Boolean hasVocabulary(List<OProperty> columnsDetail) {
        Boolean result = false;
        for (OProperty columnDetail : columnsDetail) {
            if (columnDetail.getType() != null && columnDetail.getType().equals(MyStandardConstants.VOCABULARY_TYPE)) {
                return true;
            }
        }
        return result;
    };




    /**
     * Inserimento title components
     * @param components, components a cui inserire il title
     * @param columnsDetail, struttura owl
     * @return list with title components
     */
    private List<MorfeoComponent> insertTitleComponentsProperty(List<MorfeoComponent> components, List<OProperty> columnsDetail) {
        Map<String, Map<String, Object>> customRepeatables = mapCustomRepeatablesOrVocabularyProperty(columnsDetail);
        List<MorfeoComponent> componentsWithTitles = new ArrayList<>();
        if (components != null && components.get(0) != null
                && components.get(0).getColumns() != null
                && components.get(0).getColumns().get(0) != null
                && components.get(0).getColumns().get(0).getComponents() != null) {
            for (MorfeoComponent component : components.get(0).getColumns().get(0).getComponents()) {
                String key = component.getOriginalKey();
                Map<String, Object> titles = customRepeatables.get(key);
                Boolean hidden = component.getHidden();
                if (key != null && titles != null && !hidden) {
                    MorfeoComponent title = new MorfeoComponent();
                    title.setType("columns");
                    title.setKey("title_" + key);
                    title.setOriginalKey("title_" + key);
                    MorfeoComponent htmlComponent = new MorfeoComponent();
                    htmlComponent.setKey("html_" + key);
                    htmlComponent.setOriginalKey("html_" + key);
                    htmlComponent.setType("htmlelement");
                    htmlComponent.setHtml((String)titles.get("html"));
                    htmlComponent.setTag((String)titles.get("tag"));
                    List<MorfeoComponent> htmlComponents = new ArrayList<>();
                    htmlComponents.add(htmlComponent);
                    MorfeoComponent wrapper = new MorfeoComponent();
                    wrapper.setComponents(htmlComponents);
                    List<MorfeoComponent> wrappedColumns = new ArrayList<>();
                    wrappedColumns.add(wrapper);
                    title.setColumns(wrappedColumns);
                    componentsWithTitles.add(title);
                }
                componentsWithTitles.add(component);
            }
            components.get(0).getColumns().get(0).setComponents(componentsWithTitles);
        }

        return components;
    }


    /**
     * Creazione di un container dal domain, contenente dei componenti
     * @param domain, domain del container
     * @param components, lista dei componenti
     * @return container
     */
    private MorfeoComponent getMorfeoComponentContainer(String domain, List<MorfeoComponent> components, Boolean readOnly) {
        MorfeoComponent container = new MorfeoComponent();
        container.setComponents(components);
        container.setLabel(messageSource.getMessage(domain, null, domain, null));
        container.setType(MorfeoTypeEnum.CONTAINER.getCode());
        container.setKey(domain);
        container.setOriginalKey(domain);

        return container;
    }

    private void setReadOnlyProprertyOnContainers(List<MorfeoComponent> containers, Boolean readOnly) {

        for (MorfeoComponent container : containers) {

            List<MorfeoComponent> containerComponents = container.getComponents();
            MorfeoComponent columnsComponent = containerComponents != null ? containerComponents.get(0) : null;
            List<MorfeoComponent> columnComponents = columnsComponent != null ? columnsComponent.getColumns() : null;
            MorfeoComponent columnComponent = columnComponents != null ? columnComponents.get(0) : null;
            List<MorfeoComponent> components = columnComponent != null ? columnComponent.getComponents() : null;

            for (MorfeoComponent component : components) {
                String componentType = component.getType().toString();
                String columnsType = MorfeoTypeEnum.COLUMNS.getCode().toString();
                component.setReadOnly(component.getAlwaysReadOnly() ? true : readOnly);//Se always readonly, il Boolean readonly non deve essere considerato


                if (componentType.equals(columnsType)) {

                    List<MorfeoComponent> lineColumns = component.getColumns();

                    for (MorfeoComponent lineColumn : lineColumns) {
                        List<MorfeoComponent> lineColumnComps = lineColumn.getComponents();
                        MorfeoComponent lineColumnComp = lineColumnComps != null ? lineColumnComps.get(0) : null;
                        lineColumnComp.setReadOnly(lineColumnComp.getAlwaysReadOnly() ? true : readOnly);//Se always readonly, il Boolean readonly non deve essere considerato

                    }

                }

            }

        }

    }

    /**
     * Set dati e creazione cloni in relazione alla lista elementi ricevuti
     * @param masterFieldSubComponentColumnsComponents, componente a cui settare ed aggiungere eventuali molteplici istanze
     * @param index, indice a cui aggiungere elemento per mostrarlo in ordine
     * @param masterFieldSubComponentColumnsComponent, componente da clonare
     * @param arrayIndex, indice componenta da utilizzare per clonazione
     * @param obj, oggetto json contenente i dati
     */
    private void setSubFieldsDataForObject(List<MorfeoComponent> masterFieldSubComponentColumnsComponents, int index, MorfeoComponent masterFieldSubComponentColumnsComponent, int arrayIndex, JSONObject obj, Boolean readOnly) {

        //Set readonly true or false
        for (MorfeoComponent component : masterFieldSubComponentColumnsComponents) {
            component.setReadOnly(component.getAlwaysReadOnly() ? true : readOnly);
            List<MorfeoComponent> columns = component.getColumns();
            if (columns != null) {
                for (MorfeoComponent column : columns) {
                    column.setReadOnly(column.getAlwaysReadOnly() ? true : readOnly);
                    List<MorfeoComponent> columnComponents = column.getComponents();
                    if (columnComponents != null) {
                        for (MorfeoComponent columnComponent : columnComponents) {
                            columnComponent.setReadOnly(columnComponent.getAlwaysReadOnly() ? true : readOnly);
                        }
                    }
                }
            }
        }

        //Creazione di un nuovo oggetto struttura su cui settare i dati
        //Il primo avrà key classica, dal secondo avrà key con suffisso <_index>

        MorfeoComponent column = getComponentByIndex(arrayIndex, masterFieldSubComponentColumnsComponent);
        List<MorfeoComponent> columns = column.getColumns();
        Integer numberOfColumns = columns.size();
        for (MorfeoComponent singleColumn : columns) {
            List<MorfeoComponent> components = singleColumn.getComponents();
            for (MorfeoComponent component : components) {
                String componentKey = component.getOriginalKey();//Nome del subField
                if (StringUtils.hasText(componentKey)) {
                    if (obj.has(componentKey)) {//Se
                        // il json ha il subfield, lo setto (oggetto struttura è quello iniziale, senza <_index>

                        Object componentObject = obj.get(componentKey);
                        if (componentObject instanceof JSONObject) {
                            JSONObject object = obj.getJSONObject(componentKey);
                            String dataPropertyValue = String.valueOf(object.get(MyStandardConstants.DATA_PROPERTY_VALUE));

                            component.setDefaultValue(dataPropertyValue);
                        } else {
                            component.setDefaultValue(obj.getString(componentKey));
                        }

                    } else if (obj.has(componentKeyWithoutIndex(componentKey, arrayIndex))) {//SI rimuove index dal component per vedere se c'è nel JSON

                        Object componentObject = obj.get(componentKey);
                        if (componentObject instanceof JSONObject) {
                            JSONObject object = obj.getJSONObject(componentKeyWithoutIndex(componentKey, arrayIndex));
                            String dataPropertyValue = String.valueOf(object.get(MyStandardConstants.DATA_PROPERTY_VALUE));

                            component.setDefaultValue(dataPropertyValue);
                        } else {
                            component.setDefaultValue(obj.getString(componentKeyWithoutIndex(componentKey, arrayIndex)));
                        }

                    } else if (obj.has(MyStandardConstants.FOAF_NAME)) {//Il valore potrebbe non essere definito come key, ma come rdf:type

                        Object componentObject = obj.get(MyStandardConstants.FOAF_NAME);
                        if (componentObject instanceof JSONObject) {
                            JSONObject object = obj.getJSONObject(MyStandardConstants.FOAF_NAME);
                            String dataPropertyValue = String.valueOf(object.get(MyStandardConstants.DATA_PROPERTY_VALUE));

                            component.setDefaultValue(dataPropertyValue);
                        } else {
                            component.setDefaultValue(obj.getString(MyStandardConstants.FOAF_NAME));
                        }
                    }
                }

                if (component.getDefaultValue() != null && numberOfColumns == 1) {
                    component.setLabel(null);//Se una colonna sola, non si vuole far vedere la label visto che c'è già il titolo
                }
            }

        }

        correctTypeForWebsites(column);
        if (arrayIndex > 0) {//Se non è il primo elemento (quello struttura) allora aggiungo il component alla lista
            masterFieldSubComponentColumnsComponents.add(++index, column);
        }
    }


    /**
     * Si creano tab storico e relazioni
     * @param oModel, modello dell'entità
     * @param entityType, tipo entità
     * @param owlPrefix prefisso owl
     * @return
     * @throws MyStandardException
     */
    private List<MorfeoComponent> getMorfeoTabsComponents(OModel oModel, String entityType, String owlPrefix) throws MyStandardException {

        List<MorfeoComponent> componentsTabs = new ArrayList<>();

        //Creazione component per tab storico
        componentsTabs.add(createMorfeoComponent(MyStandardConstants.HISTORICAL_TAB_KEY, MyStandardConstants.HISTORICAL_TAB_LABEL, Collections.singletonList(new MorfeoComponent())));

        //Creazione component per tab relazioni
        MorfeoComponent componentRelazioni = new MorfeoComponent();
        componentRelazioni.setKey("tabs_2_B");
        componentRelazioni.setOriginalKey("tabs_2_B");
        componentRelazioni.setType(MorfeoTypeEnum.TABS.getCode());


        //Creazione dei tabs
        Map<String, ORelation> relations = oModel.getRelations();
        Map<String, OClass> classes = oModel.getClasses();
        if (classes.containsKey(owlPrefix + entityType)) {
            OClass entityClass = classes.get(owlPrefix + entityType);

            List<MorfeoComponent> tabsDetail = new ArrayList<>();
            List<String> classRelations = entityClass.getRelations();
            for (String relationKey: classRelations) {
                if (relations.containsKey(relationKey)) {
                    ORelation classRelation = relations.get(relationKey);
                    MorfeoComponent tabDetail = getMorfeoComponentFromRelation(classRelation);
                    tabsDetail.add(tabDetail);
                } else {
                    LOGGER.error("La relazione " + relationKey + " non contenuta nella lista relazioni");
                }
            }

            tabsDetail.sort(Comparator.comparing(a -> a.getOrder()));

            componentRelazioni.setComponents(tabsDetail);


            //Creazione tab relazionio
            componentsTabs.add(createMorfeoComponent(MyStandardConstants.TAB_RELATIONS_KEY, MyStandardConstants.TAB_RELATIONS_LABEL, Collections.singletonList(componentRelazioni)));

            return componentsTabs;

        } else {
            throw new MyStandardException("Nel modello ricavato non è presente la classe " + owlPrefix + entityType);
        }


    }

    /**
     * Creazione morfeo component tab per la relazione
     * @param value, oggetto relazione
     * @return morfeo component
     */
    private MorfeoComponent getMorfeoComponentFromRelation(ORelation value) {
        MorfeoComponent relation = new MorfeoComponent();
    relation.setKey(MyStandardUtil.mystandardPrefixForMorfeo(null, value.getLabel()));
        relation.setOriginalKey(value.getIRI());
        relation.setLabel(value.getLabel());
        relation.setRange(value.getRangeLocalName());
        relation.setDomain(MyStandardConstants.TAB);
        relation.setFunctionalProperty(value.getIRI());
        relation.setOrder(value.getOrder());
        relation.setInverse(value.getInverseOf());
        return relation;
    }

        /**
         * Metodo per creare un MorfeoComponent
         * @param key, key del component
         * @param label, label del component
         * @param components, components
         * @return
         */
    private MorfeoComponent createMorfeoComponent(String key, String label, List<MorfeoComponent> components) {
        MorfeoComponent morfeoComponentTab = new MorfeoComponent();
        morfeoComponentTab.setKey(key);
        morfeoComponentTab.setOriginalKey(key);
        morfeoComponentTab.setLabel(label);
        morfeoComponentTab.setComponents(components);
        return morfeoComponentTab;
    }

    private List<MorfeoComponentColumn> createDatatableColumns(Boolean readOnly, Boolean isRelationshipTable, Boolean isUserAuthenticated) {
        List<MorfeoComponentColumn> columns = new ArrayList<>();

        if (isRelationshipTable) {
            columns.add(new MorfeoComponentColumn("Tipo", MyStandardConstants.TIPO_ENTITA_COLUMN_KEY, null));
        }
        columns.add(new MorfeoComponentColumn("Versione", MyStandardConstants.VERSIONE_COLUMN_KEY, null));
        columns.add(new MorfeoComponentColumn("Codice", MyStandardConstants.CODICE_ENTITA_COLUMN_KEY, null));
        columns.add(new MorfeoComponentColumn("Nome", MyStandardConstants.NAME_COLUMN_KEY, null));
        columns.add(new MorfeoComponentColumn("Stato", MyStandardConstants.STATO_COLUMN_KEY, null));

        List<MorfeoComponentButton> button = new ArrayList<>();
        if (readOnly) {
            button.add(new MorfeoComponentButton("Vai a versione", "search", "link", "primary", "icon"));
            columns.add(new MorfeoComponentColumn("Azioni", "link", button));
            if (!isRelationshipTable && isUserAuthenticated) {
                List<MorfeoComponentButton> restoreButton = new ArrayList<>();
                restoreButton.add(new MorfeoComponentButton("Ripristina versione", "restore", "restore", "primary", "icon"));
                columns.add(new MorfeoComponentColumn("Ripristina versione", "restore", restoreButton));
            }
        } else {
            if (isRelationshipTable) {
                button.add(new MorfeoComponentButton("Elimina associazione", "delete", "delete", "primary", "icon"));
                columns.add(new MorfeoComponentColumn("Azioni", "delete", button));
            } else if (!isRelationshipTable && isUserAuthenticated) {
                List<MorfeoComponentButton> restoreButton = new ArrayList<>();
                //restoreButton.add(new MorfeoComponentButton("Elimina", "delete", "delete", "primary", "icon"));
                restoreButton.add(new MorfeoComponentButton("Ripristina versione", "restore", "restore", "primary", "icon"));
                columns.add(new MorfeoComponentColumn("Azioni", "restore", restoreButton));
            }
        }

        return columns;
    }

    private MorfeoPagination createTablePagination() {
        List<Integer> sizes = new ArrayList<>();
        sizes.add(10);
        sizes.add(20);
        sizes.add(50);
        return new MorfeoPagination(sizes);
    }

    /**
     * Si ritorna nome icona in relazione al tipo di file
     * @param tipoFile, tipo di file che determina icona
     * @return tipo icona
     */
    private String getIconName(String tipoFile) {
        if (tipoFile.equals("jpg")) return "insert_photo";
        if (tipoFile.equals("txt")) return "description";
        if (tipoFile.equals("pdf")) return "picture_as_pdf";
        if (tipoFile.equals("png")) return "insert_photo";
        if (tipoFile.equals("xlsx")) return "insert_drive_file";
        if (tipoFile.equals("docx")) return "insert_drive_file";
        return "insert_drive_file";
    }







    private void correctTypeForWebsites(MorfeoComponent column) {
        List<MorfeoComponent> columns = column.getColumns();
        for (MorfeoComponent subColumn: columns) {
            List<MorfeoComponent> components = subColumn.getComponents();
            for (MorfeoComponent component: components) {
                if (StringUtils.hasText(component.getOriginalKey()) && !component.getOriginalKey().contains(MyStandardConstants.DOMINIO_BUSINESS_OBJPROP_KEY)) {
                    if (MorfeoTypeEnum.IMAGE.getCode().equalsIgnoreCase(component.getType()) || component.getOriginalKey().contains("hasImage") || component.getOriginalKey().contains("hasLogo")) {
                        component.setType(MorfeoTypeEnum.IMAGE.getCode());
                    } else {
                        String value = component.getDefaultValue();
                        if (StringUtils.hasText(value)) {
                            if (
                                    (value.length() >= 4 && value.substring(0, 4).equals("www.")) ||
                                            (value.length() >= 7 && value.substring(0, 7).equals("http://")) ||
                                            (value.length() >= 8 && value.substring(0, 8).equals("https://"))
                            ) {
                                if (!value.contains("#Inserito") && !value.contains("#Pubblicato") && !value.contains("#Trasmesso")) {
                                    component.setType(MorfeoTypeEnum.HTML_ELEMENT.getCode());
                                    String prefix = "";
                                    if (value.substring(0, 4).equals("www.")) {
                                        prefix = "https://";
                                    }
                                    String link = "<a href='" + prefix + component.getDefaultValue() + "'>" + component.getDefaultValue() + "</a>";
                                    component.setHtml(link);
                                    component.setReadOnly(false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Mapping custom repeatble type (oggetto e title html)
     * @param columnsDetail, struttura owl
     * @return
     */
    private Map<String, Map<String, Object>> mapCustomRepeatablesOrVocabularyProperty(List<OProperty> columnsDetail) {
        Map<String, Map<String, Object>> customRepeatables = new HashMap<>();
        for (OProperty columnDetail : columnsDetail) {
            if (columnDetail.getType() != null && (columnDetail.getType().equals(MyStandardConstants.ENTITY_PROPERTY_TYPE) || columnDetail.getType().equals(MyStandardConstants.VOCABULARY_TYPE))) {

                String key = columnDetail.getIRI();
                Map<String, Object> element = new HashMap<>();
                element.put("key", MyStandardConstants.HEADER_PREFIX_KEY + key);
                element.put(MyStandardConstants.ORDER_KEY, Integer.MAX_VALUE);
                element.put("type", MyStandardConstants.HTML_ELEMENT_TYPE);
                element.put("tag", "p");
                element.put("html", "<p><b>" + columnDetail.getLabel() + "</b></p>");
                customRepeatables.put(key, element);


            }
        }
        return customRepeatables;
    }



    /**
     * Get key without suffix index
     * @param componentKey, key
     * @param index
     * @return componentKey without suffix index
     */
    private String componentKeyWithoutIndex(String componentKey, int index) {
        int stringIndex = componentKey.lastIndexOf("_" + index);
        if (stringIndex > -1) {
            return componentKey.substring(0, stringIndex);
        } else {
            return componentKey;
        }
    }

    /**
     * Si ritorna oggetto: il primo deve essere oggetto struttura (masterFieldSubComponentColumnsComponent), gli altri dei cloni
     * @param index, indice di loop
     * @param masterFieldSubComponentColumnsComponent, oggetto struttura
     * @return oggetto su cui settare i dati
     */
    private MorfeoComponent getComponentByIndex(int index, MorfeoComponent masterFieldSubComponentColumnsComponent) {
        if (index == 0) {
            return masterFieldSubComponentColumnsComponent;
        } else {
            return new MorfeoComponent(masterFieldSubComponentColumnsComponent, index);
        }
    }



    /**
     * Si ottiene il componente per morfeo con la lista di colonne
     * @param oModel, struttura owl
     * @param domain, dominio
     * @param owlPrefix, prefisso owl
     * @param properties, lista object e data properties
     * @param readOnly, boolean se da mostrare in readonlu
     * @return componente morfeo
     */
    private List<MorfeoComponent> getMorfeoComponentsColumnsProperty(OModel oModel, String domain, String owlPrefix, List<OProperty> properties, Boolean readOnly) {
        List<MorfeoComponent> componentsColumns = new ArrayList<>();

        MorfeoComponent morfeoComponent = new MorfeoComponent();
        morfeoComponent.setKey(MyStandardConstants.FORM_COLUMN_KEY_PREFIX + domain);
        morfeoComponent.setOriginalKey(MyStandardConstants.FORM_COLUMN_KEY_PREFIX + domain);
        morfeoComponent.setLabel(MyStandardConstants.FORM_COLUMN_LABEL_PREFIX + domain);
        morfeoComponent.setType(MorfeoTypeEnum.COLUMNS.getCode());
        if (!readOnly) {
            morfeoComponent.setReadOnly(false);
        }

        List<MorfeoComponent> columns = getColumnsProperty(oModel, owlPrefix, properties, readOnly);//Si ottengono columns
        morfeoComponent.setColumns(columns);
        setColumnsReadOnlyPropOnColumns(columns, readOnly);
        componentsColumns.add(morfeoComponent);
        return componentsColumns;
    }

    private void setColumnsReadOnlyPropOnColumns(List<MorfeoComponent> columns, Boolean readOnly) {
        for (MorfeoComponent column: columns) {
            column.setReadOnly(column.getAlwaysReadOnly() ? true : readOnly);//Se always readonly, il Boolean readonly non deve essere considerato
            for (MorfeoComponent component: column.getComponents()) {
                component.setReadOnly(component.getAlwaysReadOnly() ? true : readOnly);//Se always readonly, il Boolean readonly non deve essere considerato

                List<MorfeoComponent> colCompCols = component.getColumns();
                if (colCompCols != null) {
                    for (MorfeoComponent colCompCol : colCompCols) {
                        colCompCol.setReadOnly(colCompCol.getAlwaysReadOnly() ? true : readOnly);//Se always readonly, il Boolean readonly non deve essere considerato

                        List<MorfeoComponent> colCompColComps = colCompCol.getComponents();
                        if (colCompColComps != null) {
                            for (MorfeoComponent colCompColComp: colCompColComps) {
                                colCompColComp.setReadOnly(colCompColComp.getAlwaysReadOnly() ? true : readOnly);//Se always readonly, il Boolean readonly non deve essere considerato

                            }
                        }
                    }
                }
            }
        }
    }




    /**
     * Si ottengono lista component columns da wrappare com components
     * @param oModel, modello con la struttura dei dati
     * @param owlPrefix, prefisso usato per i dati
     * @param columnsDetail, lista proprietà
     * @return lista di component che rappresentano la lista delle proprietà
     */
    private List<MorfeoComponent> getColumnsProperty(OModel oModel, String owlPrefix, List<OProperty> columnsDetail, Boolean readonly) {
        List<MorfeoComponent> columns = new ArrayList<>();
        MorfeoComponent morfeoComponentColumn = new MorfeoComponent();

        List<MorfeoComponent> components = columnsDetail.stream()
                .map(objMap -> {
                    if (objMap instanceof ODataProperty) {
                        MorfeoComponent morfeoComponent = getMorfeoComponentFromDataProperty(objMap, owlPrefix, readonly);
                        return morfeoComponent;
                    } else {
                        MorfeoComponent morfeoComponent = getMorfeoComponentFromObjectProperty(oModel, objMap, owlPrefix, readonly);
                        return morfeoComponent;
                    }

                })
                .collect(Collectors.toList());

        morfeoComponentColumn.setComponents(components);
        columns.add(morfeoComponentColumn);
        return columns;
    }



    private MorfeoComponent createMorfeoFileTypeButton(String id, String iconName) {
        return createMorfeoButton(id, iconName, "tipoFile");
    }

    /**
     * Creazione componente multiselect per morfeo
     * @param objMap, model property
     * @param component, componente su cui impostare i dati per multiselect morfeo
     */
    private void setMorfeoComponentAsMultipleSelect(OProperty objMap, MorfeoComponent component) {
        component.setType(MyStandardConstants.MULTIPLE_SELECT_TYPE);
        Map<String, Object> inputValues = objMap.getValues();
        List<Map<String, Object>> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : inputValues.entrySet()) {
            Map<String, Object> value = new HashMap<>();
            String key = entry.getKey();
            String val = String.valueOf(entry.getValue());
            value.put(MyStandardConstants.LABEL_KEY, val);
            value.put(MyStandardConstants.VALUE_KEY, key);
            values.add(value);
        }


        component.setValues(values);
        component.setHideSelectAll(true);
    }



    private void setMorfeoComponentAsSelect(OProperty objMap, MorfeoComponent component) {
        component.setType(MyStandardConstants.SELECT_TYPE);
        Map<String, Object> inputValues = objMap.getValues();
        List<Map<String, Object>> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : inputValues.entrySet()) {
            Map<String, Object> value = new HashMap<>();
            String key = entry.getKey();
            String val = String.valueOf(entry.getValue());
            value.put(MyStandardConstants.LABEL_KEY, val);
            value.put(MyStandardConstants.VALUE_KEY, key);
            values.add(value);
        }

        MorfeoComponentData data = new MorfeoComponentData();
        data.setValues(values);
        component.setData(data);
    }

    private String getMorfeoTypeByRange(String type) {
        String morfeoType = DataTypeEnum.STRING.getMorfeoType();
        if (type != null) {
            try {
                morfeoType = DataTypeEnum.of(type).getMorfeoType();
            } catch (MyStandardException e) {
                morfeoType = DataTypeEnum.STRING.getMorfeoType();
            }
        }
        return morfeoType;
    }

    /**
     * Creazione di un bottone per morfeo
     * @param id
     * @param iconName
     * @param buttonType
     * @return
     */
    private MorfeoComponent createMorfeoButton(String id, String iconName, String buttonType, String label) {


        MorfeoComponent downloadButtonDatatable = new MorfeoComponent();
        downloadButtonDatatable.setKey("button_" + buttonType + "_" + id);
        downloadButtonDatatable.setOriginalKey("button_" + buttonType + "_" + id);
        downloadButtonDatatable.setType(MorfeoTypeEnum.DATATABLE.getCode());
        downloadButtonDatatable.setDataSrc(MyStandardConstants.MORFEO_DATASRC_VALUES);

        MorfeoComponentData datatableButtonDownloadData = new MorfeoComponentData();

        Map<String, Object> value = new HashMap<>();
        value.put("save", id);
        List<Map<String, Object> > values = new ArrayList<>();
        values.add(value);
        datatableButtonDownloadData.setValues(values);

        List<MorfeoComponentColumn> datatableButtonDownloadColumns = new ArrayList<>();
        MorfeoComponentColumn datatableButtonDownloadColumnValue = new MorfeoComponentColumn();

        datatableButtonDownloadColumnValue.setValue("state");
        datatableButtonDownloadColumnValue.setLabel(label);

        datatableButtonDownloadColumns.add(datatableButtonDownloadColumnValue);

        MorfeoComponentColumn datatableButtonDownloadColumnIcon = new MorfeoComponentColumn();

        datatableButtonDownloadColumnIcon.setValue("save");
        datatableButtonDownloadColumnIcon.setLabel(label);

        List<MorfeoComponentButton> buttonDownload = new ArrayList<>();
        buttonDownload.add(new MorfeoComponentButton("saveAttachmentReadOnly", iconName, "edit", "primary", "icon"));

        datatableButtonDownloadColumnIcon.setButtons(buttonDownload);
        datatableButtonDownloadColumns.add(datatableButtonDownloadColumnIcon);

        datatableButtonDownloadData.setColumns(datatableButtonDownloadColumns);

        downloadButtonDatatable.setData(datatableButtonDownloadData);

        List<MorfeoComponent> buttonWithIconList = new ArrayList<>();
        buttonWithIconList.add(downloadButtonDatatable);

        MorfeoComponent buttonWithIcon = new MorfeoComponent();
        buttonWithIcon.setOrder(Integer.MAX_VALUE);
        buttonWithIcon.setComponents(buttonWithIconList);
        return buttonWithIcon;
    }

    /**
     * Creazione di un bottone per morfeo
     * @param id
     * @param iconName
     * @param buttonType
     * @return
     */
    private MorfeoComponent createMorfeoButton(String id, String iconName, String buttonType) {


        MorfeoComponent downloadButtonDatatable = new MorfeoComponent();
        downloadButtonDatatable.setKey("button_" + buttonType + "_" + id);
        downloadButtonDatatable.setOriginalKey("button_" + buttonType + "_" + id);
        downloadButtonDatatable.setType(MorfeoTypeEnum.DATATABLE.getCode());
        downloadButtonDatatable.setDataSrc(MyStandardConstants.MORFEO_DATASRC_VALUES);

        MorfeoComponentData datatableButtonDownloadData = new MorfeoComponentData();

        Map<String, Object> value = new HashMap<>();
        value.put("save", id);
        List<Map<String, Object> > values = new ArrayList<>();
        values.add(value);
        datatableButtonDownloadData.setValues(values);

        List<MorfeoComponentColumn> datatableButtonDownloadColumns = new ArrayList<>();
        MorfeoComponentColumn datatableButtonDownloadColumnValue = new MorfeoComponentColumn();

        datatableButtonDownloadColumnValue.setValue("state");
        datatableButtonDownloadColumnValue.setLabel("");

        datatableButtonDownloadColumns.add(datatableButtonDownloadColumnValue);

        MorfeoComponentColumn datatableButtonDownloadColumnIcon = new MorfeoComponentColumn();

        datatableButtonDownloadColumnIcon.setValue("save");
        datatableButtonDownloadColumnIcon.setLabel("");

        List<MorfeoComponentButton> buttonDownload = new ArrayList<>();
        buttonDownload.add(new MorfeoComponentButton("saveAttachmentReadOnly", iconName, "edit", "primary", "icon"));

        datatableButtonDownloadColumnIcon.setButtons(buttonDownload);
        datatableButtonDownloadColumns.add(datatableButtonDownloadColumnIcon);

        datatableButtonDownloadData.setColumns(datatableButtonDownloadColumns);

        downloadButtonDatatable.setData(datatableButtonDownloadData);

        List<MorfeoComponent> buttonWithIconList = new ArrayList<>();
        buttonWithIconList.add(downloadButtonDatatable);

        MorfeoComponent buttonWithIcon = new MorfeoComponent();
        buttonWithIcon.setOrder(Integer.MAX_VALUE);
        buttonWithIcon.setComponents(buttonWithIconList);
        return buttonWithIcon;
    }



    private class DomainComparator implements Comparator<String> {//Container ordering
        @Override
        public int compare(String entityType1, String entityType2) {
            Integer order1;
            Integer order2;

            Optional<MyStandardDetailProperties.MyStandardDetailField> first = myStandardDetailProperties.getContainer().stream().
                    filter(p -> p.getKey().equals(entityType1)).
                    findFirst();

            if (first.isPresent()) {
                order1 = first.get().getOrder();
            } else {
                order1 = Integer.MAX_VALUE;
            }

            Optional<MyStandardDetailProperties.MyStandardDetailField> second = myStandardDetailProperties.getContainer().stream().
                    filter(p -> p.getKey().equals(entityType2)).
                    findFirst();

            if (second.isPresent()) {
                order2 = second.get().getOrder();
            } else {
                order2 = Integer.MAX_VALUE;
            }

            int returned = order1 - order2;

            if (returned == 0 && !entityType1.equals(entityType2))
                returned = -1;

            return returned;


        }
    }

    /**
     * Check the field should be hidden.
     * To be hidden either the hidden field is true, or the visibleOnlyAuthenticated field is true and the user is not currently authenticated
     * @param hidden, configuration if field must be hidden or not
     * @param isVisibleOnlyAuthenticated, configuration if field must be hidden if user is not authenticated
     * @return true if field should be hidden, false otherwise
     */
    private Boolean fieldMustBeHidden(Boolean hidden, Boolean isVisibleOnlyAuthenticated) {

        boolean hideField = false;
        if (hidden) {//Hidden true, field must be hidden
            hideField = true;
        } else if (isVisibleOnlyAuthenticated) {

            if (!MyStandardUtil.isUserAuthenticated()) {
                //hideField is true if user is not authenticated
                hideField = true;
            }

        }//else field must be shown

        return hideField;
    }


}

