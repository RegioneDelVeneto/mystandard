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
package it.regioneveneto.myp3.mystd.security;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@Tag(name = "tag_auth", description = "Dati Autenticazione")
public class JwtAuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationController.class);

	@Autowired
	private SecurityService securityService;

	/**
	 * Metodo GET per ottenere le informazioni dell'utente autenticato
	 * @return List<Object> dati enti
	 * @throws Exception
	 */
	@Operation(
			summary = "Metodo GET per ottenere le informazioni dell'utente autenticato",
			responses = {
					@ApiResponse(description = "Dettagli utente autenticato",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = Object.class))),
					@ApiResponse(responseCode = "400", description = "Dati utente")
			}
	)
	@GetMapping("/userinfo")
	public ResponseEntity<?> userInfo(Authentication authentication) {

		logger.debug("Get user info");
		Map<String, Object> responseMap = new HashMap<>();
		if (authentication != null) {
			logger.debug("Presente un utente autenticato");
			UserWithAdditionalInfo user = ((UserWithAdditionalInfo) authentication.getPrincipal());
			responseMap.put("username", user.getUsername());
			responseMap.put("givenName", user.getNome());
			responseMap.put("familyName", user.getCognome());
			responseMap.put("codFiscale", user.getCodiceFiscale());
			responseMap.put("email", user.getEmail());
			responseMap.put("profile", user.getUserProfiles());
			responseMap.put("roles", user.getRoles());
			responseMap.put("ipa", user.getIpa());
			responseMap.put("domains", user.getDomains());
			responseMap.put("ipaScelto", user.getIpaScelto());
		} else {
			logger.debug("Nessun utente autenticato");
		}

		return ResponseEntity.status(HttpStatus.OK).body(responseMap);
	}


	/**
	 * Metodo POST per definire la scelta di un IPA
	 * @return Ok in caso di IPA scelto correttamente
	 * @throws Exception
	 */
	@Operation(
			summary = "Metodo POST per definire la scelta di un IPA",
			responses = {
					@ApiResponse(description = "Ok se IPA scelto correttamente",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = Object.class))),
					@ApiResponse(responseCode = "400", description = "Dati utente")
			}
	)
	@PostMapping("/postIPA")
	public ResponseEntity postIPA(@RequestBody String ipa, Authentication authentication, HttpServletResponse response) {

		logger.debug("Set ipa for user");
		try {
			securityService.setIpaforUser(ipa, authentication, response);
			return new ResponseEntity<>( HttpStatus.OK);
		} catch (MyStandardException e) {
			logger.error("JwtAuthenticationController --> Errore nella scelta dell'IPA.", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("JwtAuthenticationController --> Errore generico nella scelta dell'IPA.", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

}
