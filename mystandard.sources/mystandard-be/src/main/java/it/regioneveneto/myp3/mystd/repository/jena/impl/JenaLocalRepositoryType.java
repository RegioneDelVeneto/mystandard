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
package it.regioneveneto.myp3.mystd.repository.jena.impl;

import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.repository.jena.JenaRepositoryType;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionLocal;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JenaLocalRepositoryType implements JenaRepositoryType {

    @Autowired
    private MyStandardProperties myStandardProperties;
    
    @Override
    public RDFConnection getRdfConnection(MyStandardProperties myStandardProperties) {

        Dataset dataset = TDBFactory.createDataset(myStandardProperties.getOwl().getTdbStorePath());
        RDFConnection conn = new RDFConnectionLocal(dataset);
        conn.load(myStandardProperties.getOwl().getRdfDataFile());
        return conn;

    }
}
