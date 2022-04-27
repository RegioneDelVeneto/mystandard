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

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileUser {

    private String role;
    private String ipa;
    private String domain;
    private Boolean nazionale;
    private List<String> classDomain = new ArrayList<>();


    public ProfileUser(String role, String ipa, String domain, Boolean nazionale, List<String> classDomain) {
        this.role = role;
        this.ipa = ipa;
        this.domain = domain;
        this.nazionale = nazionale;
        this.classDomain = classDomain;
    }

    public ProfileUser() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIpa() {
        return ipa;
    }

    public void setIpa(String ipa) {
        this.ipa = ipa;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getNazionale() {
        return nazionale;
    }

    public void setNazionale(Boolean nazionale) {
        this.nazionale = nazionale;
    }

    public List<String> getClassDomain() {
        return classDomain;
    }

    public void setClassDomain(List<String> classDomain) {
        this.classDomain = classDomain;
    }

    public void addClassDomain(String classDomain) {
        if (StringUtils.hasText(classDomain)) {
            List<String> domainClasses = Arrays.asList(classDomain.split(",", -1));
            this.classDomain.addAll(domainClasses);
        }

    }
}
