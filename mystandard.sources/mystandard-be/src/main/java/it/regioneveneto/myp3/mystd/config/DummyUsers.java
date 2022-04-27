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
package it.regioneveneto.myp3.mystd.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
public class DummyUsers {
    @JsonProperty("fakeusers")
    private ArrayList<FakeUser> fakeusers;

    public ArrayList<FakeUser> getUsers() {
        return fakeusers;
    }

    public static class Profile {
        private String role;
        private String ipa;
        private boolean nazionale;
        private String domain;
        private ArrayList<String> classDomain;

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

        public Boolean getNazionale() {
            return nazionale;
        }

        public void setNazionale(Boolean nazionale) {
            this.nazionale = nazionale;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public ArrayList<String> getClassDomain() {
            return classDomain;
        }

        public void setClassDomain(ArrayList<String> classDomain) {
            this.classDomain = classDomain;
        }
    }
    public static class FakeUser {
        private String username;
        private String nome;
        private String cognome;
        private String email;
        private String telefono;
        private ArrayList<Profile> profiles;


        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCognome() {
            return cognome;
        }

        public void setCognome(String cognome) {
            this.cognome = cognome;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public ArrayList<Profile> getProfiles() {
            return profiles;
        }

        public void setProfiles(ArrayList<Profile> profiles) {
            this.profiles = profiles;
        }
    }

}

