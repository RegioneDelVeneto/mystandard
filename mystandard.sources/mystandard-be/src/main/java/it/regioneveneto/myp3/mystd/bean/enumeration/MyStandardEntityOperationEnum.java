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

import it.regioneveneto.myp3.mystd.exception.MyStandardException;

import java.util.HashMap;
import java.util.Map;

public enum MyStandardEntityOperationEnum {

    PUBBLICA("PUBBLICA", "Pubblicazione"),
    MODIFICA("MODIFICA", "Modifica"),
    APPROVA("APPROVA", "Approvazione"),
    RIFIUTA("RIFIUTA", "Operazione di rifiuto"),
    TRASMETTI("TRASMETTI", "Trasmissione"),
    PUBBLICA_COME_STANDARD("PUBBLICA_COME_STANDARD", "Pubblicazione come Standard"),
    RIFIUTA_COME_STANDARD("RIFIUTA_COME_STANDARD", "Operazione di rifiuto come Standard"),
    ELIMINA("ELIMINA", "Eliminazione");


    private String operation;
    private String description;


    MyStandardEntityOperationEnum(String operation, String description) {
        this.operation = operation;
        this.description = description;
    }

    private static final Map<String, MyStandardEntityOperationEnum> map = new HashMap<>(values().length, 1);

    static {
        for (MyStandardEntityOperationEnum myStandardEntityOperationEnum : values()) map.put(myStandardEntityOperationEnum.operation, myStandardEntityOperationEnum);
    }


    public static MyStandardEntityOperationEnum of(String operation) throws MyStandardException {
        MyStandardEntityOperationEnum result = map.get(operation);
        if (result == null) {
            throw new MyStandardException("Tipo operation non gestito. Entità: " + operation);
        }
        return result;
    }

    public String getOperation() {
        return operation;
    }

    public String getDescription() {
        return description;
    }
}
