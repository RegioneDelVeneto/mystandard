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

import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class OwlJenaConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(OwlJenaConfig.class);


    @Autowired
    private MyStandardProperties myStandardProperties;

    @Bean
    public OntModel getOwlModel() {
        //Si carica l'owl

        LOGGER.debug("Lettura OWL per creazione OntModel.");

        OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        base.setStrictMode(false);
        base.getDocumentManager().setProcessImports(false);
        List<String> filenameList = myStandardProperties.getOwl().getFilename();
        for (String filename: filenameList) {

            try(final InputStream stream = Files.newInputStream(Paths.get(
                    filename))) {
                //Do something with inputstream
                base.read(stream, RDFReaderFImpl.DEFAULTLANG);
            } catch (IOException e) {//Get from classloader
                base.read(filename, RDFReaderFImpl.DEFAULTLANG);
            }
        }

        return base;

    }

}