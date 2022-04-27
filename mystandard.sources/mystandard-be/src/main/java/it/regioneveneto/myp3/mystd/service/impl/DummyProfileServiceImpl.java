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

import com.fasterxml.jackson.databind.ObjectMapper;
import it.regioneveneto.myp3.mystd.config.DummyUsers;
import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DummyProfileServiceImpl implements ProfileService {

    private final static Logger LOG = LoggerFactory.getLogger(DummyProfileServiceImpl.class);

    private DummyUsers fakeUsers;

    public DummyProfileServiceImpl() {
        try {
            InputStream inputStreamReader = new FileInputStream(ResourceUtils.getFile("classpath:test-fake-users.json"));
            ObjectMapper objectMapper = new ObjectMapper();
            this.fakeUsers = objectMapper.readValue(inputStreamReader, DummyUsers.class);
        }
        catch (Exception ioException) {
            LOG.error("Can not read file test-fake-users!");
        }
    }

    @Override
    public List<ProfileUser> getUserProfiles(String username, String applicationCode) {
        if(fakeUsers == null) {
            return List.of(new ProfileUser());
        }
        List<DummyUsers.FakeUser> fakeUserList = fakeUsers.getUsers();
        Optional<DummyUsers.FakeUser> optionalFakeUser = fakeUserList.stream().filter(x -> x.getUsername().equals(username)).findAny();
        if(optionalFakeUser.isEmpty()) {
            return List.of(new ProfileUser());
        }
        DummyUsers.FakeUser fakeUser = optionalFakeUser.get();
        return fakeUser.getProfiles().stream().map(x -> new ProfileUser(x.getRole(), x.getIpa(), x.getDomain(), x.getNazionale(), x.getClassDomain())).collect(Collectors.toList());
    }

}
