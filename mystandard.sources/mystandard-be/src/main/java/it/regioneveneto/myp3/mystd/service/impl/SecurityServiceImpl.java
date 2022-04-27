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
package it.regioneveneto.myp3.mystd.service.impl;

import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.security.JwtTokenUtil;
import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.service.CacheService;
import it.regioneveneto.myp3.mystd.service.SecurityService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);


    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CacheService cacheService;

    @Value("${jwt.validity.seconds:36000}") //default: 10 hours
    private long jwtTokenValidity;

    @Override
    public void setIpaforUser(String ipa, Authentication authentication, HttpServletResponse response) throws MyStandardException {

        if (!StringUtils.hasText(ipa)) {
            throw new MyStandardException("IPA non valorizzato");
        } else {
            if (authentication != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserWithAdditionalInfo) {
                    UserWithAdditionalInfo user = ((UserWithAdditionalInfo) principal);
                    if (user.getIpaScelto()) {
                        throw new MyStandardException("L'utente ha già effettuato la scelta dell'IPA");
                    } else {
                        List<ProfileUser> userProfiles = user.getUserProfiles();
                        if (userProfiles == null || userProfiles.size() == 0) {
                            throw new MyStandardException("Profilo vuoto per l'utente autenticato.");
                        } else {
                            ProfileUser profileUser = userProfiles.stream().filter(profile -> ipa.equals(profile.getIpa())).findFirst().orElse(null);
                            if (profileUser == null) {
                                throw new MyStandardException("Nessun profilo utente per l'ipa " + ipa);
                            } else {

                                try {

                                    //Set info for authenticated user
                                    user.setIpa(profileUser.getIpa());
                                    user.setRoles(Collections.singletonList(profileUser.getRole()));
                                    user.setDomains(Collections.singletonList(profileUser.getDomain()));
                                    user.setIpaScelto(true);


                                    final String token = jwtTokenUtil.generateToken(user.getUsername(), user.getClaims());

                                    // store token as valid
                                    cacheService.storeTokenInCache(token);

                                    // set cookie
                                    Cookie cookie = new Cookie(MyStandardConstants.COOKIE_NAME_ACCESS_TOKEN, token);
                                    cookie.setHttpOnly(false);
                                    cookie.setMaxAge((int) jwtTokenValidity);
                                    cookie.setSecure(false);
                                    cookie.setPath("/");


                                    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                                            .map(authority -> new SimpleGrantedAuthority(authority))
                                            .collect(Collectors.toList());


                                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                            user, null, authorities);
                                    SecurityContextHolder.clearContext();
                                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                                    response.addCookie(cookie);
                                } catch (Exception e) {
                                    logger.error("MyStandard - Errore nella memorizzazione del token in Redis", e);
                                    throw e;
                                }
                            }
                        }
                    }


                } else {
                    throw new MyStandardException("Impossibile ottenere le informazioni dell'utente autenticato.");
                }
            } else {
                throw new MyStandardException("Nessun utente autenticato.");
            }
        }
    }
}

