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
package it.regioneveneto.myp3.mystd.security.saml;

import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.service.ProfileService;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

    static final Logger log = LoggerFactory.getLogger(SAMLUserDetailsServiceImpl.class);

    private static final String EMAIL_ADDRESS_PERSONALE = "emailAddressPersonale";
    private static final String CODICE_FISCALE_ATTRIBUTE = "codiceFiscale";
    private static final String NOME_ATTRIBUTE = "nome";
    private static final String COGNOME_ATTRIBUTE = "cognome";
    private static final String TELEFONO_ATTRIBUTE = "cellulare";
    private static final String USER_ID_FROM_SPID = "spidCode";
    private static final String USERNAME_ATTRIBUTE = "userAlias";

    @Autowired
    private ProfileService profileService;

    @Value("${myprofile.application}")
    String applicationCode;

    private String getAttributeValue(SAMLCredential credential, String attributeName){
        if( credential.getAttribute(attributeName) != null && credential.getAttribute(attributeName).getAttributeValues() != null
            && credential.getAttribute(attributeName).getAttributeValues().size() > 0) {
            String value = ((XSStringImpl) credential.getAttribute(attributeName).getAttributeValues().get(0)).getValue();
            log.debug("attributeName {}", value);
            return value;
        } else {
            log.info("Parametro ["+attributeName+"] non presente in MyID");
            return null;
        }
    }

    @Override
    public Object loadUserBySAML(final SAMLCredential credential) throws UsernameNotFoundException {

        log.debug(XMLHelper.nodeToString(credential.getAuthenticationAssertion().getParent().getDOM()));

        String userAlias = getAttributeValue(credential, USER_ID_FROM_SPID);
        if(userAlias==null)
            userAlias = getAttributeValue(credential, USERNAME_ATTRIBUTE);
        if(userAlias==null)
            userAlias="unknown";

        List<ProfileUser> profiles = profileService.getUserProfiles(userAlias, applicationCode);
        List<String> roles = profiles.stream().map(profile -> profile.getRole()).distinct().collect(Collectors.toList());
        List<String> domains = profiles.stream().map(profile -> profile.getDomain()).distinct().collect(Collectors.toList());

        UserWithAdditionalInfo userDetails = new UserWithAdditionalInfo(
        		userAlias,
        		getAttributeValue(credential, NOME_ATTRIBUTE),
        		getAttributeValue(credential, COGNOME_ATTRIBUTE),
        		getAttributeValue(credential, CODICE_FISCALE_ATTRIBUTE),
        		getAttributeValue(credential, EMAIL_ADDRESS_PERSONALE),
        		getAttributeValue(credential, TELEFONO_ATTRIBUTE),
                null,
                roles,
                domains,
                null,
                false,
                profiles,
                AuthorityUtils.commaSeparatedStringToAuthorityList(String.join(",", roles))
        		);

        Authentication principal = new Authentication(){
            @Override
            public String getName() {
                return userDetails.getUsername();
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return userDetails.getAuthorities();
            }

            @Override
            public Object getCredentials() {
                return credential;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return userDetails;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                throw new UnsupportedOperationException();
            }
        };

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(principal);

        return principal;
    }


}
