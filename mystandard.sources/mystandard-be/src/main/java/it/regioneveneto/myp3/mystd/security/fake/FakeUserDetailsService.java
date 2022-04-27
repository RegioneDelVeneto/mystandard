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

import com.fasterxml.jackson.databind.ObjectMapper;
import it.regioneveneto.myp3.mystd.config.DummyUsers;
import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.service.ProfileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FakeUserDetailsService implements UserDetailsService {

    private final static Logger LOG = LoggerFactory.getLogger(FakeUserDetailsService.class);

    private final ProfileService profileService;

    @Value("${myprofile.application}")
    String applicationCode;
    DummyUsers dummyUsers;
    public FakeUserDetailsService(ProfileService profileService) {
        this.profileService = profileService;
        try {
            InputStream inputStreamReader = new FileInputStream(ResourceUtils.getFile("classpath:test-fake-users.json"));
            ObjectMapper objectMapper = new ObjectMapper();
            this.dummyUsers = objectMapper.readValue(inputStreamReader, DummyUsers.class);
        }
        catch (Exception ioException) {
            LOG.error("Can not read file test-fake-users!");
        }
    }

    @Override
    public UserWithAdditionalInfo loadUserByUsername(String username) throws UsernameNotFoundException {

        List<ProfileUser> profileUserList = profileService.getUserProfiles(username, applicationCode);
        List<String> roles = profileUserList.stream().map(ProfileUser::getRole).distinct().collect(Collectors.toList());
        List<String> domains = profileUserList.stream().map(ProfileUser::getDomain).distinct().collect(Collectors.toList());
        if(dummyUsers == null) {
            return new UserWithAdditionalInfo(username, "Nome", "Cognome", StringUtils.substringBefore(username, "@"),
                    "fake@f_a_k_e_mail.com", "5615156561", (long) username.hashCode(), roles, domains, null, false,
                    profileUserList, AuthorityUtils.commaSeparatedStringToAuthorityList(String.join(",", roles)));

        }
        Optional<DummyUsers.FakeUser> fakeUserOptional = dummyUsers.getUsers().stream().filter(c -> c.getUsername().equals(username)).findFirst();
        if(fakeUserOptional.isEmpty()) {
            return new UserWithAdditionalInfo(username, "Nome", "Cognome", StringUtils.substringBefore(username, "@"),
                    "fake@f_a_k_e_mail.com", "5615156561", (long) username.hashCode(), roles, domains, null, false,
                    profileUserList, AuthorityUtils.commaSeparatedStringToAuthorityList(String.join(",", roles)));
        }
        DummyUsers.FakeUser fakeUser = fakeUserOptional.get();
        return new UserWithAdditionalInfo(username, fakeUser.getNome(), fakeUser.getCognome(), StringUtils.substringBefore(username, "@"), fakeUser.getEmail(), fakeUser.getTelefono(), (long) username.hashCode(), roles, domains, null, false,
                profileUserList, AuthorityUtils.commaSeparatedStringToAuthorityList(String.join(",", roles)));
  }


}
