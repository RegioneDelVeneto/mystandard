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


import it.regioneveneto.myp3.mystd.config.security.WebSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

	public final static String TOKEN_ERROR_CODE_ATTRIB = "_TOKEN_ERROR_CODE";
	public final static String TOKEN_ERROR_CODE_EXPIRED = "TOKEN_EXPIRED";
	public final static String TOKEN_ERROR_CODE_INVALID = "TOKEN_INVALID";
	public final static String TOKEN_ERROR_CODE_MISSING = "TOKEN_MISSING";

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		String error = (String) request.getAttribute(TOKEN_ERROR_CODE_ATTRIB);
		String message = error != null ? error : "Unauthorized";
		logger.warn("request: [" + request.getRequestURL() + "] - authentication error: " + message, authException);
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
	}
}
