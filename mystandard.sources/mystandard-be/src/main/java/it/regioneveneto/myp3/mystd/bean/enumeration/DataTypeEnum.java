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

public enum DataTypeEnum {

    ANYURI("anyURI", "textfield"),
    ABI_REGEX("abiRegex", "number"),
    SEGREGATION_CODE_REGEX("segregationCodeRegex", "number"),
    COLUMN("custom_repeatable", "columns"),
    DATETIME("dateTime", "datetime"),
    INT("int", "number", "^[0-9]*$"),
    INTEGER("integer", "number", "^[0-9]*$"),
    LITERAL("literal", "textfield"),
    STRING("string", "textfield"),
    EDITOR("editor", "codeEditor"),
    URL_IMAGE("urlImage", "image"),
    BOOLEAN("boolean", "checkbox"),
    DECIMAL("decimal", "textfield", "^([0-9]+(?:[,][0-9]{2})?)$");

    private String type;
    private String morfeoType;
    private String regex;

    DataTypeEnum(String type, String morfeoType) {

        this.type = type;
        this.morfeoType = morfeoType;
    }

    DataTypeEnum(String type, String morfeoType, String regex) {

        this.type = type;
        this.morfeoType = morfeoType;
        this.regex = regex;
    }

    public String getType() {
        return type;
    }

    public String getMorfeoType() {
        return morfeoType;
    }

    public String getRegex() {
        return regex;
    }

    private static final Map<String, DataTypeEnum> map = new HashMap<>(values().length, 1);

    static {
        for (DataTypeEnum dataTypeEnum : values()) map.put(dataTypeEnum.type, dataTypeEnum);
    }


    public static DataTypeEnum of(String type) throws MyStandardException {
        DataTypeEnum result = map.get(type);
        if (result == null) {
            throw new MyStandardException("Tipo datatype non gestito. Entità: " + type);
        }
        return result;
    }
}
