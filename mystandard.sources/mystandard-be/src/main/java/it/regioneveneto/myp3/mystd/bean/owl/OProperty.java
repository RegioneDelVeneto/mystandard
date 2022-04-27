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

import java.util.Map;
import java.util.TreeMap;

public class OProperty extends OIdentificable {

    private String label;
    private String type;
    private Boolean hidden;
    private Boolean visibleOnlyAuthenticated;
    private Integer order;
    private String domainLocalName;
    private String rangeClassIRI;
    private String rangeClassLocal;
    private String domainIRI;
    private String localName;
    private OValidation validation;
    private Map<String, OProperty> subProperties = new TreeMap<>();

    private Map<String, Object> values = new TreeMap<>();

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getVisibleOnlyAuthenticated() {
        return visibleOnlyAuthenticated;
    }

    public void setVisibleOnlyAuthenticated(Boolean visibleOnlyAuthenticated) {
        this.visibleOnlyAuthenticated = visibleOnlyAuthenticated;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDomainLocalName() {
        return domainLocalName;
    }

    public void setDomainLocalName(String domainLocalName) {
        this.domainLocalName = domainLocalName;
    }

    public String getRangeClassIRI() {
        return rangeClassIRI;
    }

    public void setRangeClassIRI(String rangeClassIRI) {
        this.rangeClassIRI = rangeClassIRI;
    }

    public String getDomainIRI() {
        return domainIRI;
    }

    public void setDomainIRI(String domainIRI) {
        this.domainIRI = domainIRI;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public String getRangeClassLocal() {
        return rangeClassLocal;
    }

    public void setRangeClassLocal(String rangeClassLocal) {
        this.rangeClassLocal = rangeClassLocal;
    }

    public OValidation getValidation() {
        return validation;
    }

    public void setValidation(OValidation validation) {
        this.validation = validation;
    }

    public Map<String, OProperty> getSubProperties() {
        return subProperties;
    }

    public void setSubProperties(Map<String, OProperty> subProperties) {
        this.subProperties = subProperties;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    //Constructore
    public OProperty(String IRI) {
        super(IRI);
    }

}
