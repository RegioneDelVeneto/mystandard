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

public class MyStandardFunctionalPropertyIndividual {

    private String _functionalPropertyIRI;
    private String _targetIndividualsIRI;
    private MyStandardIndividualOperationEnum _operation;

    // Getter and setter


    public String get_functionalPropertyIRI() {
        return _functionalPropertyIRI;
    }

    public void set_functionalPropertyIRI(String _functionalPropertyIRI) {
        this._functionalPropertyIRI = _functionalPropertyIRI;
    }

    public String get_targetIndividualsIRI() {
        return _targetIndividualsIRI;
    }

    public void set_targetIndividualsIRI(String _targetIndividualsIRI) {
        this._targetIndividualsIRI = _targetIndividualsIRI;
    }

    public MyStandardIndividualOperationEnum get_operation() {
        return _operation;
    }

    public void set_operation(MyStandardIndividualOperationEnum _operation) {
        this._operation = _operation;
    }
}
