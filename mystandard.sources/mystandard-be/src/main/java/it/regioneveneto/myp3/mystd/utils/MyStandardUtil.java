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
package it.regioneveneto.myp3.mystd.utils;

import it.regioneveneto.myp3.mystd.bean.filter.PageableFilter;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

public class MyStandardUtil {

    //Datetime and date pattern
    public static final String DATETIME_PATTERN = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String ALTERNATIVE_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN_WITH_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String EUROPE_ROME_TIMEZONE = "Europe/Rome";
    public static final DateTimeFormatter MYSTANDARD_DATETIME_ZONED_FORMATTER = DateTimeFormatter.ofPattern(MyStandardUtil.DATETIME_PATTERN_WITH_ZONE).withZone(ZoneId.of(MyStandardUtil.EUROPE_ROME_TIMEZONE));


    /**
     * Si ritorna una lista vuota se null
     * @param list list
     * @param <T> TYpe
     * @return list empty if null
     */
    public static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * Si converte dateTime al pattern richiesto (prima con zone, poi senza zone). Se arriva un date, si converte al pattern date richiesto
     * @param dateTimeString, datetime o date come stringa
     * @return datetime o date formattato
     */
    public static String convertDateTimePattern(String dateTimeString) {

        try {
            if (StringUtils.hasText(dateTimeString)) {
                return ZonedDateTime.parse(dateTimeString, MYSTANDARD_DATETIME_ZONED_FORMATTER).format(DateTimeFormatter.ofPattern(DATETIME_PATTERN).withZone(ZoneId.of(EUROPE_ROME_TIMEZONE)));
            } else return "";
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeString).format( DateTimeFormatter.ofPattern(DATETIME_PATTERN));
            } catch (DateTimeParseException ex) {
                return parseLocalDate(dateTimeString, DateTimeFormatter.ofPattern(DATE_PATTERN));
            }

        }
    }

    /**
     * Si converte il date
     * @param dateStr
     * @param dateFormatter
     * @return
     */
    public static String parseLocalDate(String dateStr, DateTimeFormatter dateFormatter) {
        String date = "";
        try {
            String toParse = dateStr.replaceAll("(\\^\\^.+)","");
            LocalDate localDate = LocalDate.parse(toParse);
            date = localDate.format(dateFormatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * Si elencano relazioni speciali non modificabili dall'utente
     * @param key, chiave della relazione da verificare
     * @return true se relazione speciale, false altrimenti
     */
    public static boolean relationshipToBeShowedOnSummary(MyStandardProperties myStandardProperties, String key) {
        return myStandardProperties.getOwl().getDefinitaDaUri().equalsIgnoreCase(key)
                || myStandardProperties.getOwl().getSpecializzaUri().equalsIgnoreCase(key);
    }


    public static String mystandardPrefixForMorfeo() {
        return MyStandardConstants.MYSTD_PREFIX_KEY + "_";
    }

    public static String mystandardPrefixForMorfeo(String objectPropName, String dataPropName) {
        return MyStandardConstants.MYSTD_PREFIX_KEY + "_" + (StringUtils.hasText(objectPropName) ? objectPropName + "_" : "") + dataPropName;
    }



    /**
     * Se settano a null le informazioni del filtro sulla paginazione
     * @param filter, oggetto di cui settare la paginazione a null
     */
    public static void setPaginationFiltersAsNull(PageableFilter filter) {
        filter.setPageNum(null);
        filter.setPageSize(null);
        filter.setSortDirection(null);
        filter.setSortField(null);
    }

    /**
     * Si verifica se l'utente è autenticato oppure no
     * @return true se autenticato, false se non autenticato
     */
    public static boolean isUserAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                //when Anonymous Authentication is enabled
                !(SecurityContextHolder.getContext().getAuthentication()
                        instanceof AnonymousAuthenticationToken);
    }
}
