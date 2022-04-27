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
import it.regioneveneto.myp3.mystd.bean.MyStandardRequest;
import it.regioneveneto.myp3.mystd.bean.MyStandardResult;
import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardEntityOperationEnum;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntity;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.service.EntitySearchService;
import it.regioneveneto.myp3.mystd.service.EntityService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/entities")
@Tag(name = "tag_entities", description = "Dati Entità")
public class EntitiesRestController {

    private static final Logger LOG = LoggerFactory.getLogger(EntitiesRestController.class);

    @Autowired
    private EntityService entityService;

    @Autowired
    private EntitySearchService entitySearchService;

    @Autowired
    private MessageSource messageSource;


    /**
     * Metodo GET per elencare le entità
     * @return List<Object> dati enti
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per elencare lista entità",
            responses = {
                    @ApiResponse(description = "Lista entità",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovati")
            }
            )

    @GetMapping("/{dominio}/{entita}")
    public ResponseEntity getListaEntitaWithDominio(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @ModelAttribute( name = "filter" ) MyStandardFilter filter,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta elenco entità {} per dominio {} con filtro {}", entita, dominio, filter);

        try {
            Object entitaDatatablePaginated = entityService.findAll(dominio, entita, filter, user);
            LOG.info("EntitiesRestController --> Elenco entità generato con con successo.");
            return new ResponseEntity<>(entitaDatatablePaginated, HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione elenco entità." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            LOG.error("EntitiesRestController --> Errore nella lettura OWL/RDF per generazione elenco entità." , e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione elenco entità ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Metodo GET per elencare le entità senza dominio
     * @return List<Object> dati enti
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per elencare lista entità senza dominio",
            responses = {
                    @ApiResponse(description = "Lista entità",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovati")
            }
    )

    @GetMapping("/{entita}")
    public ResponseEntity getListaEntita(
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @ModelAttribute( name = "filter" ) MyStandardFilter filter,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta elenco entità senza dominio {} con filtro {}", entita, filter);

        try {
            Object entitaDatatablePaginated = entityService.findAll(null, entita, filter, user);
            LOG.info("EntitiesRestController --> Elenco entità senza dominio generato con con successo.");
            return new ResponseEntity<>(entitaDatatablePaginated, HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione elenco entità." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            LOG.error("EntitiesRestController --> Errore nella lettura OWL/RDF per generazione elenco entità." , e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione elenco entità ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    /**
     * Metodo GET per ottenere dati da mostrare per nuova entità
     * @return Object nuova entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere dati da mostrare per nuova entità",
            responses = {
                    @ApiResponse(description = "Struttura nuova entità",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class)))
            }
    )

    @GetMapping("/{dominio}/{entita}/nuovo")
    public ResponseEntity getNuovaEntita(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta struttura per entita {} del dominio {}", entita, dominio);

        try {
            Object ente = entityService.getNewEntity(dominio, entita, user);
            LOG.info("EntitiesRestController --> Dati nuova entità generati con con successo.");
            return new ResponseEntity<>(ente, HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione dati nuova entità." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            LOG.error("EntitiesRestController --> Errore nella lettura OWL/RDF per generazione dati nuova entità." , e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione dati per nuova entità." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo GET per dettaglio entità by codice e versione
     * @return Dettaglio entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per dettaglio entità by codice e versione",
            responses = {
                    @ApiResponse(description = "Dettaglio entità by codice e versione",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovata")
            }
    )
    @GetMapping("/{dominio}/{entita}/{codice}/{versione}")
    public ResponseEntity getDettaglioByCodiceVersione(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice,
            @Parameter(description = "Versione", required = true)
            @PathVariable( name = "versione") final Integer versione,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta dettaglio entità {} del dominio {} con codice {} e versione {}.", entita, dominio, codice, versione);
        try {
            Object enti = entityService.findByCodiceAndVersione(dominio, entita, codice, versione, false, user, false);
            LOG.info("EntitiesRestController --> dettaglio entità generato con successo.");
            return new ResponseEntity<>(enti, HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione dettaglio entità con codice e versione." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            LOG.error("EntitiesRestController --> Errore nella lettura OWL/RDF per generazione dettaglio entità con codice e versione." , e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione dettaglio entità con codice e versione ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Metodo GET per dettaglio entità by codice e versione specifico per la clonazione per nuove versioni (si saltano dati in tab non da riportare)
     * @return Dettaglio entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per dettaglio entità by codice e versione specifico per la clonazione per nuove versioni (si saltano dati in tab non da riportare)",
            responses = {
                    @ApiResponse(description = "Dettaglio entità by codice e versione",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovata")
            }
    )
    @GetMapping("/{dominio}/{entita}/{codice}/{versione}/clone")
    public ResponseEntity getDettaglioByCodiceVersioneClone(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice,
            @Parameter(description = "Versione", required = true)
            @PathVariable( name = "versione") final Integer versione,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta dettaglio entità {} del dominio {} con codice {} e versione {} saltando i dati nei tab speciali.", entita, dominio, codice, versione);
        try {
            Object enti = entityService.findByCodiceAndVersione(dominio, entita, codice, versione, false, user, true);
            LOG.info("EntitiesRestController --> dettaglio entità generato con successo.");
            return new ResponseEntity<>(enti, HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione dettaglio entità con codice e versione." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            LOG.error("EntitiesRestController --> Errore nella lettura OWL/RDF per generazione dettaglio entità con codice e versione." , e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione dettaglio entità con codice e versione ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Metodo GET per dettaglio entità by codice e versione
     * @return Dettaglio entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per dettaglio entità by codice e versione",
            responses = {
                    @ApiResponse(description = "Dettaglio entità by codice e versione",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovata")
            }
    )
    @GetMapping("/{dominio}/{entita}/{codice}/{versione}/readonly")
    public ResponseEntity getDettaglioEntitaByCodiceVersioneReadonly(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice,
            @Parameter(description = "Versione", required = true)
            @PathVariable( name = "versione") final Integer versione,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta dettaglio entità {} del dominio {} con codice {} e versione {} in modalita readonly.", entita, dominio, codice, versione);
        try {
            Object enti = entityService.findByCodiceAndVersione(dominio, entita, codice, versione, true, user, false);
            LOG.info("EntitiesRestController --> dettaglio entità generato con successo.");
            return new ResponseEntity<>(enti, HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione dettaglio entità con codice e versione." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            LOG.error("EntitiesRestController --> Errore nella lettura OWL/RDF per generazione dettaglio entità con codice e versione." , e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione dettaglio entità con codice e versione ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Metodo GET per ottenere la lista di operazioni possibili per il dettaglio
     * @return lista operazioni possibili per entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere la lista di operazioni possibili per entità",
            responses = {
                    @ApiResponse(description = "Lista operazioni per entità by codice e versione",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovata")
            }
    )
    @GetMapping("/{dominio}/{entita}/{codice}/{versione}/operazioni")
    public ResponseEntity getListaOperazioniByCodiceVersione(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice,
            @Parameter(description = "Versione", required = true)
            @PathVariable( name = "versione") final Integer versione,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta lista operazioni su entità {} del dominio {} con codice {} e versione {}.", entita, dominio, codice, versione);
        try {
            Object listaOperazioniEntita = entityService.findAllOperations(entita, codice, versione, user);
            LOG.info("EntitiesRestController --> Lista operazioni entità generato con successo.");
            return new ResponseEntity<>(listaOperazioniEntita.toString(), HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione Lista operazioni entità con codice e versione." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            LOG.error("EntitiesRestController --> Errore nella lettura OWL/RDF per generazione Lista operazioni entità con codice e versione." , e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione Lista operazioni entità con codice e versione ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo GET per ottenere la lista di storico di un dettaglio
     * @return lista storico per entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere la lista di storico di un dettaglio",
            responses = {
                    @ApiResponse(description = "Lista storico per entità by codice e versione",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovata")
            }
    )
    @GetMapping("/{dominio}/{entita}/{codice}/{versione}/storico")
    public ResponseEntity getListaStoricoByCodiceVersione(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice,
            @Parameter(description = "Versione", required = true)
            @PathVariable( name = "versione") final Integer versione) {

        LOG.info("EntitiesRestController --> Richiesta lista storico su entità {} del dominio {} con codice {} e versione {}.", entita, dominio, codice, versione);
        try {
            Object listaStoricoEntita = entityService.findAllStorico(entita, codice, versione);
            LOG.info("EntitiesRestController --> Lista storico entità generato con successo.");
            return new ResponseEntity<>(listaStoricoEntita.toString(), HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione Lista storico entità con codice e versione." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione Lista storico entità con codice e versione ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Metodo DELETE per eliminazione entità
     * @return ok se entità eliminata con successo
     * @throws Exception
     */
    @Operation(
            summary = "Metodo DELETE per eliminazione entità",
            responses = {
                    @ApiResponse(description = "Ok se eliminazione con successo",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovata")
            }
    )
    @DeleteMapping("/{dominio}/{entita}/{codice}/{versione}/delete")
    public ResponseEntity deleteEntita(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice,
            @Parameter(description = "Versione", required = true)
            @PathVariable( name = "versione") final Integer versione,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta eleminazione entità {} del dominio {} con codice {} e versione {}.", entita, dominio, codice, versione);
        try {

            entityService.deleteEntity(dominio, entita, codice, versione, user, null);
            LOG.info("EntitiesRestController --> Entità eliminato con successo.");
            return new ResponseEntity<>(HttpStatus.OK);


        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nell'eliminazione di un Entità'.", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nell'eliminazione di un Entità.", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Metodo POST per inserire una nuova entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo POST per inserire una nuova entità",
            responses = {
                    @ApiResponse(description = "Nuova entità",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità mal formattata")
            }
    )

    @PostMapping(value = "/{dominio}/{entita}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity inserisciNuovaEntita(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Entità", required = true)
            @ModelAttribute("entity") @Valid final MyStandardEntity entityObject, BindingResult bindingResult,
            @RequestParam(required = false) List<MultipartFile> allegati, @AuthenticationPrincipal UserWithAdditionalInfo user) throws MyStandardException {

        LOG.info("EntitiesRestController --> Inserimento nuova entityObject {} per dominio {}.", entityObject, dominio);
        try {
            entityService.insertEntityByType(dominio, entita, entityObject, allegati, user);
            LOG.info("EntitiesRestController --> Entità inserita con successo.");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nell'inserimento della nuova entità.", e);

            List<Map<String, String>> errors = e.getErrors();
            if (errors == null) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(new MyStandardResult(false, e.getErrors()), HttpStatus.PRECONDITION_FAILED);
            }

        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nell'inserimento della nuova entità.", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Metodo PUT per aggiornare un'entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo PUT per aggiornare un'entità",
            responses = {
                    @ApiResponse(description = "Aggiornamento entità",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità mal formattata")
            }
    )

    @PutMapping(value = "/{dominio}/{entita}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity aggiornaEntita(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Entita", required = true)
            @ModelAttribute("entity") @Valid final MyStandardEntity entityObject, BindingResult bindingResult,
            @RequestParam(required = false) List<MultipartFile> allegati, @AuthenticationPrincipal UserWithAdditionalInfo user) throws MyStandardException {

        LOG.info("EntitiesRestController --> Aggiornamento entityObject {} dominio {}", entityObject, dominio);
        try {
            Object enteDetailReadonly = entityService.updateEntityByType(dominio, entita, entityObject, allegati, user);
            LOG.info("EntitiesRestController --> Entità aggiornato con successo.");
            return new ResponseEntity<>(enteDetailReadonly, HttpStatus.OK);

        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nell'aggiornamento dell'entità.", e);
            List<Map<String, String>> errors = e.getErrors();
            if (errors == null) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(new MyStandardResult(false, e.getErrors()), HttpStatus.PRECONDITION_FAILED);
            }
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nell'aggiornameno dell'entità.", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Metodo GET per ottenere il numero di versione da usare per creare o ripristinare una versione
     * @return numero versione
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere il numero di versione da usare per creare o ripristinare una versione",
            responses = {
                    @ApiResponse(description = "Numero versione da utilizzare",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovata")
            }
    )
    @GetMapping("/{dominio}/{entita}/{codice}/max")
    public ResponseEntity getMaxVersioneByCodice(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice) {

        LOG.info("EntitiesRestController --> Richiesta max versione  entità {} del dominio {} con codice {} ", entita, dominio, codice);
        try {
            JSONObject maxVersioneStatoObject = entityService.findMaxVersioneByCodice(entita, codice);
            LOG.info("EntitiesRestController --> Max versione generato con ccesso.");
            return new ResponseEntity<>(maxVersioneStatoObject.toString(), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione max versione by codice ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo POST per il cambio di stato di un'entità by codice e versione
     * @return Pubblicazione entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo POST per il cambio di stato di un'entità by codice e versione",
            responses = {
                    @ApiResponse(description = "Risultato cambio di stato",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovata")
            }
    )
    @PostMapping("/{dominio}/{entita}/{codice}/{versione}/{operazione}")
    public ResponseEntity cambiaStatoEntita(
            @Parameter(description = "dominio", required = true)
            @PathVariable( name = "dominio") final String dominio,
            @Parameter(description = "entita", required = true)
            @PathVariable( name = "entita") final String entita,
            @Parameter(description = "Codice", required = true)
            @PathVariable( name = "codice") final String codice,
            @Parameter(description = "Versione", required = true)
            @PathVariable( name = "versione") final Integer versione,
            @Parameter(description = "Operazione", required = true)
            @PathVariable( name = "operazione") final String operazione,
            @RequestBody final MyStandardRequest myStandardRequest,
            @AuthenticationPrincipal UserWithAdditionalInfo user){

        LOG.info("EntitiesRestController --> Operazione di {} su entità {} del dominio {} con codice {} e versione {}.", operazione, entita, dominio, codice, versione);
        try {
            Object entitaStatoUpdated = entityService.genericUpdateEntityState(operazione, dominio, entita, codice, versione, user, myStandardRequest);
            String resultMessage = MyStandardEntityOperationEnum.of(operazione).getDescription() + " eseguita con successo";
            LOG.info("EntitiesRestController --> Operazione di {} eseguita con successo.", operazione);
            return new ResponseEntity<>(new MyStandardResult(true, resultMessage, entitaStatoUpdated), HttpStatus.OK);

        } catch (MyStandardException e) {
            String resultMessage;
            try {
                resultMessage = "Errore nell'operazione di " + MyStandardEntityOperationEnum.of(operazione).getDescription()  + " sull'entità.";
            } catch (MyStandardException ex) {
                resultMessage = "Errore nell'operazione sull'entità.";
            }
            LOG.error("EntitiesRestController --> Errore nell'operazione di {} sull'entità.", operazione, e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            String resultMessage;
            try {
                resultMessage = "Errore genericonell'operazione di " + MyStandardEntityOperationEnum.of(operazione).getDescription()  + " sull'entità.";
            } catch (MyStandardException ex) {
                resultMessage = "Errore generico nell'operazione sull'entità.";
            }
            LOG.error("EntitiesRestController --> Errore generico nell'operazione di {} sull'entità", operazione, e);
            return new ResponseEntity<>(new MyStandardResult(false, resultMessage), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Metodo GET per elencare le entità da mostrare in bacheca
     * @return List<Object> entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per elencare lista entità da mostrare in bacheca in relazione al ruolo utente",
            responses = {
                    @ApiResponse(description = "Lista entità da mostrare in bacheca",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovati")
            }
    )

    @GetMapping("/bacheca")
    public ResponseEntity getListaEntitaBacheca(
            @ModelAttribute( name = "filter" ) MyStandardFilter filter,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta elenco entità da mostrare in bacheca con filtro {}", filter);

        try {
            Object entitaBachecaPaginated = entityService.findAllBacheca(filter, user, null);
            LOG.info("EntitiesRestController --> Elenco entità per bacheca generato con con successo.");
            return new ResponseEntity<>(entitaBachecaPaginated, HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione elenco entità per bacheca." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione elenco entità per bacheca ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    /**
     * Metodo GET per elencare le entità da mostrare in bacheca per operazione
     * @return List<Object> entità
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per elencare lista entità da mostrare in bacheca in relazione al ruolo utente e per operazione in input",
            responses = {
                    @ApiResponse(description = "Lista entità da mostrare in bacheca",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Entità non trovati")
            }
    )

    @GetMapping("/bacheca/{operazione}")
    public ResponseEntity getListaEntitaBachecaByOperazione(
            @Parameter(description = "Operazione", required = true)
            @PathVariable( name = "operazione") final String operazione,
            @ModelAttribute( name = "filter" ) MyStandardFilter filter,
            @AuthenticationPrincipal UserWithAdditionalInfo user) {

        LOG.info("EntitiesRestController --> Richiesta elenco entità da mostrare in bacheca con filtro {} per operazione {}", filter, operazione);

        try {
            Object entitaBachecaPaginated = entityService.findAllBacheca(filter, user, operazione);
            LOG.info("EntitiesRestController --> Elenco entità per bacheca divise per operazione generato con con successo.");
            return new ResponseEntity<>(entitaBachecaPaginated, HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("EntitiesRestController --> Errore nella generazione elenco entità divise per operazione per bacheca." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("EntitiesRestController --> Errore generico nella generazione elenco entità divise per operazione per bacheca ." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}