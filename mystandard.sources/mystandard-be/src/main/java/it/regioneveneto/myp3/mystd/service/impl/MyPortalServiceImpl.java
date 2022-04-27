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
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardMyPortalFilter;
import it.regioneveneto.myp3.mystd.bean.filter.PageableFilter;
import it.regioneveneto.myp3.mystd.bean.owl.OClass;
import it.regioneveneto.myp3.mystd.bean.owl.OIdentificable;
import it.regioneveneto.myp3.mystd.bean.owl.OModel;
import it.regioneveneto.myp3.mystd.bean.pagination.DatatablePaginated;
import it.regioneveneto.myp3.mystd.config.MyStandardConfig;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.repository.EntityDataRepository;
import it.regioneveneto.myp3.mystd.service.MyPortalService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import it.regioneveneto.myp3.mystd.utils.MyStandardUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MyPortalServiceImpl implements MyPortalService {

    private static final Logger LOG = LoggerFactory.getLogger(MyPortalServiceImpl.class);

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    @Autowired
    private MyStandardConfig myStandardConfig;

    @Autowired
    private MyStandardProperties myStandardProperties;

    @Autowired
    private EntityDataRepository entityDataRepository;

    @Autowired
    private MessageSource messageSource;

    @Override
    public Object getDatiCatalogo(PageableFilter pageableFilter) throws MyStandardException {

        LOG.debug("MyStandard - Si ottengono i dati paginati da mostrare nel catalogo MyPortal");

        JSONArray jsonArray = new JSONArray();
        Map<String, OClass> classes = extractOwlClasses();
        for (Map.Entry<String, OClass> catalogoClass: classes.entrySet()) {
            OClass value = catalogoClass.getValue();
            JSONObject jsonClass = new JSONObject();
            jsonClass.put(MyStandardConstants.NAME_KEY, value.getLocalName());
            jsonClass.put(MyStandardConstants.LABEL_KEY, messageSource.getMessage(value.getLocalName(), null, value.getLocalName(), null));
            jsonClass.put(MyStandardConstants.DESC_KEY, value.getDescription());
            jsonClass.put(MyStandardConstants.IS_NEW_KEY, entityDataRepository.checkIfEntityPublishedRecently(value.getIRI()));
            jsonClass.put(MyStandardConstants.DOMINIO_BUSINESS_KEY, MyStandardConstants.DOMINIO_GENERALE);
            jsonArray.put(jsonClass);
        }

        Integer totalRecords = jsonArray.length();
        List<Object> values = getPaginatedCatalogoValues(jsonArray, pageableFilter, totalRecords);

        LOG.debug("MyStandard - Sono stati estratti {} dati da mostrare nel catalogo", totalRecords);

        List mapData = values.stream().map(record -> new ObjectMapper().convertValue(record, Map.class))
                .collect(Collectors.toList());
        return new DatatablePaginated(mapData, totalRecords);
    }

    /**
     * Paginazione manuale di un JSON Array
     * @param jsonArray, json array da paginare
     * @param pageableFilter, dati di paginazione
     * @param totalRecords, numero di records totali
     * @return json array paginato
     */
    private List<Object> getPaginatedCatalogoValues(JSONArray jsonArray, PageableFilter pageableFilter, Integer totalRecords) {

        List<Object> values;
        if (pageableFilter.getPageNum() != null && pageableFilter.getPageSize() != null) {
            List<Object> jsonArrayList = jsonArray.toList();
            Integer offset = (pageableFilter.getPageNum() * pageableFilter.getPageSize());
            if (offset > 0 && offset < totalRecords) {
                values = jsonArrayList.subList(offset, (totalRecords > (offset + pageableFilter.getPageSize()) ? (offset + pageableFilter.getPageSize()) : totalRecords));
            } else if (offset <= 0) {
                values = jsonArrayList.subList(0, totalRecords > pageableFilter.getPageSize() ? pageableFilter.getPageSize() : totalRecords);
            } else {
                values = new ArrayList<>();
            }

        } else {
            values = jsonArray.toList();
        }

        return values;

    }

    private Map<String, OClass> extractOwlClasses() throws MyStandardException {

        Map<String, OClass> classes = new TreeMap<>();
        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        if (oModelClasses == null || oModelClasses.size() == 0) {
            throw new MyStandardException("Impossibile tornare le info sul menu poichè nel model non c'è riferimento ad alcuna classe");
        } else {
            for (Map.Entry<String, OClass> entry : oModelClasses.entrySet()) {
                Map<String, List<OIdentificable>> annotationProperties = entry.getValue().getAnnotationProperties();
                if (annotationProperties.size() > 0 && annotationProperties.containsKey(myStandardProperties.getOwl().getMenuUri())) {
                    classes.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return classes;
    }

    @Override
    public JSONObject getDataUltimoAggiornamentoCatalogo() throws MyStandardException {

        LOG.debug("MyStandard - si estrae la data di ultimo aggiornamento catalogo");
        JSONObject dataUltimoAggiornamentoObject = new JSONObject();

        String dataUltimoAggiornamentoCatalogo = entityDataRepository.getDataUltimoAggiornamento();
        dataUltimoAggiornamentoObject.put(MyStandardConstants.DATA_ULTIMO_AGGIORNAMENTO_COLUMN_KEY, MyStandardUtil.convertDateTimePattern(dataUltimoAggiornamentoCatalogo) );

        LOG.debug("MyStandard - Estratta data ultimo aggiornamento catalogo: {}", dataUltimoAggiornamentoCatalogo);
        return dataUltimoAggiornamentoObject;
    }

    @Override
    public List<JSONObject> getListaDomini() throws MyStandardException {

        LOG.debug("MyStandard - Si estrae la lista dei domini definita in MyStandard");

        List<JSONObject> domini = new ArrayList<>();
        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        if (oModelClasses == null || oModelClasses.size() == 0) {
            throw new MyStandardException("Impossibile tornare le info sul menu poichè nel model non c'è riferimento ad alcuna classe");
        } else {
            for (Map.Entry<String, OClass> entry : oModelClasses.entrySet()) {
                Map<String, List<OIdentificable>> annotationProperties = entry.getValue().getAnnotationProperties();
                List<OIdentificable> oIdentificables = annotationProperties.get(myStandardProperties.getOwl().getMenuUri());
                for (OIdentificable identificable : MyStandardUtil.emptyIfNull(oIdentificables)) {
                    JSONObject dominioJson = new JSONObject();
                    dominioJson.put(MyStandardConstants.PATH_KEY, identificable.getIRI());
                    dominioJson.put(MyStandardConstants.LABEL_KEY, identificable.getIRI());
                    Optional<JSONObject> optionalDominio = domini.stream().filter(dominio -> identificable.getIRI().equals(dominio.getString(MyStandardConstants.PATH_KEY))).findFirst();

                    if (!optionalDominio.isPresent()) {//Si aggiunge se non è già stato aggiunto
                        domini.add(dominioJson);
                    }
                }
            }
        }

        LOG.debug("MyStandard - Sono stati estratti {} domini", domini.size());

        return domini;
    }

    @Override
    public List<JSONObject> getListaTipiEntita() throws MyStandardException {

        LOG.debug("MyStandard - Si estrae la lista dei tipi entità tra i vari domini");

        List<JSONObject> tipiEntita = new ArrayList<>();
        OModel oModel = myStandardConfig.getModel();
        Map<String, OClass> oModelClasses = oModel.getClasses();
        if (oModelClasses == null || oModelClasses.size() == 0) {
            throw new MyStandardException("Impossibile tornare le info sul menu poichè nel model non c'è riferimento ad alcuna classe");
        } else {
            for (Map.Entry<String, OClass> entry : oModelClasses.entrySet()) {
                Map<String, List<OIdentificable>> annotationProperties = entry.getValue().getAnnotationProperties();
                if (annotationProperties.size() > 0 && annotationProperties.containsKey(myStandardProperties.getOwl().getMenuUri())) {
                    JSONObject tipoEntitaJson = new JSONObject();
                    tipoEntitaJson.put(MyStandardConstants.PATH_KEY, entry.getValue().getLocalName());
                    tipoEntitaJson.put(MyStandardConstants.LABEL_KEY, messageSource.getMessage(entry.getValue().getLocalName(), null, entry.getValue().getLocalName(), null));
                    tipiEntita.add(tipoEntitaJson);
                }
            }
        }

        LOG.debug("MyStandard - Sono stati estratti {} tipi entità", tipiEntita.size());

        return tipiEntita;
    }

    @Override
    public Object getListaEntitaByTipo(String tipoEntita, MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {

        LOG.debug("MyStandard - Si estrae una lista di entità a partire dal tipo");

        myStandardMyPortalFilter = myStandardMyPortalFilter == null ? new MyStandardMyPortalFilter() : myStandardMyPortalFilter;
        myStandardMyPortalFilter.setTipiEntita(Arrays.asList(tipoEntita));//Dipende da dove sono arrivato

        return getEntitaCatalogoPaginated(myStandardMyPortalFilter);


    }



    @Override
    public Object getListaEntitaByDominio(String dominio, MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {

        LOG.debug("MyStandard - Si estrae una lista di entità a partire dal dominio");

        myStandardMyPortalFilter = myStandardMyPortalFilter == null ? new MyStandardMyPortalFilter() : myStandardMyPortalFilter;
        myStandardMyPortalFilter.setDominio(dominio);

        return getEntitaCatalogoPaginated(myStandardMyPortalFilter);
    }

    private Object getEntitaCatalogoPaginated(MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {
        List<JSONObject> entitaCatalogo = entityDataRepository.getEntitaCatalogo(myStandardMyPortalFilter);

        MyStandardUtil.setPaginationFiltersAsNull(myStandardMyPortalFilter);
        Integer totalRecords = entityDataRepository.countEntitaCatalogo(myStandardMyPortalFilter);//Numero totale, la lista jsonObject torna solo gli elementi paginati

        LOG.debug("MyStandard - Sono state estratte {} entità", totalRecords);

        List<Map<String, Object>> mapData = entitaCatalogo.stream().map(record -> record.toMap()).collect(Collectors.toList());
        return new DatatablePaginated(mapData, totalRecords);
    }
}
