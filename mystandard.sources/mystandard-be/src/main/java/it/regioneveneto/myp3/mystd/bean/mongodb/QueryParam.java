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
package it.regioneveneto.myp3.mystd.bean.mongodb;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.regioneveneto.myp3.mystd.bean.enumeration.QueryParamTypeEnum;
import it.regioneveneto.myp3.mystd.validator.constraint.EnumNamePattern;

import javax.validation.constraints.NotEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryParam {

    @NotEmpty
    private String label;

    @NotEmpty
    private String key;

    @EnumNamePattern(regexp = "SCALAR|ENTITY")
    private QueryParamTypeEnum type;
    private String entityType;


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public QueryParamTypeEnum getType() {
        return type;
    }

    public void setType(QueryParamTypeEnum type) {
        this.type = type;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }


    @Override
    public String toString() {
        return "QueryParam{" +
                "label='" + label + '\'' +
                ", key='" + key + '\'' +
                ", type=" + type +
                ", entityType='" + entityType + '\'' +
                '}';
    }
}
