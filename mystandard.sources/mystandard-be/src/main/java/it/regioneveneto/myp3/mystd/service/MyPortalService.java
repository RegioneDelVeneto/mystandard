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
package it.regioneveneto.myp3.mystd.service;

import it.regioneveneto.myp3.mystd.bean.filter.MyStandardMyPortalFilter;
import it.regioneveneto.myp3.mystd.bean.filter.PageableFilter;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import org.json.JSONObject;

import java.util.List;

public interface MyPortalService {

    /**
     * Si ottengono i dati da mostrare nel catalogo MyPortal
     * @return dati catalogo
     * @param pageableFilter, filtri di paginazione
     */
    Object getDatiCatalogo(PageableFilter pageableFilter) throws MyStandardException;

    /**
     * Si ottiene la data ultimo aggiornamento catalogo
     * @return, data ultimo aggiornamento catalogo
     */
    JSONObject getDataUltimoAggiornamentoCatalogo() throws MyStandardException;

    /**
     * Si ottiene la lista delle entità da mostrare nell'indice
     * @return, lista tipi entità
     */
    List<JSONObject> getListaTipiEntita() throws MyStandardException;

    /**
     * Si ottiene la lista dei domini da mostrare nell'indice
     * @return lista dei domini
     */
    List<JSONObject> getListaDomini() throws MyStandardException;

    /**
     * Si ottiene la lista delle entità a partire dal tipo entita con eventuali filtri
     * @param tipoEntita, tipo entita
     * @param myStandardMyPortalFilter, eventuali filtri
     * @return lista entità
     */
    Object getListaEntitaByTipo(String tipoEntita, MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException;

    /**
     * Si ottiene la lista delle entità a partire dal dominio entita con eventuali filtri
     * @param dominio, dominio entita
     * @param myStandardMyPortalFilter, eventuali filtri
     * @return lista entità
     */
    Object getListaEntitaByDominio(String dominio, MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException;
}
