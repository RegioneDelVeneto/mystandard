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
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/relazioni")
@Tag(name = "tag_relazioni", description = "Estrazione lista relazioni")
public class RelazioniRestController {

    private static final Logger LOG = LoggerFactory.getLogger(RelazioniRestController.class);

    @Autowired
    private EntityService entityService;

    /**
     * Metodo GET per elencare le relazioni associabili all'entità
     * @return Lista relazioni associabili all'ente
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per elencare le relazioni associabili all'entità con codice e versione dell'entità richiedente",
            responses = {
                    @ApiResponse(description = "Lista enti by codice e versione",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Enti non trovati")
            }
    )
    @GetMapping("/{codice}/{version}/range/{entita}")
    public ResponseEntity getListaRelazioniAssociabiliEntita(
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice,
            @Parameter(description = "Versione", required = true)
            @PathVariable( name = "version") final Integer versione,
            @Parameter(description = "Entità", required = true)
            @PathVariable( name = "entita") final String entita,
            @ModelAttribute( name = "filter" ) MyStandardFilter filter) {

        LOG.info("RelazioniRestController --> Richiesta elenco relazioni per entita {} associabili all'entità con codice {} e versione {}  con filtro {}.", entita, codice, versione, filter);
        try {
            Object relazioni = entityService.findRelazioniEntitaByCodiceAndVersione(entita, codice, versione, filter);
            LOG.info("RelazioniRestController --> Elenco relazioni con successo.");
            return new ResponseEntity<>(relazioni, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("RelazioniRestController --> Errore generico nella generazione elenco relazioni con codice e versione ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Metodo GET per elencare le relazioni associabili all'ente
     * @return Lista relazioni associabili all'ente
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per elencare le relazioni associabili all'ente con codice e versione dell'entità richiedente",
            responses = {
                    @ApiResponse(description = "Lista enti by codice e versione",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Enti non trovati")
            }
    )
    @GetMapping("/range/{dominio}/{entita}")
    public ResponseEntity getListaAllRelazioniAssociabiliEntita(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "Entità", required = true)
            @PathVariable( name = "entita") final String entita,
            @ModelAttribute( name = "filter" ) MyStandardFilter filter) {

        LOG.info("RelazioniRestController --> Richiesta elenco relazioni per entita {} associabili all'entità con filtro {}", entita, filter);
        try {
            Object relazioni = entityService.findAllRelations(entita, dominio, filter);
            LOG.info("RelazioniRestController --> Elenco relazioni con successo.");
            return new ResponseEntity<>(relazioni, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("RelazioniRestController --> Errore generico nella generazione elenco relazioni con codice e versione ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}