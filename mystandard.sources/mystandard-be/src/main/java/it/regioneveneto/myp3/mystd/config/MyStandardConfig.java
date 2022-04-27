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

import it.regioneveneto.myp3.mystd.bean.owl.OClass;
import it.regioneveneto.myp3.mystd.bean.owl.OIdentificable;
import it.regioneveneto.myp3.mystd.bean.owl.OModel;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardDetailProperties;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class MyStandardConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(MyStandardConfig.class);


    @Autowired
    private OwlJenaConfig owlJenaConfig;

    @Autowired
    private MyStandardProperties myStandardProperties;

    @Autowired
    private MyStandardDetailProperties myStandardDetailProperties;


    @Bean
    public OModel getModel() {

        LOGGER.debug("MyStandard - Creazione modello mystandard con info di tutte le classi definite in OWL.");

        OntModel base = owlJenaConfig.getOwlModel();

        OModel oModel = new OModel();

        //Si estraggono tutte le classi e si inseriscono nel model "vuote"
        ExtendedIterator<OntClass> ontClassExtendedIterator = base.listClasses();
        while (ontClassExtendedIterator.hasNext()) {
            OntClass baseClass = ontClassExtendedIterator.next();
            if (baseClass != null) {
                String baseClassURI = baseClass.getURI();
                Map<String, OClass> oModelClasses = oModel.getClasses();
                if (StringUtils.hasText(baseClassURI) && !oModelClasses.containsKey(baseClassURI)) {
                    OClass modelClass = new OClass(baseClassURI);


                    //Se nel file di configurazione c'è il prefisso, lo si cerca
                    MyStandardDetailProperties.MyStandardDetailField containerElement = myStandardDetailProperties.getContainer().stream()
                            .filter(element -> baseClass.getLocalName().equals(element.getKey()))
                            .findAny()
                            .orElse(null);

                    if (containerElement != null) {//Si settano alcune info sulle classi

                        LOGGER.debug("MyStandard - Set info di utilizzo per classe {}", baseClass.getLocalName());

                        modelClass.setPrefix(containerElement.getPrefix());
                        modelClass.setPrefixData(containerElement.getPrefixData());
                        modelClass.setLocalName(baseClass.getLocalName());
                        modelClass.setShowIpaFilter(baseClass.hasSuperClass(ResourceFactory.createResource(myStandardProperties.getOwl().getIpaFilterSuperclass())));
                        modelClass.setDescription(baseClass.getComment(null));
                        modelClass.setStateMachineConfig(containerElement.getStateMachineConfig());

                    }


                    //Set annotation property per il disegno del menu
                    Map<String, List<OIdentificable>> annotationProperties = modelClass.getAnnotationProperties();
                    List<OIdentificable> annotationList = new ArrayList<>();
                    String uri = myStandardProperties.getOwl().getMenuUri();

                    //Se per la classe c'è la annotation, si aggiunge nelle annotations
                    StmtIterator businessPropertyStmtIterator = baseClass.listProperties(ResourceFactory.createProperty(uri));
                    while (businessPropertyStmtIterator.hasNext()) {
                        Statement businessStatement = businessPropertyStmtIterator.next();
                        OIdentificable annotation = new OIdentificable(businessStatement.getObject().toString());
                        annotationList.add(annotation);
                    }

                    if (annotationList.size() > 0) {//Se ho aggiunto annotation, la aggiungo
                        annotationProperties.put(uri, annotationList);
                    }


                    oModelClasses.put(baseClassURI, modelClass);
                }
            }

        }

        LOGGER.debug("MyStandard - Il modello contiene {} classi", oModel.getClasses().size());

        return oModel;
    }


}