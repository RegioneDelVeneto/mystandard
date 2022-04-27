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
import java.util.Objects;

public class OValidation {

    private Boolean required;
    private Integer cardinality;
    private Integer min;
    private Integer max;
    private Integer minLength;
    private Integer maxLength;
    private String regex;
    private Boolean conditionalShow;
    private Object conditionalValue;
    private String conditionalWhen;
    private String conditionalRequired;
    private String conditionalOperator;
    private List<String> conditionalValues = new ArrayList<>();

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }


    public Integer getCardinality() {
        return cardinality;
    }

    public void setCardinality(Integer cardinality) {
        this.cardinality = cardinality;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Boolean getConditionalShow() {
        return conditionalShow;
    }

    public void setConditionalShow(Boolean conditionalShow) {
        this.conditionalShow = conditionalShow;
    }

    public Object getConditionalValue() {
        return conditionalValue;
    }

    public void setConditionalValue(Object conditionalValue) {
        this.conditionalValue = conditionalValue;
    }

    public String getConditionalWhen() {
        return conditionalWhen;
    }

    public void setConditionalWhen(String conditionalWhen) {
        this.conditionalWhen = conditionalWhen;
    }

    public String getConditionalRequired() {
        return conditionalRequired;
    }

    public void setConditionalRequired(String conditionalRequired) {
        this.conditionalRequired = conditionalRequired;
    }

    public String getConditionalOperator() {
        return conditionalOperator;
    }

    public void setConditionalOperator(String conditionalOperator) {
        this.conditionalOperator = conditionalOperator;
    }

    public List<String> getConditionalValues() {
        return conditionalValues;
    }

    public void setConditionalValues(List<String> conditionalValues) {
        this.conditionalValues = conditionalValues;
    }



    public Boolean isEmpty() {
        return Objects.isNull(required) &&
                Objects.isNull(cardinality) &&
                Objects.isNull(min) &&
                Objects.isNull(max) &&
                Objects.isNull(minLength) &&
                Objects.isNull(maxLength) &&
                Objects.isNull(regex) &&
                Objects.isNull(conditionalShow) &&
                Objects.isNull(conditionalValue) &&
                Objects.isNull(conditionalWhen) &&
                Objects.isNull(conditionalRequired) &&
                Objects.isNull(conditionalOperator) &&
                Objects.isNull(conditionalValues);
    }
}
