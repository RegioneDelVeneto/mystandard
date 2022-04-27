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
package it.regioneveneto.myp3.mystd.repository;

import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardMyPortalFilter;
import it.regioneveneto.myp3.mystd.bean.generic.MyStandardQueryRequest;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntity;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface EntityDataRepository {

    JSONObject getSingleEntityData(String entityType, String codice, Integer versione) throws MyStandardException, IOException, URISyntaxException;

    List<JSONObject>  getStatoAndIpaSingleEntityData(String entityType, String codice, Integer versione) throws MyStandardException, IOException, URISyntaxException;

    List<JSONObject> getAll(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException, URISyntaxException;

    List<JSONObject> getAllByCodiceAndVersione(String entityType, String codice, Integer versione) throws MyStandardException;

    void addEntityIndividual(String domain, String entityType, MyStandardEntity entity, Map<String, InputStream> allegatiMap, UserWithAdditionalInfo user) throws MyStandardException, IOException;

    void updateEntityIndividual(String domain, String entityType, MyStandardEntity entity, Map<String, InputStream> allegatiMap, String username) throws MyStandardException, IOException;

    void publishEntityIndividual(String entityType, String codice, Integer versione, String username) throws MyStandardException, IOException;

    void deleteEntityIndividual(String entityType, String codice, Integer versione, String username, String note) throws MyStandardException, IOException;

    byte[] exportRdfStatements() throws MyStandardException;

    List<JSONObject> findRelazioniByEntitaCodiceVersione(String entita, String codice, Integer versione, MyStandardFilter filter) throws MyStandardException;

    List<JSONObject> findAllRelazioni(String entita, String dominio, MyStandardFilter filter) throws MyStandardException;

    JSONObject executeQuery(MyStandardQueryRequest queryRequest) throws IOException, MyStandardException;

    JSONObject findDatiDaVocabolario(String type, String codeIRI, String descIRI, String objPropIRI) throws MyStandardException;

    JSONObject findMaxVersioneByCodice(String entita, String codice) throws MyStandardException;

    Integer countAll(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException;

    String findIpaCodeDefinitoDaIdEntita(String idEntita) throws MyStandardException;

    void updateEntityState(String operazione, String entityType, String codice, Integer versione, String originalState, String nextState, String username, String note) throws MyStandardException;

    List<JSONObject> findAllBacheca(MyStandardFilter filter) throws MyStandardException;

    Integer countAllBacheca(MyStandardFilter filter) throws MyStandardException;

    Integer countAllRelazioni(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException;

    Integer countAllRelazioniByEntitaCodiceVersione(String entita, String codice, Integer versione, MyStandardFilter filter) throws MyStandardException;

    Boolean checkIfEntityPublishedRecently(String entityIRI) throws MyStandardException;

    String getDataUltimoAggiornamento() throws MyStandardException;

    List<JSONObject> getEntitaCatalogo(MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException;

    Integer countEntitaCatalogo(MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException;

    void testFusekiConnection() throws MyStandardException;
}
