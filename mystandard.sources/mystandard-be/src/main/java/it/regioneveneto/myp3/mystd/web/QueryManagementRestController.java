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


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.myp3.mystd.bean.MyStandardResult;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardQueryFilter;
import it.regioneveneto.myp3.mystd.bean.mongodb.QueryDocument;
import it.regioneveneto.myp3.mystd.bean.mongodb.QueryParam;
import it.regioneveneto.myp3.mystd.bean.pagination.DatatablePaginated;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.service.QueryManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/query/management")
@Tag(name = "tag_query", description = "Gestione query semantiche")
public class QueryManagementRestController {

    private static final Logger LOG = LoggerFactory.getLogger(QueryManagementRestController.class);

    @Autowired
    private QueryManagementService queryManagementService;

    /**
     * Metodo GET per elencare le query semantiche disponibili
     * @return Dati da vocabolario
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per elencare le query semantiche disponibili",
            responses = {
                    @ApiResponse(description = "Query semantiche",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati non trovati")
            }
    )
    @GetMapping
    public ResponseEntity getAllQuerySemantiche(
            @Parameter(description = "pageableFilter", required = false)
            @ModelAttribute MyStandardQueryFilter myStandardQueryFilter
            ) {

        LOG.debug("QueryManagementRestController --> Richiesta elenco query semantiche");
        try {
            DatatablePaginated querySemantiche = queryManagementService.findAllQuery(myStandardQueryFilter);
            String resultMessage = "Query semantiche estratte con successo";
            LOG.debug("QueryManagementRestController --> Query semantiche ritornate con successo. ");
            return new ResponseEntity<>(new MyStandardResult(true, resultMessage, querySemantiche), HttpStatus.OK);
        } catch (Exception e) {
            String resultMessage = "Errore generico nella restituzione delle query semantiche";
            LOG.error("QueryManagementRestController --> Errore generico nella restituzione delle query semantiche." , e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Metodo GET per ottenere una query semantica by id
     * @return Query semantica
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere una query semantica by id",
            responses = {
                    @ApiResponse(description = "Query semantica",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati non trovati")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity getQuerySemanticaById(
            @Parameter(description = "id", required = true)
            @PathVariable( name = "id") final String id
    ) {

        LOG.debug("QueryManagementRestController --> Get query semantica by id {}", id);
        try {
            Optional<QueryDocument> querySemantica = queryManagementService.findQueryById(id);
            String resultMessage = "Query semantica estratta con successo";
            LOG.debug("QueryManagementRestController --> Query semantica by id con successo.");
            return new ResponseEntity<>(new MyStandardResult(true, resultMessage, querySemantica), HttpStatus.OK);
        } catch (MyStandardException e) {
            String resultMessage = "Nessuna query semantica presente con l'id desiderato.";
            LOG.error("QueryManagementRestController --> {}", resultMessage , e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            String resultMessage = " Errore generico nella restituzione della query semantica by id.";
            LOG.error("QueryManagementRestController --> {}", resultMessage , e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo POST per inserire una nuova query semantica
     * @return Query semantica
     * @throws Exception
     */
    @Operation(
            summary = "Metodo POST per inserire una nuova query semantica",
            responses = {
                    @ApiResponse(description = "Query semantica",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati non trovati")
            }
    )
    @PostMapping
    public ResponseEntity insertQuerySemantica(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "query", required = true)
            @RequestBody @Valid QueryDocument query
    ) {

        LOG.debug("QueryManagementRestController --> Inserimento query semantica. {}", query);
        try {
            QueryDocument queryDocument = queryManagementService.insertQueryDocument(query);
            String resultMessage = "Query inserita correttamente.";
            LOG.debug("QueryManagementRestController --> {}", resultMessage);
            return new ResponseEntity<>(new MyStandardResult(true, resultMessage, queryDocument), HttpStatus.OK);
        } catch (MyStandardException e) {
            String resultMessage = "Impossibile inserire la query: l'id inviato è utilizzato per un'altra query.";
            LOG.error("QueryManagementRestController --> {}" ,resultMessage, e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            String resultMessage = "Errore generico nell'inserimento della query semantica.";
            LOG.error("QueryManagementRestController --> {}", resultMessage , e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo PUT per modificare nuova query semantica
     * @return Query semantica modificata
     * @throws Exception
     */
    @Operation(
            summary = "Metodo PUT per modificare una nuova query semantica",
            responses = {
                    @ApiResponse(description = "Query semantica",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati non trovati")
            }
    )
    @PutMapping
    public ResponseEntity updateQuerySemantica(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "query", required = true)
            @RequestBody @Valid QueryDocument query
    ) {

        LOG.debug("QueryManagementRestController --> Update query semantica. {}", query);
        try {
            QueryDocument queryDocument = queryManagementService.updateQueryDocument(query);
            String resultMessage = "Query modificata correttamente.";
            LOG.debug("QueryManagementRestController --> {}.", resultMessage);
            return new ResponseEntity<>(new MyStandardResult(true, resultMessage, queryDocument), HttpStatus.OK);
        } catch (MyStandardException e) {
            String resultMessage = "Impossibile modificare la query: nessuna query presente con l'id selezionato.";
            LOG.error("QueryManagementRestController --> {}" ,resultMessage, e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            String resultMessage = "Errore generico nella modifica della query semantica.";
            LOG.error("QueryManagementRestController --> {}", resultMessage, e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo DELETE per eliminare una query semantica
     * @throws Exception
     */
    @Operation(
            summary = "Metodo DELETE per eliminare una query semantica",
            responses = {
                    @ApiResponse(description = "Query semantica",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati non trovati")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity deleteQuerySemanticaById(
            @Parameter(description = "id", required = true)
            @PathVariable( name = "id") final String id
    ) {

        LOG.debug("QueryManagementRestController --> Delete query semantica by id {}", id);
        try {
            queryManagementService.deleteQueryById(id);
            String resultMessage = "Query semantica eliminata correttamente.";
            LOG.debug("QueryManagementRestController --> {}", resultMessage);
            return new ResponseEntity<>(new MyStandardResult(true, resultMessage), HttpStatus.OK);
        } catch (MyStandardException e) {
            String resultMessage = "Impossibile eliminare la query: nessuna query presente con l'id selezionato.";
            LOG.error("QueryManagementRestController --> {}" ,resultMessage, e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            String resultMessage = " Errore generico nella Delete della query semantica.";
            LOG.error("QueryManagementRestController --> {}", resultMessage , e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Metodo GET per ottenere i parametri di una query semantica by id
     * @return parametri Query semantica
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere i parametri di una query semantica by id",
            responses = {
                    @ApiResponse(description = "Parametri Query semantica",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati non trovati")
            }
    )
    @GetMapping("/{id}/params")
    public ResponseEntity getParametriQuerySemanticaById(
            @Parameter(description = "id", required = true)
            @PathVariable( name = "id") final String id
    ) {

        LOG.debug("QueryManagementRestController --> Get parametri query semantica by id {}", id);
        try {
            List<QueryParam> parametriQuery = queryManagementService.findQueryParamsById(id);
            String resultMessage = "Parametri query semantica estratti con successo.";
            LOG.debug("QueryManagementRestController --> {}. Estratti {} parametri", resultMessage, parametriQuery.size());
            return new ResponseEntity<>(new MyStandardResult(true, resultMessage, parametriQuery), HttpStatus.OK);
        } catch (MyStandardException e) {
            String resultMessage = "Parametri non estratti in quanto non esiste nessuna query con id selezionato.";
            LOG.error("QueryManagementRestController --> {}.", resultMessage , e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            String resultMessage = "Errore generico nella restituzione dei parametri della query semantica";
            LOG.error("QueryManagementRestController --> {}", resultMessage , e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}