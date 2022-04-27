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
package it.regioneveneto.myp3.mystd.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardQueryFilter;
import it.regioneveneto.myp3.mystd.bean.filter.PageableFilter;
import it.regioneveneto.myp3.mystd.bean.mongodb.QueryDocument;
import it.regioneveneto.myp3.mystd.bean.mongodb.QueryParam;
import it.regioneveneto.myp3.mystd.bean.pagination.DatatablePaginated;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.repository.mongodb.QueryRepository;
import it.regioneveneto.myp3.mystd.service.QueryManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QueryManagementServiceImpl implements QueryManagementService {

    @Autowired
    private QueryRepository queryRepository;



    @Override
    public DatatablePaginated findAllQuery(MyStandardQueryFilter myStandardQueryFilter) {
        Integer pageNum = myStandardQueryFilter.getPageNum() != null ? myStandardQueryFilter.getPageNum() : 0;
        Integer pageSize = myStandardQueryFilter.getPageSize() != null ? myStandardQueryFilter.getPageSize() : 10;

        Pageable pageable = PageRequest.of(pageNum, pageSize, getSortFromFilter(myStandardQueryFilter));

        Page<QueryDocument> page = getQueryContentPaginated(myStandardQueryFilter, pageable);
        //Page<QueryDocument> page = queryRepository.findByNameLikeAndDescriptionLike("Dettaglio", "mostra tutti", pageable);
        List<QueryDocument> queryResult = page.getContent();
        Integer totalRecords = Math.toIntExact(page.getTotalElements());

        List mapData = queryResult.stream().map(record -> new ObjectMapper().convertValue(record, Map.class))
                .collect(Collectors.toList());
        return new DatatablePaginated(mapData, totalRecords);

    }

    private Page<QueryDocument> getQueryContentPaginated(MyStandardQueryFilter myStandardQueryFilter, Pageable pageable) {

        String description = myStandardQueryFilter.getDescription();
        String name = myStandardQueryFilter.getName();

        if (StringUtils.hasText(name) && StringUtils.hasText(description)) {
            return queryRepository.findByNameLikeAndDescriptionLike(name, description, pageable);
        } else if (StringUtils.hasText(name)) {
            return queryRepository.findByNameLike(name, pageable);
        } else if (StringUtils.hasText(description)) {
            return queryRepository.findByDescriptionLike(description, pageable);
        } else {
            return queryRepository.findAll(pageable);
        }
    }

    @Override
    public Optional<QueryDocument> findQueryById(String queryId) throws MyStandardException {
        Optional<QueryDocument> query = queryRepository.findById(queryId);
        if (query.isPresent()) return query;
        else throw new MyStandardException("Query by id non esistente");

    }

    @Override
    public void deleteQueryById(String queryId) throws MyStandardException {
        if (queryRepository.existsById(queryId)) queryRepository.deleteById(queryId);
        else throw new MyStandardException("Impossibile eliminare la query. Id " + queryId + " non esistente.");
    }

    @Override
    public QueryDocument insertQueryDocument(QueryDocument queryDocument) throws MyStandardException {
        if (StringUtils.hasText(queryDocument.getId()) && queryRepository.existsById(queryDocument.getId()))
            throw new MyStandardException("Impossibile inserire la query. Id " + queryDocument.getId() + " già esistente.");
        else return queryRepository.insert(queryDocument);
    }

    @Override
    public QueryDocument updateQueryDocument(QueryDocument queryDocument) throws MyStandardException {
        if (StringUtils.hasText(queryDocument.getId()) && queryRepository.existsById(queryDocument.getId())) return queryRepository.save(queryDocument);
        else throw new MyStandardException("Impossibile modificare la query. Id " + queryDocument.getId() + " non esistente.");
    }

    @Override
    public List<QueryParam> findQueryParamsById(String queryId) throws MyStandardException {
        Optional<QueryDocument> query = queryRepository.findById(queryId);
        if (query.isPresent()) return query.get().getParams();
        else throw new MyStandardException("Query by id non esistente");
    }

    /**
     * Si ottiene oggetto sort con i filtri di sorting ricevuti in input
     * @param pageableFilter, filtri di sorting in input
     * @return oggetto sort
     */
    private Sort getSortFromFilter(PageableFilter pageableFilter) {

        Sort pageableSort = Sort.unsorted();//Default value

        if (StringUtils.hasText(pageableFilter.getSortField())) {
            if (StringUtils.hasText(pageableFilter.getSortDirection())) {
                pageableSort = pageableFilter.getSortDirection().equals("desc") ? Sort.by(pageableFilter.getSortField()).descending() : Sort.by(pageableFilter.getSortField());
            } else {
                pageableSort = Sort.by(pageableFilter.getSortField());
            }

        }

        return pageableSort;
    }
}
