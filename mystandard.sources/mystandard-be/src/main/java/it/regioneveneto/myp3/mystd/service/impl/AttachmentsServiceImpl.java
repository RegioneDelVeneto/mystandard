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
package it.regioneveneto.myp3.mystd.service.impl;

import it.regioneveneto.myp3.mybox.*;
import it.regioneveneto.myp3.mystd.service.AttachmentsService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

@Service
public class AttachmentsServiceImpl implements AttachmentsService {

  private final static Logger LOG = LoggerFactory.getLogger(AttachmentsServiceImpl.class);
  public static final String ALLEGATO_TEST_NAME = "allegato_test.txt";
  public static final String ALLEGATO_TEST_MIME_TYPE = "text/plain";

  @Value("${mystandard.myBoxBucket}")
  private String nomeBucket;

  @Value("${mystandard.myBoxConfigurationFilePath}")
  private String myBoxConfigurationFilePath;

  private BoxRepository boxRepository;

  @Override
  public InputStream get(String idEntita, String idAllegato) throws IOException, RepositoryAccessException, BoxRepositoryCreationException {
    this.ensureRepository();

        LOG.debug("MyStandard - Si ottiene l'allegato.");
		return boxRepository.get(nomeBucket, idEntita, idAllegato);
  }

  // @Override
  public ContentMetadata getMetadata(String idEntita, String idAllegato) throws IOException, RepositoryAccessException, BoxRepositoryCreationException {
    this.ensureRepository();

    LOG.debug("MyStandard - Si ottengono i metadati dell'allegato.");
    return boxRepository.getMetadata(nomeBucket, idEntita, idAllegato);
  }

  @Override
  public Object[] getAll(String idEntita, ArrayList<String> idAllegati) throws IOException, RepositoryAccessException, BoxRepositoryCreationException {
    InputStream result[] = new InputStream[idAllegati.size()];

    this.ensureRepository();

    for (int i = 0; i < result.length; i++) {
      result[i] = boxRepository.get(nomeBucket, idEntita, idAllegati.get(i));
    }

    return result;
  }

  /**
   * Metodo utilizzato solo per verificare il funzionamento. Da rimuovere nel test
   * @param idEntita, idEntita a cui associare l'allegato
   * @return idAllegato di test da inserire
   * @throws IOException
   * @throws RepositoryAccessException
   */
  @Override
  public String putTestFile(String idEntita) throws IOException, RepositoryAccessException {

    try {

      InputStream testFile = createTestFile(ALLEGATO_TEST_NAME);

      this.ensureRepository();

      return boxRepository.put(nomeBucket, idEntita, testFile, new ContentMetadata() {
        {
          this.setLength(testFile.available());
          this.setFileName(ALLEGATO_TEST_NAME);
          this.setMimeType(ALLEGATO_TEST_MIME_TYPE);
        }
      });

    } catch (IOException e) {
      LOG.debug("AttachmetsServiceImpl --> Errore IO nel recupero del file dalla repository mybox." , e);
      throw e;
    } catch (Exception e) {
      LOG.debug("AttachmetsServiceImpl --> Errore nell'accesso alla repository mybox." , e);
      throw new RepositoryAccessException(e);
    }
  }


  @Override
  public String put(String idEntita, String nomeAllegato, String mimeTypeAllegato, InputStream data) throws IOException, RepositoryAccessException {

    String idAllegato = null;

		String result = null;
    try {
      this.ensureRepository();

      result = boxRepository.put(nomeBucket, idEntita, data, new ContentMetadata() {
        {
          this.setLength(data.available());
          this.setFileName(nomeAllegato);
          this.setMimeType(mimeTypeAllegato);
        }
      });
    } catch (IOException e) {
      LOG.debug("AttachmetsServiceImpl --> Errore IO nel recupero del file dalla repository mybox." , e);
      throw e;
    } catch (Exception e) {
      LOG.debug("AttachmetsServiceImpl --> Errore nell'accesso alla repository mybox." , e);
      throw new RepositoryAccessException(e);
    } 
    idAllegato = result;
    return idAllegato;
  }

  @Override
  public void delete(String idEntita, String idAllegato) throws RepositoryAccessException {
    try {

      LOG.debug("MyStandard - si ottengono i dati del repository");

      this.ensureRepository(); //Si ottiene il repository

      LOG.debug("MyStandard - Eliminazione dell'allegato");

      boxRepository.delete(nomeBucket, idEntita, idAllegato);
    } catch (Exception e) {
      LOG.error("AttachmetsServiceImpl --> Errore nell'eliminazione di un file da mybox." , e);
			throw new RepositoryAccessException(e);
    } 
  }

  private void ensureRepository() throws IOException, BoxRepositoryCreationException {
    File configurationFile = new File(this.myBoxConfigurationFilePath);

    if (configurationFile.exists()) {
      InputStream stream = null;
      Exception throwable = null;

      try {
        Properties properties = new Properties();
        stream = new FileInputStream(configurationFile);
  
        properties.load(stream);
  
        this.boxRepository = BoxRepositoryFactory.getInstance(properties);  
      } catch (Exception e) {
        throwable = e;
      } finally {
        IOUtils.closeQuietly(stream);
      }

      if (throwable != null)
        throw new RuntimeException(throwable);
    }
    else
      this.boxRepository = BoxRepositoryFactory.getInstance();
  }

  private InputStream createTestFile(String nomeAllegato){
    InputStream stream = null;
    try {
      File myFile = new File(nomeAllegato);
      myFile.createNewFile();
      FileWriter writer = new FileWriter(myFile);
      writer.write("Test data");
      writer.close();
      stream = new FileInputStream(myFile);
      myFile.delete();
    } catch (IOException e) {
      LOG.debug("AttachmetsServiceImpl --> Errore nella creazione del file di test." , e);
      e.printStackTrace();
    }
    return stream;
  }

}
