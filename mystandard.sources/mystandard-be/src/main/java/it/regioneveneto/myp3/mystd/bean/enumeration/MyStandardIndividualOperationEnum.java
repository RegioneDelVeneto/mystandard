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
package it.regioneveneto.myp3.mystd.bean.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;

import java.util.HashMap;
import java.util.Map;

public enum MyStandardIndividualOperationEnum {

    ADD("add", "Inserimento"),
    REMOVE("remove", "Eliminazione"),
    MODIFY("modify", "Modifica"),
    PUBLISH("publish", "Pubblicazione");

    private String _operation;
    private String _description;

    MyStandardIndividualOperationEnum(String _operation) {
        this._operation = _operation;
    }

    MyStandardIndividualOperationEnum(String _operation, String _description) {
        this._operation = _operation;
        this._description = _description;
    }

    @JsonValue
    public String get_operation() {
        return _operation;
    }

    public String get_description() {
        return _description;
    }

    private static final Map<String, MyStandardIndividualOperationEnum> map = new HashMap<>(values().length, 1);

    static {
        for (MyStandardIndividualOperationEnum myStandardIndividualOperationEnum : values()) map.put(myStandardIndividualOperationEnum._operation, myStandardIndividualOperationEnum);
    }

    @JsonCreator
    public static MyStandardIndividualOperationEnum of(String operationCode) throws MyStandardException {
        MyStandardIndividualOperationEnum result = map.get(operationCode);
        if (result == null) {
            throw new MyStandardException("Operation code non gestito. Operation code: " + operationCode);
        }
        return result;
    }



}
