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

import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardIndividualOperationEnum;

import java.util.Map;

public class MyStandardEntityPropertyIndividual {

    private Map<String, Object> dataProperty;
    private MyStandardIndividualOperationEnum _operation;
    private String _entityPropertyIRI;
    private String _entityRangeIRI;
    private String _targetIndividualIRI;

    //Getter and setter


    public Map<String, Object> getDataProperty() {
        return dataProperty;
    }

    public void setDataProperty(Map<String, Object> dataProperty) {
        this.dataProperty = dataProperty;
    }

    public MyStandardIndividualOperationEnum get_operation() {
        return _operation;
    }

    public void set_operation(MyStandardIndividualOperationEnum _operation) {
        this._operation = _operation;
    }

    public String get_entityPropertyIRI() {
        return _entityPropertyIRI;
    }

    public void set_entityPropertyIRI(String _entityPropertyIRI) {
        this._entityPropertyIRI = _entityPropertyIRI;
    }

    public String get_targetIndividualIRI() {
        return _targetIndividualIRI;
    }

    public void set_targetIndividualIRI(String _targetIndividualIRI) {
        this._targetIndividualIRI = _targetIndividualIRI;
    }

    public String get_entityRangeIRI() {
        return _entityRangeIRI;
    }

    public void set_entityRangeIRI(String _entityRangeIRI) {
        this._entityRangeIRI = _entityRangeIRI;
    }
}
