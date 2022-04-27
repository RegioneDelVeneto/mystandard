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
import it.regioneveneto.myp3.mystd.service.EntityService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/vocabulary")
@Tag(name = "tag_vocabolario", description = "Estrazione dati da vocabolario")
public class VocabularyRestController {

    private static final Logger LOG = LoggerFactory.getLogger(VocabularyRestController.class);

    @Autowired
    private EntityService entityService;

    /**
     * Metodo GET per elencare dati presi da un vocabolario
     * @return Dati da vocabolario
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per elencare dati presi da un vocabolario con codice e versione dell'entità richiedente",
            responses = {
                    @ApiResponse(description = "Dati da vocabolario",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati non trovati")
            }
    )
    @GetMapping("/{type}")
    public ResponseEntity getDatiDaVocabolario(
            @Parameter(description = "type", required = true)
            @PathVariable( name = "type") final String type) {

        LOG.info("VocabularyRestController --> Richiesta dati da vocabolario per {}", type);
        try {
            JSONObject datiVocabolario = entityService.findDatiVocabolario(type);
            LOG.info("VocabularyRestController --> Dati da vocabolario tornati con successo.");
            return new ResponseEntity<>(datiVocabolario.toString(), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("VocabularyRestController --> Errore generico nella generazione dati da vocabolario ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}