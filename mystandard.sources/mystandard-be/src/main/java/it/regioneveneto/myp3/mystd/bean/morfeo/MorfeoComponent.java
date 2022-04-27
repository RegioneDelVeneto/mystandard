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
package it.regioneveneto.myp3.mystd.bean.morfeo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.regioneveneto.myp3.mystd.utils.MyStandardUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MorfeoComponent implements Serializable {

    //public String input;
    private String key;
    private String originalKey;
    private String label;
    private String type;
    private String dataSrc;
    private String defaultValue;
    private List<MorfeoComponent> columns;
    private List<MorfeoComponent> components;
    private MorfeoComponentData data;
    private List<Map<String, Object>>  values;//Selectboxes
    private Boolean hideSelectAll;//Selectboxes
    private Boolean emptyRepeatable;
    private String html;
    private String tag;
    private String range;
    private String functionalProperty;
    private Boolean readOnly = true;

    @JsonIgnore
    private Boolean alwaysReadOnly = false;//Definizione di campi che è sempre readonly sia in dettaglio che in modifica
    private Boolean hidden = false;
    private MorfeoValidate validate;
    private MorfeoConditionalValidate conditional;

    private String inverse;



    @JsonIgnore
    private Integer order;

    private String domain;//Custom property not used by Morfeo



    //Getter and setter

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public void setOriginalKey(String originalKey) {
        this.originalKey = originalKey;
    }

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

    public String getDataSrc() {
        return dataSrc;
    }

    public void setDataSrc(String dataSrc) {
        this.dataSrc = dataSrc;
    }

    public List<MorfeoComponent> getColumns() {
        return columns;
    }

    public void setColumns(List<MorfeoComponent> columns) {
        this.columns = columns;
    }

    public List<MorfeoComponent> getComponents() {
        return components;
    }

    public void setComponents(List<MorfeoComponent> components) {
        this.components = components;
    }

    public MorfeoComponentData getData() {
        return data;
    }

    public void setData(MorfeoComponentData data) {
        this.data = data;
    }

    public List<Map<String, Object>> getValues() {
        return values;
    }

    public void setValues(List<Map<String, Object>> values) {
        this.values = values;
    }

    public Boolean getHideSelectAll() {
        return hideSelectAll;
    }

    public void setHideSelectAll(Boolean hideSelectAll) {
        this.hideSelectAll = hideSelectAll;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getEmptyRepeatable() {
        return emptyRepeatable;
    }

    public void setEmptyRepeatable(Boolean emptyRepeatable) {
        this.emptyRepeatable = emptyRepeatable;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getFuntionalProperty() {
        return functionalProperty;
    }

    public void setFunctionalProperty(String functionalProperty) {
        this.functionalProperty = functionalProperty;
    }

    public MorfeoValidate getValidate() {
        return validate;
    }

    public void setValidate(MorfeoValidate validate) {
        this.validate = validate;
    }

    public MorfeoConditionalValidate getConditional() {
        return conditional;
    }

    public void setConditional(MorfeoConditionalValidate conditional) {
        this.conditional = conditional;
    }

    public String getInverse() {
        return inverse;
    }

    public void setInverse(String inverse) {
        this.inverse = inverse;
    }

    public String getFunctionalProperty() {
        return functionalProperty;
    }

    public Boolean getAlwaysReadOnly() {
        return alwaysReadOnly;
    }

    public void setAlwaysReadOnly(Boolean alwaysReadOnly) {
        this.alwaysReadOnly = alwaysReadOnly;
    }

    //Constructor


    public MorfeoComponent() {
    }

    public MorfeoComponent(String key, String originalKey, String label, String type, List<MorfeoComponent> components, List<MorfeoComponent> columns,
                           Boolean readOnly, Integer order, String domain, String defaultValue) {
        this.key = key;
        this.originalKey = originalKey;
        this.label = label;
        this.type = type;
        this.components = new ArrayList<>();
        for (MorfeoComponent comp: MyStandardUtil.emptyIfNull(components)) {
            this.components.add(new MorfeoComponent(comp));
        }
        this.columns = new ArrayList<>();
        for (MorfeoComponent comp: MyStandardUtil.emptyIfNull(columns)) {
            this.columns.add(new MorfeoComponent(comp));
        }
        this.readOnly = readOnly;
        this.order = order;
        this.domain = domain;
        this.defaultValue = defaultValue;
    }


    public MorfeoComponent(String key, String originalKey, String label, String type, List<MorfeoComponent> components, List<MorfeoComponent> columns,
                           Boolean readOnly, Integer order, String domain, String defaultValue, int index) {
        this.key = key + "_" + index;
        this.originalKey = originalKey  + "_" + index;
        this.label = label;
        this.type = type;
        this.components = new ArrayList<>();
        for (MorfeoComponent comp: MyStandardUtil.emptyIfNull(components)) {
            this.components.add(new MorfeoComponent(comp, index));
        }
        this.columns = new ArrayList<>();
        for (MorfeoComponent comp: MyStandardUtil.emptyIfNull(columns)) {
            this.columns.add(new MorfeoComponent(comp, index));
        }
        this.readOnly = readOnly;
        this.order = order;
        this.domain = domain;
        this.defaultValue = defaultValue;
    }

    public MorfeoComponent(MorfeoComponent that) {
        this(that.getKey(), that.getOriginalKey(), that.getLabel(), that.getType(), that.getComponents(), that.getColumns(), that.getReadOnly(), that.getOrder(), that.getDomain(), that.getDefaultValue());
    }

    public MorfeoComponent(MorfeoComponent that, int index) {
        this(that.getKey(), that.getOriginalKey(), null,  that.getType(), that.getComponents(), that.getColumns(), that.getReadOnly(), that.getOrder(), that.getDomain(), that.getDefaultValue(), index);
    }
}
