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

public class MyStandardMyPortalFilter extends PageableFilter {

    private String dominio;

    private List<String> tipiEntita = new ArrayList<>();
    private String codice;
    private String nome;
    private Integer versione;

    //getter and setter


    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public List<String> getTipiEntita() {
        return tipiEntita;
    }

    public void setTipiEntita(List<String> tipiEntita) {
        this.tipiEntita = tipiEntita;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getVersione() {
        return versione;
    }

    public void setVersione(Integer versione) {
        this.versione = versione;
    }


}
