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
import it.regioneveneto.myp3.mybox.BoxRepositoryCreationException;
import it.regioneveneto.myp3.mybox.ContentMetadata;
import it.regioneveneto.myp3.mybox.RepositoryAccessException;
import it.regioneveneto.myp3.mystd.service.AttachmentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.LinkedHashMap;


@RestController
@RequestMapping("/allegati")
@Tag(name = "tag_allegati", description = "Allegati")
public class AllegatiRestController {

    private static final Logger LOG = LoggerFactory.getLogger(AllegatiRestController.class);


    @Autowired
    private AttachmentsService attachmentsService;

    /**
     * Metodo GET per ottenere un allegato di una entità
     * 
     * @return List<Object> dati enti
     * @throws BoxRepositoryCreationException
     * @throws IOException
     * @throws RepositoryAccessException
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere un allegato di una entità",
            responses = {
                    @ApiResponse(
                        description = "Allegato di una entità",
                        content = @Content(mediaType = "text/plain",
                        schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Allegato non trovato")
            }
    )

    @GetMapping("/{idEntita}/{idAllegato}")
    public ResponseEntity getAllegato(
                @Parameter(description = "Id Entità", required = true)
                @PathVariable( name = "idEntita")
                final String idEntita,
                @Parameter(description = "Id Allegato", required = true)
                @PathVariable( name = "idAllegato")
                final String idAllegato)
                    throws Exception, IOException, BoxRepositoryCreationException {
        
        LOG.info("AllegatiRestController --> Richiesta allegato con id {} dell'entità con id {}.", idAllegato, idEntita);
        InputStream allegato =  null;
        ContentMetadata metadata = null;
        HttpHeaders headers = new HttpHeaders();
        Resource resource = null;
        try {
            allegato = attachmentsService.get(idEntita, idAllegato);
            metadata = attachmentsService.getMetadata(idEntita, idAllegato);
            resource = new InputStreamResource(allegato);
            headers.set("Access-Control-Expose-Headers", "attachmentName, attachmentType");
            headers.set("attachmentName", metadata.getFileName());
            headers.set("attachmentType", metadata.getMimeType());
        } catch (BoxRepositoryCreationException e) {
            LOG.error("AllegatiRestController --> Errore nella lettura di un file da mybox.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            LOG.error("AllegatiRestController --> Errore I/O nella lettura di un file da mybox.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOG.error("AllegatiRestController --> Errore generico nella lettura di un file in mybox.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOG.debug("AllegatiRestController --> Richiesta allegati generati con successo.");
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    
    /**
     * Metodo DELETE per eliminare un allegato di una entità
     * @param idEntita
     * @param idAllegato
     * @return
     * @throws Exception
     * @throws BoxRepositoryCreationException
     */
    @Operation(
            summary = "Metodo DELETE per eliminare un allegato di una entità",
            responses = {
                    @ApiResponse(
                        description = "Allegato di una entità",
                        content = @Content(mediaType = "text/plain",
                        schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Allegato non trovato")
            }
    )

    @DeleteMapping("/{idEntita}/{idAllegato}")
    public ResponseEntity deleteAllegato(
                @Parameter(description = "Id Entità", required = true)
                @PathVariable(name = "idEntita")
                final String idEntita,
                @Parameter(description = "Id Allegato", required = true)
                @PathVariable(name = "idAllegato")
                final String idAllegato) {
        
        LOG.info("AllegatiRestController --> Richiesta eliminazione allegato con id {} dell'entità con id {}.", idAllegato, idEntita);
        try {
            attachmentsService.delete(idEntita, idAllegato);
        } catch (Exception e) {
            LOG.error("AllegatiRestController --> Errore generico nell'eliminazione del file.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOG.info("AllegatiRestController --> Eliminazione allegato eseguita con successo.");
        return new ResponseEntity<>(idAllegato, HttpStatus.OK);
    }

    /**
     * Metodo POST per inserire un nuovo allegato
     *
     * @throws BoxRepositoryCreationException
     * @throws Exception
     */
    @Operation(summary = "TEST METHOD: Metodo POST per inserire un nuovo allegato", responses = {
                    @ApiResponse(description = "Nuovo allegato", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "allegato mal formattato") })

    @PostMapping("/")
    public ResponseEntity inserisciNuovoAllegato(
        @RequestBody final LinkedHashMap allegato
    ) {

        LOG.info("AllegatiRestController --> Richiesta inserimento nuovo allegato");

        String idEntita = "1";
        String idAllegato = null;
        Object tipoAllegato = allegato.get("attachmentType");;
        Object nomeAllegato = allegato.get("attachmentName");;

        try {
            InputStream data = createFile();
            idAllegato = attachmentsService.put(idEntita, nomeAllegato.toString(), tipoAllegato.toString(), data) ;
        } catch (Exception e) {
            LOG.debug("AllegatiRestController --> Errore generico nel salvataggio del file.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOG.info("AllegatiRestController --> Nuovo allegato inserito con successo.");
        return new ResponseEntity<>(idAllegato, HttpStatus.OK);
    }

    private InputStream createFile(){
        InputStream stream = null;
        try {
            File myFile = new File("allegato_testo.txt");
            myFile.createNewFile();
            FileWriter writer = new FileWriter(myFile);
            writer.write("Test data");
            writer.close();
            stream = new FileInputStream(myFile);
            myFile.delete();
        } catch (IOException e) {
            LOG.debug("AttachmetsServiceImpl --> Errore nella creazione del file di test." , e);
            e.printStackTrace();
        }
        return stream;
    }

}