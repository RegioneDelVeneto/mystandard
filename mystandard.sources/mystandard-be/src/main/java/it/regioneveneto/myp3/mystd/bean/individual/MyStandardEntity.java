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
package it.regioneveneto.myp3.mystd.bean.individual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyStandardEntity {

    private String entityIRI;
    private Map<String, Object> dataProperty = new HashMap<>();
    private List<MyStandardEntityPropertyIndividual> entityProperty;
    private List<MyStandardFunctionalPropertyIndividual> functionalProperty;

    //Getter and Setter


    public String getEntityIRI() {
        return entityIRI;
    }

    public void setEntityIRI(String entityIRI) {
        this.entityIRI = entityIRI;
    }

    public Map<String, Object> getDataProperty() {
        return dataProperty;
    }

    public void setDataProperty(Map<String, Object> dataProperty) {
        this.dataProperty = dataProperty;
    }

    public List<MyStandardEntityPropertyIndividual> getEntityProperty() {
        return entityProperty;
    }

    public void setEntityProperty(List<MyStandardEntityPropertyIndividual> entityProperty) {
        this.entityProperty = entityProperty;
    }

    public List<MyStandardFunctionalPropertyIndividual> getFunctionalProperty() {
        return functionalProperty;
    }

    public void setFunctionalProperty(List<MyStandardFunctionalPropertyIndividual> functionalProperty) {
        this.functionalProperty = functionalProperty;
    }


}
