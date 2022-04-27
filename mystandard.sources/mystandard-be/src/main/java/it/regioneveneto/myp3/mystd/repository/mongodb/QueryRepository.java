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
package it.regioneveneto.myp3.mystd.repository.mongodb;

import it.regioneveneto.myp3.mystd.bean.mongodb.QueryDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueryRepository extends MongoRepository<QueryDocument, String> {

    Page<QueryDocument> findByNameLike(String name, Pageable pageable);

    Page<QueryDocument> findByNameLikeAndDescriptionLike(String name, String description, Pageable pageable);

    Page<QueryDocument> findByDescriptionLike(String description, Pageable pageable);
}