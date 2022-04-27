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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MorfeoComponentColumn {

    private String label;
    private String value;
    private List<MorfeoComponentButton> buttons;

    public MorfeoComponentColumn(String label, String value, List<MorfeoComponentButton> buttons) {
        this.label = label;
        this.value = value;
        this.buttons = buttons;
    }

    public MorfeoComponentColumn(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public MorfeoComponentColumn() {
    }

    //Getter and setter
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<MorfeoComponentButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<MorfeoComponentButton> buttons) {
        this.buttons = buttons;
    }
}
