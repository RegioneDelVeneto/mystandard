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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.service.EntityService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/menu")
@Tag(name = "tag_menu", description = "Menu applicativo")
public class MenuRestController {

    private static final Logger LOG = LoggerFactory.getLogger(MenuRestController.class);

    @Autowired
    private EntityService entityService;


    /**
     * Metodo GET per ottenere il menu
     * @return List<Object> dati menu
     * @throws Exception
     */
    @Operation(
            summary = "Metodo GET per ottenere il menu",
            responses = {
                    @ApiResponse(description = "Menu",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class))),
                    @ApiResponse(responseCode = "400", description = "Dati menu non trovati")
            }
            )

    @GetMapping("/")
    public ResponseEntity getMenu() {

        LOG.info("MenuRestController --> Richiesta per ottenere il menu");

        try {
            JSONObject menu = entityService.getMenuInfo();
            LOG.info("MenuRestController --> Menu generato con successo.");
            return new ResponseEntity<>(menu.toString(), HttpStatus.OK);
        } catch (MyStandardException e) {
            LOG.error("Errore nell'estrazione del menu.", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("Errore generico nell'estrazione del menu.", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}