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

import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntity;

/**
 * 
 */
public interface EntitySearchService {

    /**
     * 
     * 
     */
    void createIndexes();

    /**
     *
     * 
     * 
     */
    void dropIndexes();

    /**
     *
     * 
     * 
     */
    void indexAllEntities();

    /**
     * 
     * @param id
     * @param version
     * @param entityType
     * @param entity
     */
    void indexNewEntity(final String id, final long version, final String entityType, final String domain, final MyStandardEntity entity);

    /**
     * 
     * @param id
     * @param version
     * @param entityType
     * @param entity
     */
    void updateExistingEntity(final String id, final long version, final String entityType,
            final MyStandardEntity entity);

    /**
     * 
     * @param id
     * @param entityType
     */
    void removeEntityFromIndex(final String id, final long version, final String entityType);

    /**
     * 
     * @param query
     * @param entityType
     * @param offset
     * @param size
     * @return
     */
    it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchOnLatestVersions(final String query,
            final String entityType, final int offset, final int size);

    /**
     * 
     * @param query
     * @param offset
     * @param size
     * @return
     */
    it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchOnLatestVersions(final String query, final int offset,
            final int size);

    /**
     * 
     * @param query
     * @param entityType
     * @param offset
     * @param size
     * @return
     */
    it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchOnAllVersions(final String query,
            final String entityType, final int offset, final int size);

    /**
     * 
     * @param query
     * @param offset
     * @param size
     * @return
     */
    it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchOnAllVersions(final String query, final int offset,
            final int size);
}
