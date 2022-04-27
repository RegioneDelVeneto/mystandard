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

import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.fsm.CustomFSMReader;
import it.regioneveneto.myp3.mystd.service.StateMachineService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StateMachineServiceImpl implements StateMachineService {

    private static final Logger LOG = LoggerFactory.getLogger(StateMachineServiceImpl.class);

    @Autowired
    private MyStandardProperties myStandardProperties;


    @Override
    @Cacheable(value = MyStandardConstants.CACHE_STATE_MACHINE)
    public List<String> calculateStateMachine(String stateMachineConfig) throws MyStandardException {

        LOG.debug("MyStandard - Si ottiene la state machine dal file {}", stateMachineConfig);

        List<JSONObject> jsonObjectList = new ArrayList<>();
        if (StringUtils.hasText(stateMachineConfig)) {
            jsonObjectList = getStateMachine(stateMachineConfig);
        } else {
            jsonObjectList = getStateMachine(myStandardProperties.getStateMachine());
        }

        LOG.debug("MyStandard - Estratti {} elementi", jsonObjectList.size());
        return jsonObjectList.stream().map(element -> element.toString()).collect(Collectors.toList());
    }

    private List<JSONObject> getStateMachine(String stateMachineConfig) throws MyStandardException {
        List<JSONObject> stateMachine = new ArrayList<>();
        try {
            CustomFSMReader customFSMReader = new CustomFSMReader( stateMachineConfig);
            List<String> states = customFSMReader.getStates();
            for (String state: states) {
                JSONObject stateObject = new JSONObject();
                stateObject.put(state, customFSMReader.getStateInfo
                        (state));
                stateMachine.add(stateObject);
            }


        } catch (ParserConfigurationException e) {
            LOG.error("Errore nel parsing della configurazione degli stati per {}", stateMachineConfig, e);
            throw new MyStandardException("Errore nel parsing della configurazione degli stati");
        } catch (SAXException e) {
            LOG.error("Errore nel parsing XML della configurazione degli stati per {}", stateMachineConfig, e);
            throw new MyStandardException("Errore nel parsing XML della configurazione degli stati");
        } catch (IOException e) {
            LOG.error("Errore I/O nella lettura della configurazione degli stati per {}", stateMachineConfig, e);
            throw new MyStandardException("Errore I/O nella lettura della configurazione degli stati");

        }

        return stateMachine;
    }
}
