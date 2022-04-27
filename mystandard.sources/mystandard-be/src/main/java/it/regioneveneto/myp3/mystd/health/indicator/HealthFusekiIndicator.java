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
package it.regioneveneto.myp3.mystd.health.indicator;

import it.regioneveneto.myp3.mystd.repository.EntityDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("fuseki")
public class HealthFusekiIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(HealthFusekiIndicator.class);

    @Autowired
    private EntityDataRepository entityDataRepository;

    @Override
    public Health health() {

        try {
            entityDataRepository.testFusekiConnection();

            LOG.info("MyStandard - Fuseki connection funziona correttamente.");

            return Health.up().build();
        } catch (Exception e) {
            LOG.error("MyStandard - Errore nella verifica del funzionamento della connessione a Fuseki", e);
            return Health.outOfService().withException(e).build();
        }

    }
}
