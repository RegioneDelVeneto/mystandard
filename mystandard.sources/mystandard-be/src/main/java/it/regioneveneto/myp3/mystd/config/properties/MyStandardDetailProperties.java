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
package it.regioneveneto.myp3.mystd.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "detail")
public class MyStandardDetailProperties {

    private List<MyStandardDetailField> container = new ArrayList<>();
    private List<MyStandardDetailField> relations = new ArrayList<>();
    private List<MyStandardDetailField> fields = new ArrayList<>();


    public static class MyStandardDetailField {
        private String key;
        private Integer order;
        private Boolean hidden = false;
        private Boolean visibleOnlyAuthenticated = false;
        private String container;
        private String containerLocalName;
        private String prefix;
        private String prefixData;
        private Vocabulary vocabulary = new Vocabulary();
        private String stateMachineConfig;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public Boolean getHidden() {
            return hidden;
        }

        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }

        public Boolean getVisibleOnlyAuthenticated() {
            return visibleOnlyAuthenticated;
        }

        public void setVisibleOnlyAuthenticated(Boolean visibleOnlyAuthenticated) {
            this.visibleOnlyAuthenticated = visibleOnlyAuthenticated;
        }

        public String getContainer() {
            return container;
        }

        public void setContainer(String container) {
            this.container = container;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefixData() {
            return prefixData;
        }

        public void setPrefixData(String prefixData) {
            this.prefixData = prefixData;
        }

        public Vocabulary getVocabulary() {
            return vocabulary;
        }

        public void setVocabulary(Vocabulary vocabulary) {
            this.vocabulary = vocabulary;
        }

        public String getContainerLocalName() {
            return containerLocalName;
        }

        public void setContainerLocalName(String containerLocalName) {
            this.containerLocalName = containerLocalName;
        }

        public String getStateMachineConfig() {
            return stateMachineConfig;
        }

        public void setStateMachineConfig(String stateMachineConfig) {
            this.stateMachineConfig = stateMachineConfig;
        }

        public static class Vocabulary {
            private String path;
            private String codeIRI;
            private String descIRI;
            private String objPropIRI;

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public String getCodeIRI() {
                return codeIRI;
            }

            public void setCodeIRI(String codeIRI) {
                this.codeIRI = codeIRI;
            }

            public String getDescIRI() {
                return descIRI;
            }

            public void setDescIRI(String descIRI) {
                this.descIRI = descIRI;
            }

            public String getObjPropIRI() {
                return objPropIRI;
            }

            public void setObjPropIRI(String objPropIRI) {
                this.objPropIRI = objPropIRI;
            }
        }
    }

    public List<MyStandardDetailField> getContainer() {
        return container;
    }

    public void setContainer(List<MyStandardDetailField> container) {
        this.container = container;
    }

    public List<MyStandardDetailField> getRelations() {
        return relations;
    }

    public void setRelations(List<MyStandardDetailField> relations) {
        this.relations = relations;
    }

    public List<MyStandardDetailField> getFields() {
        return fields;
    }

    public void setFields(List<MyStandardDetailField> fields) {
        this.fields = fields;
    }
}
