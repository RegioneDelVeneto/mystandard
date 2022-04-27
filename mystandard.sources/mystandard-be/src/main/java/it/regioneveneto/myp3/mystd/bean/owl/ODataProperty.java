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

public class ODataProperty extends OProperty {


    private String domainIRI;
    private Map<String, Object> values = new TreeMap<>();
    private OValidation validation;



    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public String getDomainIRI() {
        return domainIRI;
    }

    public void setDomainIRI(String domainIRI) {
        this.domainIRI = domainIRI;
    }

    public OValidation getValidation() {
        return validation;
    }

    public void setValidation(OValidation validation) {
        this.validation = validation;
    }

    //Constructore
    public ODataProperty(String IRI) {
        super(IRI);
    }


}
