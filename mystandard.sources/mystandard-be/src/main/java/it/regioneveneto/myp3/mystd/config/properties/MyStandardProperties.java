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
package it.regioneveneto.myp3.mystd.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "mystandard")
public class MyStandardProperties {

    private String enteNazionale;
    private Owl owl = new Owl();
    private List<MyStandardStaticMenu> staticMenu = new ArrayList<>();
    private String stateMachine;


    public class Owl{

        private String fusekiServerUrl;
        private Boolean fusekiProxyEnabled;
        private List<String> rdfFiles = new ArrayList<>();
        private List<String> filename;
        private String ontopiaFilename;
        private String nativeStorePath;
        private String tdbStorePath;
        private String rdfDataFile;
        private String menuUri;
        private String relazioneMenu;//Functional property che raggruppa menu per dominio
        private String ipaFilterSuperclass;
        private String definitaDaUri;
        private String definisceUri;
        private String specializzataDaUri;
        private String specializzaUri;
        private String mainDomain;//Indica se il dominio è quello principale o secondario
        private String defaultPrefix;

        public String getFusekiServerUrl() {
            return fusekiServerUrl;
        }

        public void setFusekiServerUrl(String fusekiServerUrl) {
            this.fusekiServerUrl = fusekiServerUrl;
        }

        public Boolean getFusekiProxyEnabled() {
            return fusekiProxyEnabled;
        }

        public void setFusekiProxyEnabled(Boolean fusekiProxyEnabled) {
            this.fusekiProxyEnabled = fusekiProxyEnabled;
        }

        public List<String> getFilename() {
            return filename;
        }

        public void setFilename(List<String> filename) {
            this.filename = filename;
        }

        public List<String> getRdfFiles() {
            return rdfFiles;
        }

        public void setRdfFiles(List<String> rdfFiles) {
            this.rdfFiles = rdfFiles;
        }

        public String getOntopiaFilename() {
            return ontopiaFilename;
        }

        public void setOntopiaFilename(String ontopiaFilename) {
            this.ontopiaFilename = ontopiaFilename;
        }

        public String getNativeStorePath() {
            return nativeStorePath;
        }

        public void setNativeStorePath(String nativeStorePath) {
            this.nativeStorePath = nativeStorePath;
        }

        public String getTdbStorePath() {
            return tdbStorePath;
        }

        public void setTdbStorePath(String tdbStorePath) {
            this.tdbStorePath = tdbStorePath;
        }

        public String getRdfDataFile() {
            return rdfDataFile;
        }

        public void setRdfDataFile(String rdfDataFile) {
            this.rdfDataFile = rdfDataFile;
        }

        public String getMenuUri() {
            return menuUri;
        }

        public void setMenuUri(String menuUri) {
            this.menuUri = menuUri;
        }

        public String getRelazioneMenu() {
            return relazioneMenu;
        }

        public void setRelazioneMenu(String relazioneMenu) {
            this.relazioneMenu = relazioneMenu;
        }

        public String getIpaFilterSuperclass() {
            return ipaFilterSuperclass;
        }

        public void setIpaFilterSuperclass(String ipaFilterSuperclass) {
            this.ipaFilterSuperclass = ipaFilterSuperclass;
        }

        public String getDefinitaDaUri() {
            return definitaDaUri;
        }

        public void setDefinitaDaUri(String definitaDaUri) {
            this.definitaDaUri = definitaDaUri;
        }

        public String getDefinisceUri() {
            return definisceUri;
        }

        public void setDefinisceUri(String definisceUri) {
            this.definisceUri = definisceUri;
        }

        public String getMainDomain() {
            return mainDomain;
        }

        public void setMainDomain(String mainDomain) {
            this.mainDomain = mainDomain;
        }

        public String getSpecializzataDaUri() {
            return specializzataDaUri;
        }

        public void setSpecializzataDaUri(String specializzataDaUri) {
            this.specializzataDaUri = specializzataDaUri;
        }

        public String getSpecializzaUri() {
            return specializzaUri;
        }

        public void setSpecializzaUri(String specializzaUri) {
            this.specializzaUri = specializzaUri;
        }

        public String getDefaultPrefix() {
            return defaultPrefix;
        }

        public void setDefaultPrefix(String defaultPrefix) {
            this.defaultPrefix = defaultPrefix;
        }
    }

    public static class MyStandardStaticMenu {

        private String main;
        private String url;
        private String label;
        private Boolean visibleOnlyAuthenticated = false;

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Boolean getVisibleOnlyAuthenticated() {
            return visibleOnlyAuthenticated;
        }

        public void setVisibleOnlyAuthenticated(Boolean visibleOnlyAuthenticated) {
            this.visibleOnlyAuthenticated = visibleOnlyAuthenticated;
        }
    }

    public String getEnteNazionale() {
        return enteNazionale;
    }

    public void setEnteNazionale(String enteNazionale) {
        this.enteNazionale = enteNazionale;
    }

    public Owl getOwl() {
        return owl;
    }

    public void setOwl(Owl owl) {
        this.owl = owl;
    }

    public List<MyStandardStaticMenu> getStaticMenu() {
        return staticMenu;
    }

    public void setStaticMenu(List<MyStandardStaticMenu> staticMenu) {
        this.staticMenu = staticMenu;
    }


    public String getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(String stateMachine) {
        this.stateMachine = stateMachine;
    }
}
