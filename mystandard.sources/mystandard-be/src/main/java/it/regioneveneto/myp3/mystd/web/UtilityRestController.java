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
import it.regioneveneto.myp3.mystd.bean.generic.MyStandardQueryRequest;
import it.regioneveneto.myp3.mystd.service.UtilityService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/utility")
@Tag(name = "tag_utility", description = "Elementi di utilità")
public class UtilityRestController {

    private static final Logger LOG = LoggerFactory.getLogger(UtilityRestController.class);

    @Autowired
    private UtilityService utilityService;

    @GetMapping("/export/rdf")
    public ResponseEntity<byte[]> rdfExport() {

        LOG.info("UtilityRestController --> Richiesta export rdf file");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-disposition", "attachment; filename=\"rdf_export.owl\"");
            headers.set("Content-Type", "application/rdf+xml");
            byte[] file = utilityService.getExportRdfByteArray();
            LOG.info("UtilityRestController --> Rdf file estratto correttamente");
            return ResponseEntity.ok().headers(headers).body(file);
        } catch (Exception e) {
            LOG.error("UtilityRestController --> Errore nell'export del file rdf." , e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @PostMapping("/query/execute")
    public ResponseEntity executeQuery(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "myStandardQueryRequest", required = true)
            @RequestBody MyStandardQueryRequest myStandardQueryRequest) {

        LOG.info("UtilityRestController --> Richiesta di esecuzione query {} ", myStandardQueryRequest);

        try {
            JSONObject result = utilityService.executeQuery(myStandardQueryRequest);
            LOG.info("UtilityRestController --> Query eseguita correttamente");

            return ResponseEntity.ok().body(result.toString());
        } catch (Exception e) {
            LOG.error("UtilityRestController --> Errore nell'esecuzione della query." , e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }



}