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

import it.regioneveneto.myp3.mystd.bean.filter.MyStandardQueryFilter;
import it.regioneveneto.myp3.mystd.bean.mongodb.QueryDocument;
import it.regioneveneto.myp3.mystd.bean.mongodb.QueryParam;
import it.regioneveneto.myp3.mystd.bean.pagination.DatatablePaginated;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;

import java.util.List;
import java.util.Optional;

public interface QueryManagementService {

    DatatablePaginated findAllQuery(MyStandardQueryFilter myStandardQueryFilter);

    Optional<QueryDocument> findQueryById(String queryId) throws MyStandardException;

    void deleteQueryById(String queryId) throws MyStandardException;

    QueryDocument insertQueryDocument(QueryDocument queryDocument) throws MyStandardException;

    QueryDocument updateQueryDocument(QueryDocument queryDocument) throws MyStandardException;

    List<QueryParam> findQueryParamsById(String id) throws MyStandardException;
}
