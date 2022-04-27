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

public enum EntityTypeEnum {

    THING("Thing"),
    ENTITA_GENERICA("EntitàGenerica", "entitaGenerica"),
    DATI_DI_BASE("Dati di Base"),
    ENTITA_STRUTTURATA("EntitàStrutturata"),
    PRIVATE_ORGANIZATION("Azienda"),
    ORGANIZATION("Organization"),
    ORGANIZZAZIONE("Organizzazione"),
    ENTE("Ente", "ente"),
    API("API", "api"),
    AZIENDA_ICT("AziendaICT", "aziendaICT"),
    PROCESSO("Processo", "processo");

    private String type;
    private String frontendType;

    EntityTypeEnum(String type) {
        this.type = type;
    }

    EntityTypeEnum(String type, String frontendType) {
        this.type = type;
        this.frontendType = frontendType;
    }

    public String getType() {
        return type;
    }

    public String getFrontendType() {
        return frontendType;
    }


    private static final Map<String, EntityTypeEnum> map = new HashMap<>(values().length, 1);
    private static final Map<String, EntityTypeEnum> mapFrontend = new HashMap<>(values().length, 1);

    static {
        for (EntityTypeEnum entityTypeEnum : values()) {
            map.put(entityTypeEnum.type, entityTypeEnum);
            mapFrontend.put(entityTypeEnum.frontendType, entityTypeEnum);
        }
    }


    public static EntityTypeEnum of(String type) throws MyStandardException {
        EntityTypeEnum result = map.get(type);
        if (result == null) {
            throw new MyStandardException("Tipo entità non gestita. Entità: " + type);
        }
        return result;
    }

    public static EntityTypeEnum ofFrontendType(String frontendType) throws MyStandardException {
        EntityTypeEnum result = mapFrontend.get(frontendType);
        if (result == null) {
            throw new MyStandardException("Tipo entità non gestita. Entità: " + frontendType);
        }
        return result;
    }

}
