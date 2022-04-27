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

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class OwlOntApiConfig {

    @Autowired
    private MyStandardProperties myStandardProperties;


    public OntModel getOntApiModel() {
        OntModel ontapiBase = OntModelFactory.createModel();
        List<String> filenameList = myStandardProperties.getOwl().getFilename();

        for (String filename: filenameList) {

            try(final InputStream stream = Files.newInputStream(Paths.get(
                    filename))) {
                //Do something with inputstream
                ontapiBase.read(stream, RDFReaderFImpl.DEFAULTLANG);
            } catch (IOException e) {//Get from classloader
                ontapiBase.read(filename, RDFReaderFImpl.DEFAULTLANG);
            }
        }

        return ontapiBase;
    }
}