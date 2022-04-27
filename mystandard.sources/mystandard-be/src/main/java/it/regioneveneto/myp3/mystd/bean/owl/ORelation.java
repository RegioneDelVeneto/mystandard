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

public class ORelation extends OIdentificable {

    private String domainIRI;
    private String rangeIRI;
    private String label;
    private String rangeLocalName;
    private Integer order;
    private String subPropertyOf;
    private String inverseOf;

    public String getSubPropertyOf() {
        return subPropertyOf;
    }

    public void setSubPropertyOf(String subPropertyOf) {
        this.subPropertyOf = subPropertyOf;
    }

    public String getInverseOf() {
        return inverseOf;
    }

    public void setInverseOf(String inverseOf) {
        this.inverseOf = inverseOf;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    private String relationType;

    public ORelation(String IRI, String domainIRI, String rangeIRI) {
        super(IRI);
        this.domainIRI = domainIRI;
        this.rangeIRI = rangeIRI;
    }

    public String getDomainIRI() {
        return domainIRI;
    }

    public void setDomainIRI(String domainIRI) {
        this.domainIRI = domainIRI;
    }

    public String getRangeIRI() {
        return rangeIRI;
    }

    public void setRangeIRI(String rangeIRI) {
        this.rangeIRI = rangeIRI;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRangeLocalName() {
        return rangeLocalName;
    }

    public void setRangeLocalName(String rangeLocalName) {
        this.rangeLocalName = rangeLocalName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
