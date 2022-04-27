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

import it.regioneveneto.myp3.mybox.BoxRepositoryCreationException;
import it.regioneveneto.myp3.mybox.ContentMetadata;
import it.regioneveneto.myp3.mybox.RepositoryAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public interface AttachmentsService {

  InputStream get(String idEntita, String idAllegato) throws IOException, RepositoryAccessException, BoxRepositoryCreationException;

  ContentMetadata getMetadata(String idEntita, String idAllegato) throws IOException, RepositoryAccessException, BoxRepositoryCreationException;

  Object[] getAll(String idEntita, ArrayList<String> idsAllegati) throws IOException, RepositoryAccessException, BoxRepositoryCreationException;

  String putTestFile(String idEntita) throws IOException, RepositoryAccessException;

  String put(String idEntita, String nomeAllegato, String mimeTypeAllegato, InputStream data) throws IOException, RepositoryAccessException;

  void delete(String idEntita, String idAllegato) throws RepositoryAccessException;

}
