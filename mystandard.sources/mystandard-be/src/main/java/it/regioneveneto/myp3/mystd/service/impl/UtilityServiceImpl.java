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

import it.regioneveneto.myp3.mystd.bean.generic.MyStandardQueryRequest;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.repository.EntityDataRepository;
import it.regioneveneto.myp3.mystd.service.UtilityService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class UtilityServiceImpl implements UtilityService {

  private static final Logger LOG = LoggerFactory.getLogger(UtilityServiceImpl.class);

  @Autowired
  private EntityDataRepository entityDataRepository;

  @Override
  public byte[] getExportRdfByteArray() throws MyStandardException {
    return entityDataRepository.exportRdfStatements();
  }

  @Override
  public JSONObject executeQuery(MyStandardQueryRequest queryRequest) throws IOException, MyStandardException {


    if (queryRequest != null) {

      LOG.debug("MyStandard - Richiesta esecuzione query");

      try {

        JSONObject queryResult = entityDataRepository.executeQuery(decodeQueryBase64(queryRequest));

        //Si riesegue la query con filtri nulli per ottenere il numero totale dei records
        setFilterNull(queryRequest);
        JSONObject allResults = entityDataRepository.executeQuery(queryRequest);
        queryResult.put("totalRecords", allResults.getJSONArray("values").length());

        LOG.debug("MyStandard - Estratte {} righe", allResults.getJSONArray("values").length());

        return queryResult;
      } catch (Exception e) {
        LOG.error("Errore nell'esecuzione della query custom SPARQL", e);
        throw new MyStandardException("Errore nell'esecuzione della query SparQL");
      }
    } else {
      throw new MyStandardException("La query ricevuta è nulla");
    }


  }

  /**
   * Si setta la query decodificata base64
   * @param queryRequest
   * @return
   */
  private MyStandardQueryRequest decodeQueryBase64(MyStandardQueryRequest queryRequest) {

    String query = queryRequest.getQuery();
    byte[] queryByteArray = Base64.getDecoder().decode(query.getBytes(StandardCharsets.UTF_8));
    queryRequest.setQuery(new String(queryByteArray,StandardCharsets.UTF_8));
    return queryRequest;

  }


  private void setFilterNull(MyStandardQueryRequest queryRequest) {
    queryRequest.setPageNum(null);
    queryRequest.setPageSize(null);
    queryRequest.setSortDirection(null);
    queryRequest.setSortField(null);
  }
}
