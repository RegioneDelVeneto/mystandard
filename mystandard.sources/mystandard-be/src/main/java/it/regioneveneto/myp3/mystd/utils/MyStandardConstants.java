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
package it.regioneveneto.myp3.mystd.utils;

public class MyStandardConstants {

    public static final String JENA_FRAMEWORK = "jena";
    public static final String JENA_RDF_REPOSITORY_FUSEKI = "fuseki";
    public static final String JENA_RDF_REPOSITORY_LOCAL = "local";

    public static final String RELATION_TYPE = "relation_type";

    public static final String ENUMERATION = "enumeration";
    public static final String RELATION_TYPE_ENTITY_PROPERTY = "entity property";
    public static final String RELATION_TYPE_FUNCTIONAL = "functional";
    public static final String RELATION_TYPE_ENUMERATION = "enumeration";
    public static final String RELATION_TYPE_MULTIPLE_ENUMERATION = "multiple_enumeration";
    public static final String CONDITIONAL_REQUIRED = "conditionalRequired";
    public static final String CONDITIONAL_REQUIRED_OPERATOR = "conditionalRequiredOperator";
    public static final String CONDITIONAL_REQUIRED_VALUES = "conditionalRequiredValues";




    public static final String CONDITIONAL_SHOW = "conditionalShow";
    public static final String CONDITIONAL_SHOW_WHEN = "conditionalShowWhen";
    public static final String CONDITIONAL_SHOW_VALUE = "conditionalShowValue";





    public static final String MULTIPLE_VALUES_SEPARATOR = ",";


    public static final String ENTITY_IRI = "entityIRI";



    public static final String ALLEGATI_DOMAIN = "allegati";
    public static final String ALLEGATI_KEY_PREFIX = "allegati_";
    public static final String FORM_COLUMN_KEY_PREFIX = "formCompContainer";
    public static final String FORM_COLUMN_LABEL_PREFIX = "Form";
    public static final String HEADER_PREFIX_KEY = "header_";
    public static final String HISTORICAL_TAB_KEY = "historical_tab_key";
    public static final String HISTORICAL_TAB_LABEL = "Storico";
    public static final String HISTORIC_DATATABLE_KEY = "historicTable";
    public static final String ID_DOC_PREFIX = "id_doc_";
    public static final String MORFEO_DATASRC_VALUES = "values";
    public static final String TAB = "Tab";
    public static final String TABS_WRAPPER_KEY = "tabs_wrapper";
    public static final String TABS_WRAPPER_LABEL = "Tabs";
    public static final String HSTORIC_TABS_WRAPPER_LABEL = "Historic_Tabs";
    public static final String TITLE_PREFIX_KEY = "title_";

    public static final String TAB_RELATIONS_KEY = "tab_relations_key";
    public static final String TAB_RELATIONS_LABEL = "Relazioni";
    public static final String NEW_RELATIONSHIP = "Nuova Relazione";
    public static final String NEW_RELATIONSHIP_BUTTON_PREFIX = "newRelationshipButton_";
    public static final String VISUALIZZA_DATI_BUTTON_LABEL = "Visualizza dati";
    public static final String VISUALIZZA_DATI_BUTTON_PREFIX = "visualizzaDatiButton_";

    public static final String TABS_TYPE = "tabs";
    public static final String SELECT_TYPE = "select";
    public static final String MULTIPLE_SELECT_TYPE = "selectboxes";

    public static final String EDITOR_TYPE = "editor";
    public static final String CODE_EDITOR_TYPE = "codeEditor";
    public static final String CUSTOM_REPEATABLE_TYPE = "custom_repeatable";
    public static final String ENTITY_PROPERTY_TYPE = "entity_property";
    public static final String VOCABULARY_TYPE = "vocabulary_property";

    public static final String TEXTFIELD_TYPE = "textfield";
    public static final String COLUMNS_TYPE = "columns";
    public static final String HTML_ELEMENT_TYPE = "htmlelement";

    public static final String CUSTOM_TAB_NAME = "custom_tab_name";
    public static final String CUSTOM_IND_NAME = "custom_ind_name";
    public static final String YES_LABEL = "si";
    public static final String DOMAIN_KEY = "domain";
    public static final String ORDER_KEY = "order";
    public static final String COMPONENTS_KEY = "components";
    public static final String DATATABLE_KEY_PREFIX = "datatable_";

    //SPARQL
    public static final String SPARQL_OBJ_PROP_COLUMN_KEY = "objProp";
    public static final String SPARQL_VERSIONE_COLUMN_KEY = "versioneCol";
    public static final String SPARQL_CODICE_COLUMN_KEY = "codiceCol";
    public static final String SPARQL_STATO_COLUMN_KEY = "statoCol";
    public static final String SPARQL_NOME_COLUMN_KEY = "nomeCol";
    public static final String SPARQL_TIPO_COLUMN_KEY = "tipoCol";
    public static final String SPARQL_DATA_PROP_COLUMN_KEY = "dataProp";
    public static final String SPARQL_VALORE_DAT_PROP_COLUMN_KEY = "valoreDatProp";
    public static final String SPARQL_IND_PROP_COLUMN_KEY = "indProp";
    public static final String SPARQL_VALUE_COLUMN_KEY = "value";

    //Column
    public static final String CODICE_ENTITA_COLUMN_KEY = "CodiceEntita";
    public static final String VERSIONE_COLUMN_KEY = "Versione";
    public static final String IPA_CODE_COLUMN_KEY = "IPAcode";
    public static final String MAX_VERSIONE_COLUMN_KEY = "MaxVersione";
    public static final String TOTAL_COUNT_KEY = "totalCount";

    public static final String ATTACHMENT_FILE_TYPE_COLUMN_KEY = "tipoFile";
    public static final String ATTACHMENT_NAME_COLUMN_KEY = "nome_allegato";
    public static final String ATTACHMENT_DESCRIPTION_COLUMN_KEY = "descrizione_allegato";
    public static final String ATTACHMENT_DATE_COLUMN_KEY = "date";
    public static final String ATTACHMENT_ACTIONS_COLUMN_KEY = "azioni";
    public static final String ATTACHMENT_ID_DOCUMENTO_COLUMN_KEY = "id_documento";
    public static final String DOMAIN_COLUMN_KEY = "Dominio";
    public static final String TIPO_ENTITA_COLUMN_KEY = "TipoEntita";
    public static final String LABEL_TIPO_ENTITA_COLUMN_KEY = "LabelTipoEntita";
    public static final String ID_ENTITA_COLUMN_KEY = "IdEntita";
    public static final String STATO_COLUMN_KEY = "Stato";
    public static final String STATO_OBJPROP_KEY = "stato";
    public static final String NAME_COLUMN_KEY = "name";
    public static final String DT_INS_COLUMN_KEY = "dtIns";
    public static final String UTE_INS_COLUMN_KEY = "uteIns";
    public static final String DEFINITA_DA_COLUMN_KEY = "DefinitaDa";
    public static final String SPECIALIZZAZIONE_COLUMN_KEY = "Specializzazione";
    public static final String DATA_ULTIMA_MODIFICA_KEY = "DataUltimaModifica";
    public static final String UTENTE_ULTIMA_MODIFICA_KEY = "UtenteUltimaModifica";
    public static final String ULTIMA_MODIFICA_KEY = "ultimaModifica";
    public static final String DATA_ULTIMO_AGGIORNAMENTO_COLUMN_KEY = "dataUltimoAggiornamento";



    public static final String DOMINIO_BUSINESS_KEY = "DominioBusiness";
    public static final String DOMINIO_BUSINESS_OBJPROP_KEY = "dominio_di_business";
    public static final String COUNT_INDIVIDUALS_KEY = "countInd";
    public static final String OBJECT_COLUMN_KEY = "object";



    public static final String DT_UPD_COLUMN_KEY = "dtUpdate";
    public static final String UTE_UPD_COLUMN_KEY = "uteUpdate";
    public static final String INDIVIDUALS_COLUMN_KEY = "individuals";


    public static final String RDF_SYNTAX_NS_TYPE = "22-rdf-syntax-ns#type";
    public static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";

    public static final String TAB_RELATION_PREFIX_KEY = "tab_relation_";
    public static final String FUNCTIONAL_PROPERTY_IRI = "_functionalPropertyIRI";
    public static final String RANGE = "range";
    public static final String FUNCTIONAL_PROP_TARGET_INDIVIDUALS_IRI = "_targetIndividualsIRI";
    public static final String OBJ_PROPERTY_TARGET_INDIVIDUAL_IRI = "_targetIndividualIRI";
    public static final String OBJ_PROPERTY_ENTITY_PROPERTY_IRI = "_entityPropertyIRI";
    public static final String OBJ_PROPERTY_ENTITY_PROPERTY_LOCAL_NAME = "_entityPropertyLocalName";


    public static final String DATA_PROPERTY_IRI = "_dataPropertyIRI";
    public static final String DATA_PROPERTY_VALUE = "_dataPropertyValue";
    public static final String DATA_PROPERTY_LOCAL_NAME = "_dataPropertyLocalName";


    public static final String NOME_FILE_COLUMN_KEY = "nome_allegato";
    public static final String TIPO_FILE_COLUMN_KEY = "tipoFile";
    public static final String ID_DOCUMENTO_COLUMN_KEY = "id_documento";
    public static final String ID_FILE_COLUMN_KEY = "idFile";
    public static final String ID_ENTITA_ORIGINE_COLUMN_KEY = "idEntitaOrigine";
    public static final String ENTITY_STATE = "entityState";



    public static final String PREFIX_MYSTD = "https://mystandard.regione.veneto.it/onto/BPO#";
    public static final String ITEMS_FIELD_KEY = "items";
    public static final String LABEL_KEY = "label";
    public static final String URL_KEY = "url";
    public static final String DOMINIO_GENERALE = "Generale";
    public static final String CODE_KEY = "Codice";
    public static final String DESC_KEY = "Descrizione";
    public static final String IS_NEW_KEY = "isNew";
    public static final String CATALOGO_KEY = "catalogo";
    public static final String VALUE_KEY = "value";
    public static final String PATH_KEY = "path";

    public static final String NAME_KEY = "name";
    public static final String MYSTD_PREFIX_KEY = "mystd";
    public static final String SHOW_IPA_FILTER = "ipaFilter";
    public static final String MENU_PRINCIPALE = "menuPrincipale";
    public static final String RIEPILOGO_CONTAINER = "Riepilogo";



    public static final Integer DEFAULT_FIRST_VERSIONE = 1;
    public static final String EMPTY_SELECT_KEY = "";
    public static final String EMPTY_SELECT_VALUE = "--";


    //Security
    public static final String COOKIE_NAME_ACCESS_TOKEN = "MYSTANDARD_ACCESS_TOKEN";
    public static final String COOKIE_PATH = "/";
    public static final String CACHE_NAME_USER_ROLES = "MyStandard-UserRoles";
    public static final String CACHE_NAME_USER_VALIDITY = "MyStandard-UserValidity";
    public static final String CACHE_NAME_VALID_TOKENS = "MyStandard-ValidTokens";
    public static final String CACHE_STATE_MACHINE = "MyStandard-stateMachine";


    //MyProfile
    public static final String PERMISSION_DOMINIO = "dominio";
    public static final String PERMISSION_CLASSE = "classe";



    //JSON Logic
    public static final String REGEX_OP = "regex";
    public static final String AND_OP = "and";
    public static final String NOT_EQUALS_OP = "!==";
    public static final String EQUALS_OP = "===";

    //ERRORS
    public static final String ERROR_REQUIRED = "error_required";
    public static final String ERROR_REGEX = "error_regex";
    public static final String ERROR_CONDITIONAL_REQUIRED = "error_conditional_required";
    public static final String ERROR_FIELD_NOT_EXISTS = "error_field_not_exists";


}
