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
package it.regioneveneto.myp3.mystd.bean.owl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OClass extends OIdentificable {

    private Map<String, OProperty> dataProperty = new TreeMap<>();
    private List<String> relations = new ArrayList<>();
    private Map<String, OProperty> objectProperty = new TreeMap<>();
    private List<String> parentClasses = new ArrayList<>();
    private Map<String, List<OIdentificable>> annotationProperties = new TreeMap<>();
    private String prefix;
    private String prefixData;
    private String localName;
    private String description;
    private Boolean fullParsed = false;
    private Boolean showIpaFilter;
    private String stateMachineConfig;
    private List<String> stateMachine;

    public List<String> getParentClasses() {
        return parentClasses;
    }

    public void setParentClasses(List<String> parentClasses) {
        this.parentClasses = parentClasses;
    }

    public List<String> getRelations() {
        return relations;
    }

    public void setRelations(List<String> relations) {
        this.relations = relations;
    }


    public Map<String, OProperty> getDataProperty() {
        return dataProperty;
    }

    public void setDataProperty(Map<String, OProperty> dataProperty) {
        this.dataProperty = dataProperty;
    }

    public Map<String, OProperty> getObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(Map<String, OProperty> objectProperty) {
        this.objectProperty = objectProperty;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefixData() {
        return prefixData;
    }

    public void setPrefixData(String prefixData) {
        this.prefixData = prefixData;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public Boolean getFullParsed() {
        return fullParsed;
    }

    public void setFullParsed(Boolean fullParsed) {
        this.fullParsed = fullParsed;
    }

    public Map<String, List<OIdentificable>> getAnnotationProperties() {
        return annotationProperties;
    }

    public void setAnnotationProperties(Map<String, List<OIdentificable>> annotationProperties) {
        this.annotationProperties = annotationProperties;
    }

    public Boolean getShowIpaFilter() {
        return showIpaFilter;
    }

    public void setShowIpaFilter(Boolean showIpaFilter) {
        this.showIpaFilter = showIpaFilter;
    }

    public String getStateMachineConfig() {
        return stateMachineConfig;
    }

    public void setStateMachineConfig(String stateMachineConfig) {
        this.stateMachineConfig = stateMachineConfig;
    }

    public List<String> getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(List<String> stateMachine) {
        this.stateMachine = stateMachine;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //Constructor


    public OClass(String IRI) {
        super(IRI);
    }

}
