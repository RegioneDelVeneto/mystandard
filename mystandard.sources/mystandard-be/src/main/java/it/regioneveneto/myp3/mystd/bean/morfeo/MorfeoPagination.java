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
package it.regioneveneto.myp3.mystd.bean.morfeo;

import java.util.List;

public class MorfeoPagination {

    public List<Integer> sizeOptions;

    public MorfeoPagination() {}

    public MorfeoPagination(List<Integer> sizeOptions) {
        this.sizeOptions = sizeOptions;
    }

    //Getter and setter
    public List<Integer> getSizeOptions() {
        return sizeOptions;
    }

    public void setSizeOptions(List<Integer> sizeOptions) {
        this.sizeOptions = sizeOptions;
    }
}
