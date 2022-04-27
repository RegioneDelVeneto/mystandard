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
package it.regioneveneto.myp3.mystd.exception;

import java.util.List;
import java.util.Map;

public class MyStandardException extends Exception {
    private static final long serialVersionUID = 1L;

    private List<Map<String, String>> errors;

    public MyStandardException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public MyStandardException(String errorMessage) {
        super(errorMessage);
    }

    public MyStandardException(String errorMessage, List<Map<String, String>> errors) {
        super(errorMessage);
        this.errors = errors;
    }


    public MyStandardException(String errorMessage, Throwable err, List<Map<String, String>> errors) {
        super(errorMessage, err);
        this.errors = errors;
    }

    public List<Map<String, String>> getErrors() {
        return errors;
    }
}
