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
package it.regioneveneto.myp3.mystd.bean.pagination;

import java.util.List;
import java.util.Map;

public class DatatablePaginated {

    private List<Map<String, Object>> records;
    private DatatablePagination pagination;

    public DatatablePaginated(List<Map<String, Object>> allDataFiltered, Integer totalRecords) {
        this.records = allDataFiltered;
        this.pagination = new DatatablePagination(totalRecords);
    }

    public class DatatablePagination {
        private Integer totalRecords;

        public DatatablePagination(Integer totalRecords) {
            this.totalRecords = totalRecords;
        }

        public Integer getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(Integer totalRecords) {
            this.totalRecords = totalRecords;
        }
    }

    public List<Map<String, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }

    public DatatablePagination getPagination() {
        return pagination;
    }

    public void setPagination(DatatablePagination pagination) {
        this.pagination = pagination;
    }
}
