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
package it.regioneveneto.myp3.mystd.bean.search;

import java.util.List;

/**
 * 
 * @author Regione del Veneto
 *
 */
public class SearchResponse {

	private long totaltems;
	
	private List<SearchResult> results;

	/**
	 * @return the totaltems
	 */
	public long getTotaltems() {
		return totaltems;
	}

	/**
	 * @param totaltems the totaltems to set
	 */
	public void setTotaltems(long totaltems) {
		this.totaltems = totaltems;
	}

	/**
	 * @return the results
	 */
	public List<SearchResult> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(List<SearchResult> results) {
		this.results = results;
	}
	
}
