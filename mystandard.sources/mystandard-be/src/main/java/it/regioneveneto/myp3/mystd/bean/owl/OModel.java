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
package it.regioneveneto.myp3.mystd.bean.owl;

import java.util.Map;
import java.util.TreeMap;

public class OModel {

    private Map<String, OClass> classes = new TreeMap<>();
    private Map<String, ORelation> relations = new TreeMap<>();

    public OModel() {
    }

    public OModel(Map<String, OClass> classes) {
        this.classes = classes;
    }

    public OModel(Map<String, OClass> classes, Map<String, ORelation> relations) {
        this.classes = classes;
        this.relations = relations;
    }

    public Map<String, OClass> getClasses() {
        return classes;
    }

    public void setClasses(Map<String, OClass> classes) {
        this.classes = classes;
    }

    public Map<String, ORelation> getRelations() {
        return relations;
    }

    public void setRelations(Map<String, ORelation> relations) {
        this.relations = relations;
    }


    public ORelation getRelation(String iri){
        return relations.get(iri);
    }

    public OClass getClass(String iri){
        return classes.get(iri);
    }
}
