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

import java.util.ArrayList;
import java.util.List;

public class MyStandardMorfeoDetail {

    private MorfeoElement masterFields;
    private MorfeoElement tabsFields;
    private MorfeoElement attachments;
    private MorfeoElement tableAttachments;

    //Getter and setter
    public MorfeoElement getMasterFields() {
        return masterFields;
    }

    public void setMasterFields(MorfeoElement masterFields) {
        this.masterFields = masterFields;
    }

    public MorfeoElement getTabsFields() {
        return tabsFields;
    }

    public void setTabsFields(MorfeoElement tabsFields) {
        this.tabsFields = tabsFields;
    }

    public MorfeoElement getAttachments() {
        return attachments;
    }

    public void setAttachments(MorfeoElement attachments) {
        this.attachments = attachments;
    }

    public void setTableAttachments(MorfeoElement attachments) {
        this.tableAttachments = attachments;
    }

    public List<MorfeoElement> getAllElements() {
        
        List<MorfeoElement> elements = new ArrayList<>();
        elements.add(masterFields);
        elements.add(tabsFields);
        elements.add(attachments);
        elements.add(tableAttachments);

        return elements;
    }
}
