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

import it.regioneveneto.myp3.mystd.bean.mongodb.StoricoDocument;
import it.regioneveneto.myp3.mystd.repository.mongodb.StoricoRepository;
import it.regioneveneto.myp3.mystd.service.StoricoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoricoServiceImpl implements StoricoService {

    private final static Logger LOG = LoggerFactory.getLogger(StoricoServiceImpl.class);

    @Autowired
    private StoricoRepository storicoRepository;

    @Override
    public void insertStorico(String operation, String idEntita, String statoOrigine,
                              String statoDestinazione, String operationDate, String operationUser, String note) {
        try {
            StoricoDocument storicoDocument = new StoricoDocument(operation, idEntita, statoOrigine, statoDestinazione, operationDate, operationUser, note);
            storicoRepository.insert(storicoDocument);
            LOG.info("Operazione su entita {} inserita nello storico", idEntita);
        } catch (Exception e) {//Errore storico non bloccante
            LOG.error("Impossibile inserire operazione nello storico", e);
        }
    }
}
