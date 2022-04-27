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
package it.regioneveneto.myp3.mystd.bean.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe per filtri di ricerca per mystandard
 */
public class MyStandardFilter extends PageableFilter {

    private String name;
    private String Stato;
    private String CodiceEntita;
    private Boolean ipa = false;
    private String userIpa;
    private Integer Versione;
    private String domain;
    private String type;
    private List<String> stateList = new ArrayList<>();
    private Boolean opeLocaleEnte = false;
    private String enteNazionale;
    private String specializzazioneFinalState;
    private List<String> classDomain = new ArrayList<>();
    private Boolean isResponsabileDominio = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStato() {
        return Stato;
    }

    public void setStato(String stato) {
        Stato = stato;
    }

    public String getCodiceEntita() {
        return CodiceEntita;
    }

    public void setCodiceEntita(String codiceEntita) {
        CodiceEntita = codiceEntita;
    }

    public Boolean getIpa() {
        return ipa;
    }

    public void setIpa(Boolean ipa) {
        this.ipa = ipa;
    }

    public String getUserIpa() {
        return userIpa;
    }

    public void setUserIpa(String userIpa) {
        this.userIpa = userIpa;
    }

    public Integer getVersione() {
        return Versione;
    }

    public void setVersione(Integer versione) {
        Versione = versione;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getStateList() {
        return stateList;
    }

    public void setStateList(List<String> stateList) {
        this.stateList = stateList;
    }

    public Boolean getOpeLocaleEnte() {
        return opeLocaleEnte;
    }

    public void setOpeLocaleEnte(Boolean opeLocaleEnte) {
        this.opeLocaleEnte = opeLocaleEnte;
    }

    public String getEnteNazionale() {
        return enteNazionale;
    }

    public void setEnteNazionale(String enteNazionale) {
        this.enteNazionale = enteNazionale;
    }

    public List<String> getClassDomain() {
        return classDomain;
    }

    public void setClassDomain(List<String> classDomain) {
        this.classDomain = classDomain;
    }

    public Boolean getResponsabileDominio() {
        return isResponsabileDominio;
    }

    public void setResponsabileDominio(Boolean responsabileDominio) {
        isResponsabileDominio = responsabileDominio;
    }

    public String getSpecializzazioneFinalState() {
        return specializzazioneFinalState;
    }

    public void setSpecializzazioneFinalState(String specializzazioneFinalState) {
        this.specializzazioneFinalState = specializzazioneFinalState;
    }

    @Override
    public String toString() {
        return "MyStandardFilter{" +
                "name='" + name + '\'' +
                ", Stato='" + Stato + '\'' +
                ", CodiceEntita='" + CodiceEntita + '\'' +
                ", ipa=" + ipa +
                ", userIpa='" + userIpa + '\'' +
                ", Versione=" + Versione +
                ", domain='" + domain + '\'' +
                ", type='" + type + '\'' +
                ", stateList=" + stateList +
                ", opeLocaleEnte=" + opeLocaleEnte +
                ", enteNazionale='" + enteNazionale + '\'' +
                ", specializzazioneFinalState='" + specializzazioneFinalState + '\'' +
                ", classDomain=" + classDomain +
                ", isResponsabileDominio=" + isResponsabileDominio +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", sortField='" + sortField + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}
