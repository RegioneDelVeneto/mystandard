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
import it.regioneveneto.myp3.mystd.service.CacheService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/saml")
@Tag(name = "tag_utility", description = "Elementi di utilità")
public class MyStandardRestController {

    private static final Logger LOG = LoggerFactory.getLogger(MyStandardRestController.class);

    @Autowired
    private CacheService cacheService;

    /**
     * Logout sull'applicativo
     * @param request
     * @throws ServletException
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = MyStandardConstants.COOKIE_NAME_ACCESS_TOKEN) String jwtToken, HttpServletRequest request) throws ServletException {
        LOG.info("MyStandardRestController --> Richiesta logout all'applicativo MyStandard.");

        try {
            cacheService.evictTokenFromCache(jwtToken);
            request.logout();

            LOG.info("MyStandardRestController --> Logout dall'applicativo MyStandard effettuato correttamente.");

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            LOG.error("MyStandardRestController --> Errore generico nell'operazione di logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}