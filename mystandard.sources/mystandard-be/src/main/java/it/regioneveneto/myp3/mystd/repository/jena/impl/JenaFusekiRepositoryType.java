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

import it.regioneveneto.myp3.clients.common.utils.ProxyUtils;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.repository.jena.JenaRepositoryType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;


@Configuration
public class JenaFusekiRepositoryType implements JenaRepositoryType {

    private static final Logger LOG = LoggerFactory.getLogger(JenaFusekiRepositoryType.class);

    @Override
    public RDFConnection getRdfConnection(MyStandardProperties myStandardProperties) throws MyStandardException {

        RDFConnectionRemoteBuilder builder;
        String fusekiServerUrl = myStandardProperties.getOwl().getFusekiServerUrl();
        Boolean proxyFusekiEnabled = myStandardProperties.getOwl().getFusekiProxyEnabled();

        if (proxyFusekiEnabled != null && proxyFusekiEnabled) {

            try {
                LOG.debug("MyStandard - Connecting to fuseki. Proxy enabled");

                CloseableHttpClient httpClient = ProxyUtils.getClosableHttpClient(fusekiServerUrl);

                builder = RDFConnectionFuseki.create()
                        .destination(fusekiServerUrl).httpClient(httpClient);

            } catch (URISyntaxException e) {
                LOG.error("Errore nella ricerca del proxy per l'url " + fusekiServerUrl, e);
                throw new MyStandardException("Errore nella ricerca del proxy per l'url " + fusekiServerUrl);
            }
        } else {

            LOG.debug("MyStandard - Connecting to fuseki without proxy");

            builder = RDFConnectionFuseki.create()
                        .destination(fusekiServerUrl);
        }

        return builder.build();

    }

}
