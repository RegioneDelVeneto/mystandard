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

import it.regioneveneto.myp3.mystd.service.AttachmentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("ceph")
public class HealthCephIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCephIndicator.class);

    @Autowired
    private AttachmentsService attachmentsService;

    @Override
    public Health health() {

        try {

            //Scrittura file

            String idEntita = UUID.randomUUID().toString();
            String idAllegato = attachmentsService.putTestFile(idEntita);

            LOG.debug("MyStandard - Put allegato di test su ceph eseguita correttamente.");

            //Lettura file
            attachmentsService.get(idEntita, idAllegato);

            LOG.debug("MyStandard - Get allegato di test su ceph eseguita correttamente.");
            //Cancellazione file
            attachmentsService.delete(idEntita, idAllegato);

            LOG.debug("MyStandard - Delete allegato di test su ceph eseguita correttamente.");

            LOG.info("MyStandard - Ceph connection funziona correttamente.");

            return Health.up().build();
        } catch (Exception e) {
            LOG.error("MyStandard - Errore nella verifica del funzionamento della connessione a Ceph", e);
            return Health.outOfService().withException(e).build();
        }

    }
}
