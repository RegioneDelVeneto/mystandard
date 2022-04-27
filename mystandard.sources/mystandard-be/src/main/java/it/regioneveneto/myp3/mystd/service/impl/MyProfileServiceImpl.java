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

import it.regioneveneto.myp3.myprofile.client.model.myprofile.*;
import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardRoleEnum;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.security.ProfileUser;
import it.regioneveneto.myp3.mystd.service.ProfileService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyProfileServiceImpl implements ProfileService {

    private final static Logger LOG = LoggerFactory.getLogger(MyProfileServiceImpl.class);

    private it.regioneveneto.myp3.myprofile.client.ProfileService myprofileClient;

    private String enteNazionale;
    @Autowired
    private MyStandardProperties myStandardProperties;

    public MyProfileServiceImpl(
            @Value("${myprofile.baseUrl}") String myProfileBaseUrl,
            @Value("${mystandard.enteNazionale}") String enteNazionale
    ) {
        super();
        this.myprofileClient = new it.regioneveneto.myp3.myprofile.client.ProfileService(myProfileBaseUrl);
        this.enteNazionale = enteNazionale;
    }

    @Override
    public List<ProfileUser> getUserProfiles(String username, String applicationCode) throws UsernameNotFoundException {
        LOG.debug(String.format("*** Calling getIpaAndRoles {} {} ", username, applicationCode));

        List<ProfileUser> profileUserList = new ArrayList<>();
        List<MyProfileTenantUser> tenantUserList = null;

        try {
            tenantUserList = myprofileClient.getUserProfiles(username);
        } catch (Exception e) {
            LOG.debug(String.format("User profile not found for code \"%s\"", username), e);
        }

        if (tenantUserList == null) {
            return null;
        }

        for (MyProfileTenantUser tenantUser: tenantUserList) {
            // search in organs
            String currentIpa = tenantUser.getTenant().getTenantCode();

            //GROUPLOGIC
           Set<MyProfileGroup> groups = tenantUser.getGroups();
            for (MyProfileGroup myProfileGroup : groups) {

                Set<MyProfileGroupRole> roles = myProfileGroup.getRoles();
                for (MyProfileGroupRole myProfileGroupRole : roles) {
                    MyProfileRole myProfileRole = myProfileGroupRole.getRole();
                    if (myProfileRole != null) {
                        MyProfileApplication application = myProfileRole.getApplication();
                        if (application != null && application.getApplCode().equals(applicationCode)) {
                            ProfileUser profileUser = new ProfileUser();
                            profileUser.setIpa(currentIpa);

                            if (currentIpa.equals(enteNazionale)) {
                                profileUser.setNazionale(true);
                            } else {
                                profileUser.setNazionale(false);
                            }

                            String role = myProfileRole.getRoleName();
                            if (role.equalsIgnoreCase(MyStandardRoleEnum.OPERATORE.getMyProfileRole())) {
                                if (profileUser.getNazionale()) {
                                    profileUser.setRole(MyStandardRoleEnum.OPERATORE_ENTE_NAZIONALE.getRole());
                                } else {
                                    profileUser.setRole(MyStandardRoleEnum.OPERATORE_ENTE_LOCALE.getRole());
                                }

                            } else if (role.equalsIgnoreCase(MyStandardRoleEnum.RESPONSABILE_DOMINIO.getMyProfileRole())) {
                                if (!profileUser.getNazionale()) {
                                    throw new UsernameNotFoundException("Un responsabile di dominio può appartenere solo all'ente standardizzatore nazionale");
                                } else {
                                    Set<MyProfilePermission> permissions = myProfileGroupRole.getPermissions();
                                    for (MyProfilePermission myProfilePermission : permissions) {
                                        MyProfilePermissionAnag permissionAnag = myProfilePermission.getPermission();
                                        if (MyStandardConstants.PERMISSION_DOMINIO.equalsIgnoreCase(permissionAnag.getPermission())) {
                                            profileUser.setDomain(myProfilePermission.getResource());
                                        } else if (MyStandardConstants.PERMISSION_CLASSE.equalsIgnoreCase(permissionAnag.getPermission())) {
                                            profileUser.addClassDomain(myProfilePermission.getResource());
                                        }
                                    }
                                    profileUser.setRole(MyStandardRoleEnum.RESPONSABILE_DOMINIO.getRole());
                                }

                            } else if (role.equalsIgnoreCase(MyStandardRoleEnum.RESPONSABILE_STANDARD.getMyProfileRole())) {
                                profileUser.setRole(MyStandardRoleEnum.RESPONSABILE_STANDARD.getRole());
                            }

                            profileUserList.add(profileUser);
                        }


                    }
                }

            }

        }

        return profileUserList;
    }
}
