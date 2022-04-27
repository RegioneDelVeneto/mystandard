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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MorfeoComponentData {

    private List<MorfeoComponentColumn> columns;
    private List<Map<String, Object>>  values;
    private MorfeoPagination pagination;

    public MorfeoComponentData(List<MorfeoComponentColumn> columns, List<Map<String, Object>> values, MorfeoPagination pagination) {
        this.columns = columns;
        this.values = values;
        this.pagination = pagination;
    }

    public MorfeoComponentData() {}

    //Getter and setter
    public List<MorfeoComponentColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<MorfeoComponentColumn> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getValues() {
        return values;
    }

    public void setValues(List<Map<String, Object>> values) {
        this.values = values;
    }

    public MorfeoPagination getPagination() {
        return pagination;
    }

    public void setPagination(MorfeoPagination pagination) {
        this.pagination = pagination;
    }
}
