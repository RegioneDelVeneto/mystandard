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

public enum MyStandardStatoEnum {

    INSERITO("Inserito"),
    PUBBLICATO("Pubblicato"),
    PUBBLICATO_ENTE("Pubblicato_Ente"),
    ELIMINATO("Eliminato");

    private String code;

    MyStandardStatoEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
