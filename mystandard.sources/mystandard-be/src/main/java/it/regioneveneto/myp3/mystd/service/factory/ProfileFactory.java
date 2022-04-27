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
package it.regioneveneto.myp3.mystd.service.factory;

import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.service.ProfileService;
import it.regioneveneto.myp3.mystd.service.impl.DummyProfileServiceImpl;
import it.regioneveneto.myp3.mystd.service.impl.MyProfileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileFactory implements ProfileService {

    @Value("${myprofile.fake}")
    Boolean fakeProfile;

    @Value("${myprofile.baseUrl}")
    String myProfileBaseUrl;

    @Autowired
    private MyStandardProperties myStandardProperties;

    private ProfileService getProfileService() {
        if (fakeProfile) {
            return new DummyProfileServiceImpl();
        } else {
            return new MyProfileServiceImpl(myProfileBaseUrl, myStandardProperties.getEnteNazionale());
        }
    }

    @Override
    public List<ProfileUser> getUserProfiles(String username, String applicationCode) {
        return getProfileService().getUserProfiles(username, applicationCode);
    }
}
