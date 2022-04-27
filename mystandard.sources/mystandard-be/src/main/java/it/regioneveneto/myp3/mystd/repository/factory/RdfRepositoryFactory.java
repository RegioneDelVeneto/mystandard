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
package it.regioneveneto.myp3.mystd.repository.factory;

import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardMyPortalFilter;
import it.regioneveneto.myp3.mystd.bean.generic.MyStandardQueryRequest;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntity;
import it.regioneveneto.myp3.mystd.config.MyStandardConfig;
import it.regioneveneto.myp3.mystd.config.OwlJenaConfig;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.repository.EntityDataRepository;
import it.regioneveneto.myp3.mystd.repository.jena.impl.JenaEntityDataRepository;
import it.regioneveneto.myp3.mystd.repository.jena.impl.JenaFusekiRepositoryType;
import it.regioneveneto.myp3.mystd.repository.jena.impl.JenaLocalRepositoryType;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.service.AttachmentsService;
import it.regioneveneto.myp3.mystd.service.StoricoService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Service
public class RdfRepositoryFactory implements EntityDataRepository {

    @Value("${mystandard.owl.rdf.framework}")
    String rdfFramework;

    @Value("${mystandard.owl.rdf.repository-type}")
    String rdfRepositoryType;

    @Autowired
    private OwlJenaConfig owlJenaConfig;

    @Autowired
    private MyStandardConfig mystandardConfig;

    @Autowired
    private MyStandardProperties myStandardProperties;

    @Autowired
    private AttachmentsService attachmentsService;

    @Autowired
    private StoricoService storicoService;

    @Autowired
    private MessageSource messageSource;


    private EntityDataRepository getEntityDataRepository() throws MyStandardException {


        if (MyStandardConstants.JENA_FRAMEWORK.equals(rdfFramework)) {

            if (MyStandardConstants.JENA_RDF_REPOSITORY_FUSEKI.equals(rdfRepositoryType)) {
                return new JenaEntityDataRepository(new JenaFusekiRepositoryType(), owlJenaConfig, mystandardConfig, myStandardProperties, attachmentsService, storicoService, messageSource);
            } else if (MyStandardConstants.JENA_RDF_REPOSITORY_LOCAL.equals(rdfRepositoryType)) {
                return new JenaEntityDataRepository(new JenaLocalRepositoryType(), owlJenaConfig, mystandardConfig, myStandardProperties, attachmentsService, storicoService, messageSource);

            } else {
                throw new MyStandardException("Repository " + rdfRepositoryType + " non previsto per Jena");
            }
        } else {
            throw new MyStandardException("Nessun Framework definito nel file di configurazione");
        }
    }


    @Override
    public JSONObject getSingleEntityData(String entityType, String codice, Integer versione) throws MyStandardException, IOException, URISyntaxException {
        return getEntityDataRepository().getSingleEntityData(entityType, codice, versione);
    }

    @Override
    public List<JSONObject> getAll(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException, URISyntaxException {
        return getEntityDataRepository().getAll(dominio, entityType, filter);
    }

    @Override
    public List<JSONObject> getAllByCodiceAndVersione(String entityType, String codice, Integer versione) throws MyStandardException {
        return getEntityDataRepository().getAllByCodiceAndVersione(entityType, codice, versione);
    }

    @Override
    public void addEntityIndividual(String domain, String entityType, MyStandardEntity entity, Map<String, InputStream> allegatiMap, UserWithAdditionalInfo user) throws MyStandardException, IOException {
        getEntityDataRepository().addEntityIndividual(domain, entityType, entity, allegatiMap, user);
    }

    @Override
    public void updateEntityIndividual(String domain, String entityType, MyStandardEntity entity, Map<String, InputStream> allegatiMap, String username) throws MyStandardException, IOException {
        getEntityDataRepository().updateEntityIndividual(domain, entityType, entity, allegatiMap, username);
    }

    @Override
    public void publishEntityIndividual(String entityType, String codice, Integer versione, String username) throws MyStandardException, IOException {
        getEntityDataRepository().publishEntityIndividual(entityType, codice, versione, username);
    }

    @Override
    public void deleteEntityIndividual(String entityType, String codice, Integer versione, String username, String note) throws MyStandardException, IOException {
        getEntityDataRepository().deleteEntityIndividual(entityType, codice, versione, username, note);
    }

    @Override
    public byte[] exportRdfStatements() throws MyStandardException {
        return getEntityDataRepository().exportRdfStatements();
    }

    @Override
    public List<JSONObject> findRelazioniByEntitaCodiceVersione(String entita, String codice, Integer versione, MyStandardFilter filter) throws MyStandardException {
        return getEntityDataRepository().findRelazioniByEntitaCodiceVersione(entita, codice, versione, filter);
    }

    @Override
    public List<JSONObject> findAllRelazioni(String entita, String dominio, MyStandardFilter filter) throws MyStandardException {
        return getEntityDataRepository().findAllRelazioni(entita, dominio, filter);
    }

    @Override
    public JSONObject executeQuery(MyStandardQueryRequest queryRequest) throws IOException, MyStandardException {
        return getEntityDataRepository().executeQuery(queryRequest);
    }


    @Override
    public JSONObject findDatiDaVocabolario(String type, String codeIRI, String descIRI, String objPropIRI) throws MyStandardException {
        return getEntityDataRepository().findDatiDaVocabolario(type, codeIRI, descIRI, objPropIRI);
    }

    @Override
    public JSONObject findMaxVersioneByCodice(String entita, String codice) throws MyStandardException {
        return getEntityDataRepository().findMaxVersioneByCodice(entita, codice);
    }

    @Override
    public Integer countAll(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException {
        return getEntityDataRepository().countAll(dominio, entityType, filter);
    }

    @Override
    public String findIpaCodeDefinitoDaIdEntita(String idEntita) throws MyStandardException {
        return getEntityDataRepository().findIpaCodeDefinitoDaIdEntita(idEntita);
    }

    @Override
    public List<JSONObject>  getStatoAndIpaSingleEntityData(String entityType, String codice, Integer versione) throws MyStandardException, IOException, URISyntaxException {
        return getEntityDataRepository().getStatoAndIpaSingleEntityData(entityType, codice, versione);
    }

    @Override
    public void updateEntityState(String operazione, String entityType, String codice, Integer versione, String originalState, String nextState, String username, String note) throws MyStandardException {
        getEntityDataRepository().updateEntityState(operazione, entityType, codice, versione, originalState, nextState, username, note);
    }

    @Override
    public List<JSONObject> findAllBacheca(MyStandardFilter filter) throws MyStandardException {
        return getEntityDataRepository().findAllBacheca(filter);
    }

    @Override
    public Integer countAllBacheca(MyStandardFilter filter) throws MyStandardException {
        return getEntityDataRepository().countAllBacheca(filter);
    }

    @Override
    public Integer countAllRelazioni(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException {
        return getEntityDataRepository().countAllRelazioni(dominio, entityType, filter);
    }

    @Override
    public Integer countAllRelazioniByEntitaCodiceVersione(String entita, String codice, Integer versione, MyStandardFilter filter) throws MyStandardException {
        return getEntityDataRepository().countAllRelazioniByEntitaCodiceVersione(entita, codice, versione, filter);
    }
    public Boolean checkIfEntityPublishedRecently(String entityIRI) throws MyStandardException {
        return getEntityDataRepository().checkIfEntityPublishedRecently(entityIRI);
    }

    @Override
    public String getDataUltimoAggiornamento() throws MyStandardException {
        return getEntityDataRepository().getDataUltimoAggiornamento();
    }

    @Override
    public List<JSONObject> getEntitaCatalogo(MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {
        return getEntityDataRepository().getEntitaCatalogo(myStandardMyPortalFilter);
    }

    @Override
    public Integer countEntitaCatalogo(MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {
        return getEntityDataRepository().countEntitaCatalogo(myStandardMyPortalFilter);
    }

    @Override
    public void testFusekiConnection() throws MyStandardException {
        getEntityDataRepository().testFusekiConnection();

    }
}
