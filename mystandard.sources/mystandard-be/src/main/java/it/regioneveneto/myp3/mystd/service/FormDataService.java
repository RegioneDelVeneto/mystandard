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
package it.regioneveneto.myp3.mystd.service;

import it.regioneveneto.myp3.mystd.bean.owl.OModel;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import org.json.JSONObject;

import java.util.List;

public interface FormDataService {


    Object mergeStructureAndData(OModel omodel, JSONObject entityData, List<JSONObject> historicEntityData, String entityType, String owlPrefix, Boolean readOnly, Boolean isUserAuthenticated, Boolean skipDataInInverseTabs) throws MyStandardException;

    Object getStructureWithEmptyData(OModel oModel, String entityType, String owlPrefix) throws MyStandardException;

    Object getPaginatedDatatable(List<JSONObject> allDataFiltered, Integer totalRecords);
}