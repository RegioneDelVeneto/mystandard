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
package it.regioneveneto.myp3.mystd.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.myp3.mystd.bean.search.SearchResponse;
import it.regioneveneto.myp3.mystd.service.EntitySearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@Tag(name = "search_api", description = "Ricerca")
public class SearchRestController {

    private static final Logger LOG = LoggerFactory.getLogger(SearchRestController.class);

    @Autowired
    private EntitySearchService entitySearchService;

    /**
     * @param query
     * @param entityType
     * @param offset
     * @param size
     * @param searchType
     * 
     * @return
     */
    @GetMapping("/")
    public ResponseEntity cercaEntita(
    		@RequestParam String query, // testo da cercare
    		@RequestParam(required = false) String entityType, // scegli il tipo, aggiungere 'tutte', se tutte allora passo null	
    		@RequestParam int offset, // paginatore della tabella
    		@RequestParam int size, // idem Morfeo
    		@RequestParam String searchType // allVersions, latestVersions, FE default a ultime versione
    	) {
        LOG.debug("SearchRestController --> Ricerca entità");
        
        try {
        	SearchResponse response = null;
        	
        	if ("allVersions".equals(searchType))
        		response = entitySearchService.searchOnAllVersions(query, entityType, offset, size);
        	else
        		response = entitySearchService.searchOnLatestVersions(query, entityType, offset, size);
        	
            LOG.debug("SearchRestController --> Ricerca completata con con successo.");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("ApiRestController --> Errore generico nella ricerca." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 
     * @return
     */
    @PostMapping("/crea")
    public ResponseEntity creaIndici() {
        LOG.debug("SearchRestController --> Creazione indici");
        
        try {
            LOG.debug("SearchRestController --> Creazione indici completata con con successo.");
            
            entitySearchService.createIndexes();
            
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("ApiRestController --> Errore generico nella creazione degli indici." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 
     * @return
     */
    @PostMapping("/reindex")
    public ResponseEntity riCreaIndici() {
        LOG.debug("SearchRestController --> Ricreazione indici");
        
        try {
            LOG.debug("SearchRestController --> Cancellazione indici.");
            entitySearchService.dropIndexes();

            LOG.debug("SearchRestController --> Creazione indici.");
            entitySearchService.createIndexes();

            LOG.debug("SearchRestController --> Indicizzazione di tutte le entità.");
            entitySearchService.indexAllEntities();
            
            LOG.debug("SearchRestController --> Ricreazione indici completata con con successo.");

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("ApiRestController --> Errore generico nella ricreazione degli indici." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
