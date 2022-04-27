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

public enum MyStandardRoleEnum {

    OPERATORE("OPERATORE", "OPERATORE"),
    RESPONSABILE_DOMINIO("ROLE_RESPONSABILE_DOMINIO", "RESPONSABILE_DOMINIO"),
    RESPONSABILE_STANDARD("ROLE_RESPONSABILE_STANDARD", "RESPONSABILE_STANDARD"),
    OPERATORE_ENTE_NAZIONALE("ROLE_OPERATORE_EN"),
    OPERATORE_ENTE_LOCALE("ROLE_OPERATORE_EE_LL");

    private String role;
    private String myProfileRole;

    MyStandardRoleEnum(String role) {
        this.role = role;
    }

    MyStandardRoleEnum(String role, String myProfileRole) {
        this.role = role;
        this.myProfileRole = myProfileRole;
    }

    public String getRole() {
        return role;
    }

    public String getMyProfileRole() {
        return myProfileRole;
    }
}
