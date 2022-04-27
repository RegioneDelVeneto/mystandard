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
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardMyPortalFilter;
import it.regioneveneto.myp3.mystd.bean.filter.PageableFilter;
import it.regioneveneto.myp3.mystd.service.MyPortalService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/myportal")
@Tag(name = "tag_myportal", description = "API da usare in MyPortal")
public class MyPortalRestController {

    private static final Logger LOG = LoggerFactory.getLogger(MyPortalRestController.class);

    @Autowired
    private MyPortalService myPortalService;


    /**
     * Metodo GET per ottenere il catalogo per MyPortal
     * @return List<Object> dati myportal
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere il catalogo MyPortal",
            responses = {
                    @ApiResponse(description = "Catalogo MyPortal",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati catalogo non trovati")
            }
            )

    @GetMapping("/catalogo")
    public ResponseEntity getDatiCatalogo(@Parameter(description = "pageableFilter")
                                              @ModelAttribute PageableFilter pageableFilter) {

        LOG.info("MyPortalRestController --> Richiesta per ottenere i dati catalogo con filtri {}", pageableFilter);

        try {
            Object datiCatalogo = myPortalService.getDatiCatalogo(pageableFilter);
            LOG.info("MyPortalRestController --> Dati catalogo estratti con successo");
            return new ResponseEntity<>(datiCatalogo, HttpStatus.OK);

        } catch (Exception e) {
            LOG.error("Errore generico nella restituzione dei dati catalogo.", e);
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo GET per ottenere la data di ultimo aggiornamento catalogo
     * @return Data ultimo aggiornamento catalogo
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere la data di ultimo aggiornamento catalogo",
            responses = {
                    @ApiResponse(description = "Data ultimo aggiornamento catalogo",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Data ultimo aggiornamento catalogo")
            }
    )

    @GetMapping("/catalogo/ultimoAggiornamento")
    public ResponseEntity getUltimoAggiornamentoCatalogo() {

        LOG.info("MyPortalRestController --> Richiesta per ottenere ultimo aggiornamento catalogo");

        try {
            JSONObject dataUltimoAggiornamento = myPortalService.getDataUltimoAggiornamentoCatalogo();
            LOG.info("MyPortalRestController --> Data ultimo aggiornamento catalogo estratto con successo");;
            return new ResponseEntity<>(dataUltimoAggiornamento.toString(), HttpStatus.OK);

        } catch (Exception e) {

            LOG.error("Errore generico nella restituzione della data ultimo aggiornamento catalogo.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo GET per ottenere la lista dei domini
     * @return Lista domini
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere la lista dei domini",
            responses = {
                    @ApiResponse(description = "Lista domini",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Lista domini")
            }
    )

    @GetMapping("/domini")
    public ResponseEntity getListaDomini() {

        LOG.info("MyPortalRestController --> Richiesta per ottenere la lista dei domini");

        try {
            Object domini = myPortalService.getListaDomini();
            LOG.info("MyPortalRestController --> Lista domini estratta con successo");
            return new ResponseEntity<>( domini.toString(), HttpStatus.OK);

        } catch (Exception e) {

            LOG.error("Errore generico nella restituzione della lista domini.", e);
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Metodo GET per ottenere la lista dei tipi entita
     * @return lista dei tipi entita
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere la lista dei tipi entita",
            responses = {
                    @ApiResponse(description = "lista dei tipi entita",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "lista dei tipi entita")
            }
    )
    @GetMapping("/tipiEntita")
    public ResponseEntity getListaTipiEntita() {

        LOG.info("MyPortalRestController --> Richiesta per ottenere la lista dei tipi entita");

        try {
            Object tipiEntita = myPortalService.getListaTipiEntita();
            LOG.info("MyPortalRestController --> Lista Tipi entita estratta con successo");
            return new ResponseEntity<>(tipiEntita.toString(), HttpStatus.OK);

        } catch (Exception e) {
            LOG.error("Errore generico nella restituzione della lista  Tipi entita.", e);
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo GET per ottenere la lista entità dal tipo entità con filtri
     * @return lista entità dal tipo entità con filtri
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere la lista entità dal tipo entità con filtri",
            responses = {
                    @ApiResponse(description = "lista entità dal tipo entità con filtri",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati entità non trovati")
            }
    )
    @GetMapping("/entita/tipo/{tipoEntita}")
    public ResponseEntity getEntitaByTipo(
            @Parameter(description = "tipoEntita", required = true)
            @PathVariable( name = "tipoEntita") final String tipoEntita,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "myStandardMyPortalFilter")
            @ModelAttribute MyStandardMyPortalFilter myStandardMyPortalFilter) {

        LOG.info("MyPortalRestController --> Richiesta per ottenere la lista entita by tipo {} e filtro {}", tipoEntita, myStandardMyPortalFilter);

        try {
            Object entitaByTipo = myPortalService.getListaEntitaByTipo(tipoEntita, myStandardMyPortalFilter);
            LOG.info("MyPortalRestController --> Lista entita estratta con successo");
            return new ResponseEntity<>(entitaByTipo, HttpStatus.OK);

        } catch (Exception e) {
            LOG.error("Errore generico nella restituzione della lista entita.", e);
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo GET per ottenere la lista entità dal dominio entità con filtri
     * @return lista entità dal dominio entità con filtri
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere la lista entità dal dominio entità con filtri",
            responses = {
                    @ApiResponse(description = "lista entità dal dominio entità con filtri",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati entità non trovati")
            }
    )
    @GetMapping("/entita/dominio/{dominio}")
    public ResponseEntity getEntitaByDominio(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "myStandardMyPortalFilter")
            @ModelAttribute MyStandardMyPortalFilter myStandardMyPortalFilter) {

        LOG.info("MyPortalRestController --> Richiesta per ottenere la lista entita by dominio {} e filtro {}", dominio, myStandardMyPortalFilter);

        try {
            Object entitaByDominio = myPortalService.getListaEntitaByDominio(dominio, myStandardMyPortalFilter);
            LOG.info("MyPortalRestController --> Lista entita estratta con successo");
            return new ResponseEntity<>(entitaByDominio, HttpStatus.OK);

        } catch (Exception e) {
            LOG.error("Errore generico nella restituzione della lista entita.", e);
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}