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
package it.regioneveneto.myp3.mystd.security.fake;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import it.regioneveneto.myp3.mystd.security.JwtTokenUtil;
import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.service.CacheService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    private JwtTokenUtil jwtTokenUtil;
    private CacheService cacheService;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, CacheService cacheService) {

        super(authenticationManager);
        this.jwtTokenUtil = jwtTokenUtil;
        this.cacheService = cacheService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if (authentication == null) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)  {


        // get token from a Cookie
        String jwtToken = getAccessTokenFromCookie(request);


        if (jwtToken != null) {

            try {

                // check if is still active
                if (!cacheService.isTokenInCache(jwtToken)) {
                    throw new MalformedJwtException("The token is not valid");
                }


                // parse JWT token
                Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken); //retrieve user info from token
                List<Map<String, Object>> profilesMap = (List<Map<String, Object>>) claims.get("profile");
                List<ProfileUser> profiles = profilesMap.stream().map(element -> new ProfileUser(String.valueOf(element.get("role")), String.valueOf(element.get("ipa")),
                        String.valueOf(element.get("domain")), Boolean.parseBoolean(String.valueOf(element.get("nazionale"))), (List<String>)element.get("classDomain"))).collect(Collectors.toList());
                List<String> roles = (List<String>) claims.get("roles");
                List<String> domains = (List<String>) claims.get("domains");


                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(authority -> new SimpleGrantedAuthority((String) authority))
                        .collect(Collectors.toList());

                UserWithAdditionalInfo user = new UserWithAdditionalInfo(
                        claims.getSubject(), claims.get("nome", String.class),
                        claims.get("cognome", String.class), claims.get("codiceFiscale", String.class),
                        claims.get("email", String.class), null, null, roles, domains, claims.get("ipa", String.class),
                        claims.get("ipaScelto", Boolean.class), profiles, AuthorityUtils.commaSeparatedStringToAuthorityList(String.join(",", roles)));


                // set the current user (with details) from JWT Token into Spring Security
                // configuration
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        user, null, authorities);
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } catch (ExpiredJwtException exception) {
                log.warn("Request to parse expired JWT : {} failed : {}", jwtToken, exception.getMessage());
            } catch (UnsupportedJwtException exception) {
                log.warn("Request to parse unsupported JWT : {} failed : {}", jwtToken, exception.getMessage());
            } catch (MalformedJwtException exception) {
                log.warn("Request to parse invalid JWT : {} failed : {}", jwtToken, exception.getMessage());
            } catch (SignatureException exception) {
                log.warn("Request to parse JWT with invalid signature : {} failed : {}", jwtToken, exception.getMessage());
            } catch (IllegalArgumentException exception) {
                log.warn("Request to parse empty or null JWT : {} failed : {}", jwtToken, exception.getMessage());
            } catch (Exception e) {
                logger.error("Generic error", e);
            }
        }

        return null;
    }


    private String getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookiesArr = request.getCookies();
        if (cookiesArr == null) return null;

        List<Cookie> cookies = new ArrayList<Cookie>(Arrays.asList(cookiesArr));
        Optional<Cookie> accessTokenCookieOptional = cookies.stream().filter(c -> c.getName().equals(MyStandardConstants.COOKIE_NAME_ACCESS_TOKEN)).findFirst();

        return accessTokenCookieOptional.isPresent() ? accessTokenCookieOptional.get().getValue() : null;
    }


}
