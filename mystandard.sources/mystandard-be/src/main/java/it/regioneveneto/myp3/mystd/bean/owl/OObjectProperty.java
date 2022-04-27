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

public class OObjectProperty extends OProperty {


    private String rangeClassIRI;
    private String domainIRI;


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

    //Constructor
    public OObjectProperty(String IRI) {
        super(IRI);
    }

}
