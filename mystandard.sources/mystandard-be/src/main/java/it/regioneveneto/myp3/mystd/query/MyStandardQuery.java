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
package it.regioneveneto.myp3.mystd.query;

import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardStatoEnum;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardMyPortalFilter;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class MyStandardQuery {

    /**
     * Return query SPARQL per getAll a seconda del tipo di entità e di dominio
     * @param entityPrefix, prefisso da usare per entità e dominio
     * @param dominio, dominio individual
     * @param entityType, tipo entitù
     * @param filter
     * @return query SPARQL
     */
    public static String getQueryAllByEntity(MyStandardProperties myStandardProperties, String entityPrefix, String dominio, String entityType, MyStandardFilter filter) throws  MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList();

            query.addVar("?CodiceEntita");
            query.addVar("?Versione");
            query.addVar("?Stato");
            query.addVar("?name");
            query.addVar("?IPAcode");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name");

            SelectBuilder optionals = new SelectBuilder();
            optionals.addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefinitaDaUri() + ">", "?DefinitaDa");
            optionals.addWhere("?DefinitaDa", "<https://w3id.org/italia/onto/COV/IPAcode>", "?IPAcode");

            whereBuilder.addOptional(optionals);

            query.addWhere(whereBuilder);

            query.addFilter("?Versione=?MaxVersione");
            query.addFilter("?CodiceEntita=?InnerCodiceEntita");
            if (StringUtils.hasText(filter.getUserIpa())) {
                query.addFilter("?IPAcode='" + filter.getUserIpa() + "'");
            }

            SelectBuilder subQuery = new SelectBuilder();
            subQuery.addVar("max(?InnerVersione)", "?MaxVersione");
            subQuery.addVar("?InnerCodiceEntita");

            WhereBuilder subQueryWhereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "<" + entityPrefix + entityType + ">")
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?InnerVersione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?InnerCodiceEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?InnerStato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?Innername");


            subQuery.addWhere(subQueryWhereBuilder);
            subQuery.addGroupBy("?InnerCodiceEntita")
                    .addGroupBy("?InnerStato")
                    .addGroupBy("?Innername");


            getAllFilterByEntity(subQuery, dominio, entityPrefix, filter);


            query.addSubQuery(subQuery);

            query.addGroupBy("?CodiceEntita")
                    .addGroupBy("?Versione")
                    .addGroupBy("?Stato")
                    .addGroupBy("?name")
                    .addGroupBy("?IPAcode");

            getAllSortFilter(query, filter);
            getAllPaginationFilter(query, filter);

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }


    public static String getQueryCountAllByEntity(MyStandardProperties myStandardProperties, String entityPrefix, String dominio, String entityType, MyStandardFilter filter) throws ParseException {

        SelectBuilder query = getQueryPrefixesList();
        query.addVar("count(*)", "?countInd");

        SelectBuilder subQuery = new SelectBuilder();
        subQuery.addVar("?InnerCodiceEntita");
        subQuery.addVar("max(?InnerVersione)", "?MaxVersione");
        subQuery.addVar("?InnerStato");
        subQuery.addVar("?Innername");




        WhereBuilder whereBuilder = new WhereBuilder()
                .addWhere( "?individuals", RDF.Init.type(), "<" + entityPrefix + entityType + ">" )
                .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?InnerVersione")
                .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?InnerCodiceEntita")
                .addWhere("?individuals", "<" + entityPrefix + "stato>", "?InnerStato")
                .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getRelazioneMenu()  + ">", "?DominioBusiness")
                .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?Innername");

        SelectBuilder optionals = new SelectBuilder();
        optionals.addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefinitaDaUri() + ">", "?DefinitaDa");
        optionals.addWhere("?DefinitaDa", "<https://w3id.org/italia/onto/COV/IPAcode>", "?IPAcode");
        whereBuilder.addOptional(optionals);

        subQuery.addWhere(whereBuilder);

        if (StringUtils.hasText(filter.getUserIpa())) {
            subQuery.addFilter("?IPAcode='" + filter.getUserIpa() + "'");
        }

        getAllFilterByEntity(subQuery, dominio, entityPrefix, filter);


        subQuery.addGroupBy("?InnerCodiceEntita").addGroupBy("?InnerStato").addGroupBy("?Innername");
        query.addSubQuery(subQuery);


        return query.buildString();
    }


    /**
     * Return query SPARQL per ottenere le entità da mostrare in bacheca
     * @param myStandardProperties, configurazioni mystandard
     * @param filter, filtro entità
     * @return query SPARQL
     */
    public static String getQueryBachecaAllByEntity(MyStandardProperties myStandardProperties, MyStandardFilter filter) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList();

            query.addVar("?TipoEntita");
            query.addVar("GROUP_CONCAT(?InnerDominioBusiness;SEPARATOR=\",\")", "?DominioBusiness");
            query.addVar("?CodiceEntita");
            query.addVar("?Versione");
            query.addVar("?Stato");
            query.addVar("?name");
            query.addVar("?IPAcode");
            query.addVar("?dtIns");
            query.addVar("?uteIns");
            query.addVar("?dtUpdate");
            query.addVar("?uteUpdate");
            query.addVar("IF(BOUND(?dtUpdate), ?dtUpdate, ?dtIns)", "?DataUltimaModifica");


            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?InnerDominioBusiness")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "dtIns>", "?dtIns")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "uteIns>", "?uteIns");



            SelectBuilder optionals = new SelectBuilder();//Optionals per valori che potrebbero non esistenre
            optionals.addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefinitaDaUri() + ">", "?DefinitaDa");
            optionals.addWhere("?DefinitaDa", "<https://w3id.org/italia/onto/COV/IPAcode>", "?IPAcode");

            whereBuilder.addOptional(optionals);

            SelectBuilder optionalSpecializza = new SelectBuilder();
            optionalSpecializza.addWhere("?individuals", "<" + myStandardProperties.getOwl().getSpecializzaUri() + ">", "?Specializzazione");

            whereBuilder.addOptional(optionalSpecializza);



            SelectBuilder auditorUpdate = new SelectBuilder();//utente e data update potrebbero non esistere
            auditorUpdate.addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "dtUpdate>", "?dtUpdate")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "uteUpdate>", "?uteUpdate");

            whereBuilder.addOptional(auditorUpdate);

            query.addWhere(whereBuilder);

            getAllBachecaFilterByEntity(query, myStandardProperties.getOwl().getDefaultPrefix(), filter);

            query.addGroupBy("?TipoEntita")
                    .addGroupBy("?CodiceEntita")
                    .addGroupBy("?Versione")
                    .addGroupBy("?Stato")
                    .addGroupBy("?name")
                    .addGroupBy("?IPAcode")
                    .addGroupBy("?dtIns")
                    .addGroupBy("?uteIns")
                    .addGroupBy("?dtUpdate")
                    .addGroupBy("?uteUpdate");

            //Eventuale sorting
            getAllBachecaSortFilter(query, filter);
            //Eventuale pagination
            getAllPaginationFilter(query, filter);

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }


    /**
     * Si ritornano il numero totale di records per la paginazione della bacheca
     * @param myStandardProperties, configurazioni mystandard
     * @param filter, filtro entità
     * @return query SPARQL
     * @throws ParseException
     */
    public static String getQueryCountBachecaAllByEntity(MyStandardProperties myStandardProperties, MyStandardFilter filter) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList();
            query.addVar("count(*)", "?countInd");

            SelectBuilder subquery = getQueryPrefixesList();

            subquery.addVar("?TipoEntita");
            subquery.addVar("?CodiceEntita");
            subquery.addVar("?Versione");
            subquery.addVar("?Stato");
            subquery.addVar("?name");
            subquery.addVar("?IPAcode");
            subquery.addVar("?dtIns");
            subquery.addVar("?uteIns");
            subquery.addVar("?dtUpdate");
            subquery.addVar("?uteUpdate");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?InnerDominioBusiness")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "dtIns>", "?dtIns")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "uteIns>", "?uteIns");



            SelectBuilder optionals = new SelectBuilder();
            optionals.addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefinitaDaUri() + ">", "?DefinitaDa");
            optionals.addWhere("?DefinitaDa", "<https://w3id.org/italia/onto/COV/IPAcode>", "?IPAcode");

            whereBuilder.addOptional(optionals);

            SelectBuilder optionalSpecializza = new SelectBuilder();
            optionalSpecializza.addWhere("?individuals", "<" + myStandardProperties.getOwl().getSpecializzaUri() + ">", "?Specializzazione");

            whereBuilder.addOptional(optionalSpecializza);


            SelectBuilder auditorUpdate = new SelectBuilder();
            auditorUpdate.addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "dtUpdate>", "?dtUpdate")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "uteUpdate>", "?uteUpdate");

            whereBuilder.addOptional(auditorUpdate);

            subquery.addWhere(whereBuilder);


            getAllBachecaFilterByEntity(subquery, myStandardProperties.getOwl().getDefaultPrefix(), filter);


            subquery.addGroupBy("?TipoEntita")
                    .addGroupBy("?CodiceEntita")
                    .addGroupBy("?Versione")
                    .addGroupBy("?Stato")
                    .addGroupBy("?name")
                    .addGroupBy("?IPAcode")
                    .addGroupBy("?dtIns")
                    .addGroupBy("?uteIns")
                    .addGroupBy("?dtUpdate")
                    .addGroupBy("?uteUpdate");

            query.addSubQuery(subquery);

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }

    }

    /**
     * Query per verificare se ci sono entità pubblicate di recente
     * @param entityIRI, IRI entità
     * @param numberDaysNewInCatalog
     * @return
     */
    public static String getQueryCountEntityPublishedInLastDays(MyStandardProperties myStandardProperties, String entityIRI, Duration numberDaysNewInCatalog) throws MyStandardException {


        try {
            SelectBuilder query = getQueryPrefixesList();
            query.addVar("count(*)", "?countInd");

            SelectBuilder subquery = getQueryPrefixesList();
            subquery.addVar("?IdEntita");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "<" + entityIRI + ">")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "IdEntita>", "?IdEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "dtUpdate>", "?dtUpdate");


            subquery.addWhere(whereBuilder);

            subquery.addBind(" xsd:date(REPLACE(?dtUpdate, '(\\\\d{4}-\\\\d\\\\d-\\\\d\\\\d).*$', '$1'))", "?dtTimeUpdate");

            subquery.addFilter("xsd:dateTime(?dtTimeUpdate) > (now()-\"" + numberDaysNewInCatalog.toString() + "\"^^xsd:duration)");

            query.addSubQuery(subquery);

            return query.buildString();

        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }

    /**
     * Query per ottenere la data ultimo aggiornamento catalogo
     * @return, query per data ultimo aggiornamento catalogo
     */
    public static String getQueryDataUltimoAggiornamentoCatalogo(MyStandardProperties myStandardProperties) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList();

            query.addVar("MAX(?dtUpdate)", "?dataUltimoAggiornamento");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "dtUpdate>", "?dtUpdate");

            whereBuilder.addFilter("?Stato = <" + myStandardProperties.getOwl().getDefaultPrefix() + MyStandardStatoEnum.PUBBLICATO.getCode() +"> " +
                    " || ?Stato = <" + myStandardProperties.getOwl().getDefaultPrefix() + MyStandardStatoEnum.PUBBLICATO_ENTE.getCode() +"> ");



            query.addWhere(whereBuilder);

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }


    /**
     * Query per ottenere il dettaglio di un'entità, escluse le relazioni
     * @param entityPrefix, prefisso entità
     * @param codice, codice dell'entità
     * @param versione, versione dell'entità
     * @return dettaglio entità
     */
    public static String getQuerySingleEntityByCodiceVersione(String entityPrefix, String codice, Integer versione) throws MyStandardException {
        try {
            SelectBuilder query = getQueryPrefixesList()
                    .setDistinct(true)
                    .addVar("?objProp").addVar("?indProp").addVar("?dataProp").addVar("?valoreDatProp");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere( "?individuals", "<" + entityPrefix + "Versione>", "?versione" )
                    .addWhere( "?individuals", "<" + entityPrefix + "CodiceEntita>", "?codice" )
                    .addWhere( "?individuals", "?objProp", "?indProp" )
                    .addWhere( "?objProp", RDF.Init.type(), OWL.ObjectProperty)
                    .addWhere( "?objProp", "<" + entityPrefix + "relation_type>", "?reltype" )
                    .addWhere( "?indProp", "?dataProp", "?valoreDatProp" );

            whereBuilder.addFilter("?codice='" + codice +"'")
                    .addFilter("?versione='" + versione + "'")
                    .addFilter("?reltype='entity property' || ?reltype='enumeration' || ?reltype='multiple_enumeration'")
                    .addFilter("?valoreDatProp != <http://www.w3.org/2002/07/owl#NamedIndividual>");


            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere( "?individuals", "<" + entityPrefix + "Versione>", "?versione" )
                    .addWhere( "?individuals", "<" + entityPrefix + "CodiceEntita>", "?codice" )
                    .addWhere( "?individuals", "?dataProp", "?valoreDatProp" )
                    .addWhere( "?dataProp", "?y", OWL.DatatypeProperty )
                    .addFilter("?codice='" + codice + "'").addFilter("?versione='" + versione + "'"));


            query.addWhere(whereBuilder);

            return query.toString();


        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }


    }

    /**
     * Query per ottenere le relazioni di un'entità
     * @param entityPrefix, prefisso da utilizzare per la query
     * @param codice, codice dell'entità
     * @param versione, versione dell'entità
     * @return relazioni dell'entità
     */
    public static String getQueryFunctionalRelations(String entityPrefix, String codice, Integer versione) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList()
                    .setDistinct(true)
                    .addVar("?objProp").addVar("?value").addVar("?tipoCol")
                    .addVar("?nomeCol").addVar("?statoCol").addVar("?versioneCol").addVar("?codiceCol");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?versione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?codice")
                    .addWhere("?individuals", "?objProp", "?value")
                    .addWhere("?objProp", "<" + entityPrefix + "relation_type>", "?reltype")
                    .addWhere("?value", "<" + entityPrefix + "Versione>", "?versioneCol")
                    .addWhere("?value", "<" + entityPrefix + "CodiceEntita>", "?codiceCol")
                    .addWhere("?value", "<" + entityPrefix + "stato>", "?statoCol")
                    .addWhere("?tipoCol", RDFS.subClassOf, "?Class")
                    .addWhere("?value", RDF.Init.type(), "?tipoCol")
                    .addWhere("?value", "<https://w3id.org/italia/onto/l0/name> ", "?nomeCol");


            query.addWhere(whereBuilder);

            query.addFilter("?codice='" + codice + "'")
                    .addFilter("?versione='" + versione + "'")
                    .addFilter("?reltype='functional'");

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }


    /**
     * query per ottenere ipa che definisce entità
     * @param entityPrefix, prefisso entità
     * @param idEntita, identità
     * @return ipa che definsce entità
     * @throws MyStandardException
     */
    public static String getQueryIpaCodeDefinisceIdEntita(MyStandardProperties myStandardProperties, String entityPrefix, String idEntita) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList()
                    .setDistinct(true)
                    .addVar("?IPAcode");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", "<" + entityPrefix + "IdEntita>", "?IdEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefinitaDaUri() + ">", "?DefinitaDa")
                    .addWhere("?DefinitaDa", "<https://w3id.org/italia/onto/COV/IPAcode>", "?IPAcode");

            query.addWhere(whereBuilder);

            query.addFilter("?IdEntita='" + idEntita + "'");

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }

    /**
     * Query per ottenere stato ipa e dominio entità
     * @param entityPrefix, prefisso entità
     * @param entityType, tipo entità
     * @param codice, codice entità
     * @param versione, versione entità
     * @return
     * @throws MyStandardException
     */
    public static String getQueryStatoAndIpaCodeEntita(MyStandardProperties myStandardProperties, String entityPrefix, String entityType, String codice, Integer versione) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList()
                    .addVar("?Stato")
                    .addVar("?IPAcode")
                    .addVar("?DominioBusiness")
                    .addVar("?Specializzazione");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere( "?individuals", RDF.Init.type(), "<" + entityPrefix + entityType + ">" )
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getRelazioneMenu()  + ">", "?DominioBusiness");

            SelectBuilder optionals = new SelectBuilder();
            optionals.addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefinitaDaUri() + ">", "?DefinitaDa");
            optionals.addWhere("?DefinitaDa", "<https://w3id.org/italia/onto/COV/IPAcode>", "?IPAcode");


            whereBuilder.addOptional(optionals);
            SelectBuilder optionalSpecializza = new SelectBuilder();
            optionalSpecializza.addWhere("?individuals", "<" + myStandardProperties.getOwl().getSpecializzaUri() + ">", "?Specializzazione");

            whereBuilder.addOptional(optionalSpecializza);

            query.addWhere(whereBuilder);

            query.addFilter("?CodiceEntita='" + codice + "'")
                    .addFilter("?Versione='" + versione + "'");


            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }


    /**
     * Id entità di ente pubblicato più recente by ipa
     * @param entityPrefix, prefisso entità
     * @param ipa, ipa da cercare
     * @return
     * @throws MyStandardException
     */
    public static String getQueryEntePubblicatoRecenteByIPA(MyStandardProperties myStandardProperties, String entityPrefix, String ipa) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList()
                    .setDistinct(true)
                    .addVar("?IdEntita");


            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere( "?individuals", RDF.Init.type(), "<" + entityPrefix + "Ente>" )
                    .addWhere("?individuals", "<" + entityPrefix + "IdEntita>", "?IdEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getRelazioneMenu()  + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/COV/IPAcode> ", "?IPAcode");

            query.addWhere(whereBuilder);

            query.addFilter("?IPAcode='" + ipa + "'")
                    .addFilter("?Stato=<" + entityPrefix + "Pubblicato> ");


            query.addGroupBy("?IdEntita");

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }


    }


    /**
     * Si ritornano i dati da vocabolario
     * @param codeIRI, codice da vocabolario
     * @param descIRI, descrizione da vocabolario
     * @return
     */
    public static String getQueryVocabulary(String codeIRI, String descIRI) throws MyStandardException {
        try {
            SelectBuilder query = new SelectBuilder()
                    .setDistinct(true)
                    .addPrefix("covapit", "https://w3id.org/italia/onto/COV/");

            query.addVar("?Codice").addVar("SAMPLE(?Desc)", "?Descrizione");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", "<" + codeIRI + ">", "?Codice")
                    .addWhere("?individuals", "<" + descIRI + ">", "?Desc");

            query.addWhere(whereBuilder);

            query.addGroupBy("?Codice");
            query.addOrderBy("?Descrizione");

            return query.buildString();

        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }

    }

    /**
     * Return query SPARQL per storico a seconda del tipo di entità e codice
     * @param entityType, tipo entità
     * @param codice, codice
     * @return, query
     */
    public static String getQueryStoricoByEntityAndCodice(MyStandardProperties myStandardProperties, String entityType, String entityPrefix, String codice,
                                                          Integer versione) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList()
                    .setDistinct(true)
                    .addVar("?CodiceEntita").addVar("?Versione").addVar("?Stato").addVar("?name").addVar("?IPAcode");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere( "?individuals", RDF.Init.type(), "<" + entityPrefix +  entityType + ">" )
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name");

            SelectBuilder optionals = new SelectBuilder();
            optionals.addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefinitaDaUri() + ">", "?DefinitaDa");
            optionals.addWhere("?DefinitaDa", "<https://w3id.org/italia/onto/COV/IPAcode>", "?IPAcode");

            whereBuilder.addOptional(optionals);

            query.addWhere(whereBuilder);

            query.addFilter("?CodiceEntita='" + codice + "'")
                    .addFilter("?Versione<'" + versione + "'");



            query.addGroupBy("?CodiceEntita").addGroupBy("?Versione")
                    .addGroupBy("?Stato").addGroupBy("?name").addGroupBy("?IPAcode");

            query.addOrderBy("?Versione", Order.DESCENDING);

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }


    }



    /**
     * Si ritornano le relazioni associabili all'entità, escludendo se stesso
     * @param entityType, tipo entità
     * @param entityPrefix, prefisso entita
     * @param codice, codice di me stesso
     * @param versione, versione di me stesso
     * @return
     */
    public static String getQueryAllRelationsByCodiceAndVersione(String entityType, String entityPrefix, String codice,
                                                                 Integer versione, MyStandardFilter filter) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList()
                    .addVar("?individuals").addVar("?TipoEntita").addVar("?name")
                    .addVar("max(?MaxVersione)", "?Versione").addVar("?CodiceEntita").addVar("?Stato");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?Class", RDFS.subClassOf, "<" + entityPrefix + entityType + ">")
                    .addWhere("?TipoEntita", RDFS.subClassOf, "?Class")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?MaxVersione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name");
            whereBuilder
                    .addFilter("?CodiceEntita!='" + codice +"'");


            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?Class", RDFS.subClassOf, "<" + entityPrefix + entityType + ">")
                    .addWhere("?sottoclasse", RDFS.subClassOf, "?Class")
                    .addWhere("?TipoEntita", RDFS.subClassOf, "?sottoclasse")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere( "?individuals", "<" + entityPrefix + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?CodiceEntita!='" + codice +"'"));

            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?TipoEntita", RDFS.subClassOf, "<" + entityPrefix + entityType + ">")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere( "?individuals", "<" + entityPrefix + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?CodiceEntita!='" + codice +"'"));


            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "<" + entityPrefix + entityType + ">")
                    .addWhere( "?individuals", "<" + entityPrefix + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")

                    .addFilter("?CodiceEntita!='" + codice +"'"));


            query.addWhere(whereBuilder);

            getRelationsFilterByEntity(query, null, entityPrefix, filter);

            //Eventuale sorting
            getAllBachecaSortFilter(query, filter);
            //Eventuale pagination
            getAllPaginationFilter(query, filter);

            query.addGroupBy("?individuals").addGroupBy("?TipoEntita").addGroupBy("?name")
                    .addGroupBy("?CodiceEntita").addGroupBy("?Stato");

            query.addOrderBy("?TipoEntita");

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }

    }

    /**
     * Query per contare le relazioni associabili all'entità escluso se stessa
     * @param myStandardProperties, properties
     * @param entityType, tipo entità
     * @param codice, codice entita
     * @param versione, versione entita
     * @param filter, filtri entità
     * @return
     * @throws MyStandardException
     */
    public static String getQueryCountRelazioniByEntitaCodiceVersione(MyStandardProperties myStandardProperties, String entityType, String codice, Integer versione, MyStandardFilter filter) throws MyStandardException {
        try {

            SelectBuilder query = getQueryPrefixesList();
            query.addVar("count(*)", "?countInd");


            SelectBuilder subQuery = getQueryPrefixesList()

                    .addVar("?individuals").addVar("?TipoEntita").addVar("?name")
                    .addVar("max(?MaxVersione)", "?Versione").addVar("?CodiceEntita").addVar("?Stato");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?Class", RDFS.subClassOf, "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + entityType + ">")
                    .addWhere("?TipoEntita", RDFS.subClassOf, "?Class")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "Versione>", "?MaxVersione")
                    .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name");
            whereBuilder
                    .addFilter("?CodiceEntita!='" + codice +"'");


            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?Class", RDFS.subClassOf, "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + entityType + ">")
                    .addWhere("?sottoclasse", RDFS.subClassOf, "?Class")
                    .addWhere("?TipoEntita", RDFS.subClassOf, "?sottoclasse")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere( "?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?CodiceEntita!='" + codice +"'"));

            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?TipoEntita", RDFS.subClassOf, "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + entityType + ">")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere( "?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?CodiceEntita!='" + codice +"'"));


            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + entityType + ">")
                    .addWhere( "?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" +  myStandardProperties.getOwl().getDefaultPrefix()  + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")

                    .addFilter("?CodiceEntita!='" + codice +"'"));


            subQuery.addWhere(whereBuilder);

            getRelationsFilterByEntity(subQuery, null,  myStandardProperties.getOwl().getDefaultPrefix() , filter);


            subQuery.addGroupBy("?individuals").addGroupBy("?TipoEntita").addGroupBy("?name")
                    .addGroupBy("?CodiceEntita").addGroupBy("?Stato");

            subQuery.addOrderBy("?TipoEntita");
            query.addSubQuery(subQuery);


            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }

    /**
     * Si ritornano tutte le relazioni possibili per entità
     * @param entityType, tipo entità da cui ricavare le relazioni
     * @param entityPrefix, prefisso entità
     * @return
     */
    public static String getQueryAllRelationsByEntity(MyStandardProperties myStandardProperties, String entityType, String entityPrefix,
                                                      String dominio, MyStandardFilter filter) throws MyStandardException {


        try {
            SelectBuilder query = getQueryPrefixesList()
                    .addVar("?individuals").addVar("?TipoEntita").addVar("?name")
                    .addVar("max(?MaxVersione)", "?Versione").addVar("?CodiceEntita").addVar("?Stato");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?Class", RDFS.subClassOf, "<" + entityPrefix + entityType + ">")
                    .addWhere("?TipoEntita", RDFS.subClassOf, "?Class")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?MaxVersione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness");

            whereBuilder.addFilter("?DominioBusiness=<" + entityPrefix + dominio +">");

            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?Class", RDFS.subClassOf, "<" + entityPrefix + entityType + ">")
                    .addWhere("?sottoclasse", RDFS.subClassOf, "?Class")
                    .addWhere("?TipoEntita", RDFS.subClassOf, "?sottoclasse")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere( "?individuals", "<" + entityPrefix + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?DominioBusiness=<" + entityPrefix + dominio +">"));

            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?TipoEntita", RDFS.subClassOf, "<" + entityPrefix + entityType + ">")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere( "?individuals", "<" + entityPrefix + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?DominioBusiness=<" + entityPrefix + dominio +">"));

            whereBuilder.addUnion( new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "<" + entityPrefix + entityType + ">")
                    .addWhere( "?individuals", "<" + entityPrefix + "Versione>", "?MaxVersione" )
                    .addWhere( "?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita" )
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?DominioBusiness=<" + entityPrefix + dominio +">"));

            query.addWhere(whereBuilder);

            getRelationsFilterByEntity(query, dominio, entityPrefix, filter);
            //Eventuale sorting
            getAllBachecaSortFilter(query, filter);
            //Eventuale pagination
            getAllPaginationFilter(query, filter);

            query.addGroupBy("?individuals").addGroupBy("?TipoEntita").addGroupBy("?name")
                    .addGroupBy("?CodiceEntita").addGroupBy("?Stato");

            query.addOrderBy("?TipoEntita");

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }

    }


    /**
     * Query per contare le relazioni associabili all'entità
     * @param myStandardProperties, properties
     * @param entityType, tipo entità
     * @param dominio, dominio entita
     * @param filter, filtri entità
     * @return
     * @throws MyStandardException
     */
    public static String getQueryCountRelazioniAllByEntity(MyStandardProperties myStandardProperties, String dominio, String entityType, MyStandardFilter filter) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList();
            query.addVar("count(*)", "?countInd");


            SelectBuilder subQuery = getQueryPrefixesList()
                    .addVar("?individuals").addVar("?TipoEntita").addVar("?name")
                    .addVar("max(?MaxVersione)", "?Versione").addVar("?CodiceEntita").addVar("?Stato");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?Class", RDFS.subClassOf, "<" + myStandardProperties.getOwl().getDefaultPrefix() + entityType + ">")
                    .addWhere("?TipoEntita", RDFS.subClassOf, "?Class")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "Versione>", "?MaxVersione")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness");

            whereBuilder.addFilter("?DominioBusiness=<" + myStandardProperties.getOwl().getDefaultPrefix() + dominio + ">");


            whereBuilder.addUnion(new WhereBuilder()
                    .addWhere("?Class", RDFS.subClassOf, "<" + myStandardProperties.getOwl().getDefaultPrefix() + entityType + ">")
                    .addWhere("?sottoclasse", RDFS.subClassOf, "?Class")
                    .addWhere("?TipoEntita", RDFS.subClassOf, "?sottoclasse")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "Versione>", "?MaxVersione")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?DominioBusiness=<" + myStandardProperties.getOwl().getDefaultPrefix() + dominio + ">"));

            whereBuilder.addUnion(new WhereBuilder()
                    .addWhere("?TipoEntita", RDFS.subClassOf, "<" + myStandardProperties.getOwl().getDefaultPrefix() + entityType + ">")
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "Versione>", "?MaxVersione")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?DominioBusiness=<" + myStandardProperties.getOwl().getDefaultPrefix() + dominio + ">"));

            whereBuilder.addUnion(new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "<" + myStandardProperties.getOwl().getDefaultPrefix() + entityType + ">")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "Versione>", "?MaxVersione")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getDefaultPrefix() + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name")
                    .addFilter("?DominioBusiness=<" + myStandardProperties.getOwl().getDefaultPrefix() + dominio + ">"));

            subQuery.addWhere(whereBuilder);

            getRelationsFilterByEntity(subQuery, dominio, myStandardProperties.getOwl().getDefaultPrefix() , filter);

            subQuery.addGroupBy("?individuals").addGroupBy("?TipoEntita").addGroupBy("?name")
                    .addGroupBy("?CodiceEntita").addGroupBy("?Stato");

            subQuery.addOrderBy("?TipoEntita");
            query.addSubQuery(subQuery);


            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }

    }


    public static String getBuilderQueryMaxVersioneByCodice(String entityType, String entityPrefix, String codice) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList()
                    .addVar("?Versione").addVar("?Stato");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + entityPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addFilter("?CodiceEntita='" + codice + "'")
                    .addFilter("?Versione=?MaxVersione");

            SelectBuilder subQuery = new SelectBuilder()
                    .addVar("max(?InnerVersione)", "?MaxVersione");

            WhereBuilder subWhereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "<" + entityPrefix + entityType + ">")
                    .addWhere("?individuals", "<" + entityPrefix + "Versione>", "?InnerVersione")
                    .addWhere("?individuals", "<" + entityPrefix + "CodiceEntita>", "?InnerCodiceEntita")
                    .addFilter("?InnerCodiceEntita='" + codice + "'");

            subQuery.addWhere(subWhereBuilder);

            whereBuilder.addSubQuery(subQuery);

            query.addWhere(whereBuilder);

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }

    /**
     * Query per ottenere le entità per il catalogo
     * @param myStandardProperties, mystandard properties
     * @param owlPrefix, prefisso owl
     * @param myStandardMyPortalFilter, filtri di ricerca
     * @return query per ottenere le entità da catalogo
     */
    public static String getQueryCatalogoEntity(MyStandardProperties myStandardProperties, String owlPrefix, MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList();

            query.addVar("?CodiceEntita");
            query.addVar("?Versione");
            query.addVar("?name");
            query.addVar("?TipoEntita");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + owlPrefix + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + owlPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + owlPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name");

            query.addWhere(whereBuilder);

            query.addFilter("?Versione=?MaxVersione");
            query.addFilter("?CodiceEntita=?InnerCodiceEntita");
            query.addFilter("?TipoEntita=?InnerTipoEntita");

            SelectBuilder subQuery = new SelectBuilder();
            subQuery.addVar("max(?InnerVersione)", "?MaxVersione");
            subQuery.addVar("?InnerCodiceEntita");
            subQuery.addVar("?InnerTipoEntita");

            WhereBuilder subQueryWhereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "?InnerTipoEntita")
                    .addWhere("?individuals", "<" + owlPrefix + "Versione>", "?InnerVersione")
                    .addWhere("?individuals", "<" + owlPrefix + "CodiceEntita>", "?InnerCodiceEntita")
                    .addWhere("?individuals", "<" + owlPrefix + "stato>", "?InnerStato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?Innername");

            subQuery.addWhere(subQueryWhereBuilder);
            subQuery.addGroupBy("?InnerCodiceEntita")
                    .addGroupBy("?InnerStato")
                    .addGroupBy("?Innername")
                    .addGroupBy("?InnerTipoEntita");

            subQuery.addFilter("?InnerStato = <" + myStandardProperties.getOwl().getDefaultPrefix() + MyStandardStatoEnum.PUBBLICATO.getCode() +"> " +
                    " || ?InnerStato = <" + myStandardProperties.getOwl().getDefaultPrefix() + MyStandardStatoEnum.PUBBLICATO_ENTE.getCode() +"> ");


            getCatalogoFilterByEntity(subQuery, owlPrefix, myStandardMyPortalFilter);


            query.addSubQuery(subQuery);

            query.addGroupBy("?CodiceEntita")
                    .addGroupBy("?Versione")
                    .addGroupBy("?name")
                    .addGroupBy("?TipoEntita");

            getCatalogoPaginationFilter(query, myStandardMyPortalFilter);

            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }


    /**
     * Query per contare le entità per il catalogo
     * @param myStandardProperties, mystandard properties
     * @param owlPrefix, prefisso owl
     * @param myStandardMyPortalFilter, filtri di ricerca
     * @return query per contare le entità da catalogo
     */
    public static String getQueryCountCatalogoEntity(MyStandardProperties myStandardProperties, String owlPrefix, MyStandardMyPortalFilter myStandardMyPortalFilter) throws MyStandardException {

        try {
            SelectBuilder query = getQueryPrefixesList();
            query.addVar("count(*)", "?countInd");

            SelectBuilder subQuery = getQueryPrefixesList();

            subQuery.addVar("?CodiceEntita");
            subQuery.addVar("?Versione");
            subQuery.addVar("?name");
            subQuery.addVar("?TipoEntita");

            WhereBuilder whereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "?TipoEntita")
                    .addWhere("?individuals", "<" + owlPrefix + "Versione>", "?Versione")
                    .addWhere("?individuals", "<" + owlPrefix + "CodiceEntita>", "?CodiceEntita")
                    .addWhere("?individuals", "<" + owlPrefix + "stato>", "?Stato")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?name");

            subQuery.addWhere(whereBuilder);

            subQuery.addFilter("?Versione=?MaxVersione");
            subQuery.addFilter("?CodiceEntita=?InnerCodiceEntita");
            subQuery.addFilter("?TipoEntita=?InnerTipoEntita");

            SelectBuilder subSubQuery = new SelectBuilder();
            subSubQuery.addVar("max(?InnerVersione)", "?MaxVersione");
            subSubQuery.addVar("?InnerCodiceEntita");
            subSubQuery.addVar("?InnerTipoEntita");

            WhereBuilder subQueryWhereBuilder = new WhereBuilder()
                    .addWhere("?individuals", RDF.Init.type(), "?InnerTipoEntita")
                    .addWhere("?individuals", "<" + owlPrefix + "Versione>", "?InnerVersione")
                    .addWhere("?individuals", "<" + owlPrefix + "CodiceEntita>", "?InnerCodiceEntita")
                    .addWhere("?individuals", "<" + owlPrefix + "stato>", "?InnerStato")
                    .addWhere("?individuals", "<" + myStandardProperties.getOwl().getRelazioneMenu() + ">", "?DominioBusiness")
                    .addWhere("?individuals", "<https://w3id.org/italia/onto/l0/name> ", "?Innername");

            subSubQuery.addWhere(subQueryWhereBuilder);
            subSubQuery.addGroupBy("?InnerCodiceEntita")
                    .addGroupBy("?InnerStato")
                    .addGroupBy("?Innername")
                    .addGroupBy("?InnerTipoEntita");

            subSubQuery.addFilter("?InnerStato = <" + myStandardProperties.getOwl().getDefaultPrefix() + MyStandardStatoEnum.PUBBLICATO.getCode() +"> " +
                    " || ?InnerStato = <" + myStandardProperties.getOwl().getDefaultPrefix() + MyStandardStatoEnum.PUBBLICATO_ENTE.getCode() +"> ");


            getCatalogoFilterByEntity(subSubQuery, owlPrefix, myStandardMyPortalFilter);


            subQuery.addSubQuery(subSubQuery);

            subQuery.addGroupBy("?CodiceEntita")
                    .addGroupBy("?Versione")
                    .addGroupBy("?name")
                    .addGroupBy("?TipoEntita");


            query.addSubQuery(subQuery);


            return query.buildString();
        } catch (ParseException e) {
            throw new MyStandardException("Errore nella creazione della query SPARQL");
        }
    }

    /**
     * Query per ottenere object di uno statement a partire da subject e predicato
     * @param subject, soggetto statement
     * @param predicate, predicato statement
     * @return valore object
     */
    public static String getQueryObjectStatement(String subject, String predicate) {
        SelectBuilder query = getQueryPrefixesList()
                .addVar("?object");


        WhereBuilder whereBuilder = new WhereBuilder()
                .addWhere("<" + subject + ">", "<" + predicate + ">", "?object");

        query.addWhere(whereBuilder);

        return query.toString();
    }




    /**
     * Si esegue un filtro sulla query per ottenere tutte le entità
     * @param dominio, dominio su cui filtrare
     * @param entityPrefix, prefisso entità
     * @param filter, filtro
     * @param subQuery
     * @return string a filter per la query
     */
    private static void getAllFilterByEntity(SelectBuilder subQuery, String dominio, String entityPrefix, MyStandardFilter filter) throws ParseException {

        //Query per lo sfoglia catalogo: di default si estraggono solo le entità pubblicato
        subQuery.addFilter("?InnerStato = <" + entityPrefix + MyStandardStatoEnum.PUBBLICATO.getCode() +"> " +
                " || ?InnerStato = <" + entityPrefix + MyStandardStatoEnum.PUBBLICATO_ENTE.getCode() +"> ");

        //Se dominio generale, non ci deve essere il filter (tutti sono generali), altrimenti inserire dominio
        if (StringUtils.hasText(dominio) && !MyStandardConstants.DOMINIO_GENERALE.equals(dominio)) {
            subQuery.addFilter("?DominioBusiness=<" + entityPrefix + dominio +">");
        }
        if (filter != null) {
            if (StringUtils.hasText(filter.getName())) {
                subQuery.addFilter("CONTAINS(LCASE(STR(?Innername)),'" + filter.getName().toLowerCase() +"')");
            }
            if (StringUtils.hasText(filter.getStato())) {
                subQuery.addFilter("?InnerStato = <" + entityPrefix + filter.getStato() +">");
            }
            if (StringUtils.hasText(filter.getCodiceEntita())) {
                subQuery.addFilter("CONTAINS(LCASE(STR(?InnerCodiceEntita)),'" + filter.getCodiceEntita().toLowerCase() +"')");
            }
            if (filter.getVersione() != null) {
                subQuery.addFilter("?InnerVersione='" + filter.getVersione() +"'");
            }


        }

    }

    /**
     * Si esegue un filtro sulla query per ottenere tutte le entità
     * @param dominio, dominio su cui filtrare
     * @param entityPrefix, prefisso entità
     * @param filter, filtro
     * @param subQuery
     * @return string a filter per la query
     */
    private static void getRelationsFilterByEntity(SelectBuilder subQuery, String dominio, String entityPrefix, MyStandardFilter filter) throws ParseException {

        //Se dominio generale, non ci deve essere il filter (tutti sono generali), altrimenti inserire dominio
        if (StringUtils.hasText(dominio) && !MyStandardConstants.DOMINIO_GENERALE.equals(dominio)) {
            subQuery.addFilter("?DominioBusiness=<" + entityPrefix + dominio +">");
        }
        if (filter != null) {
            if (StringUtils.hasText(filter.getName())) {
                subQuery.addFilter("CONTAINS(LCASE(STR(?name)),'" + filter.getName().toLowerCase() +"')");
            }
/*            if (StringUtils.hasText(filter.getStato())) {
                subQuery.addFilter("?InnerStato = <" + entityPrefix + filter.getStato() +">");
            }*/
            if (StringUtils.hasText(filter.getCodiceEntita())) {
                subQuery.addFilter("CONTAINS(LCASE(STR(?CodiceEntita)),'" + filter.getCodiceEntita().toLowerCase() +"')");
            }
            if (filter.getVersione() != null) {
                subQuery.addFilter("?MaxVersione='" + filter.getVersione() +"'");
            }

        }

    }


    /**
     * Eventuale order by alla query
     * @param builder, selectBuilder
     * @param filter, oggetto filtro
     * @return order by alla query
     */
    private static void getAllSortFilter(SelectBuilder builder, MyStandardFilter filter) {
        if (StringUtils.hasText(mapOrderPropertyFilter(filter.getSortField()))) {
            if (StringUtils.hasText(filter.getSortDirection())) {
                Order direction = filter.getSortDirection().equals("desc") ? Order.DESCENDING : Order.ASCENDING;
                builder.addOrderBy(filter.getSortField(), direction);
            } else {
                builder.addOrderBy(filter.getSortField());
            }

        }
    }

    private static String mapOrderPropertyFilter(String orderProperty) {
        if (StringUtils.hasText(orderProperty)) {
            if (orderProperty.equals(MyStandardConstants.CODICE_ENTITA_COLUMN_KEY)) {
                return "InnerCodiceEntita";
            } else if (orderProperty.equals(MyStandardConstants.VERSIONE_COLUMN_KEY)) {
                return "InnerVersione";
            } else if (orderProperty.equals(MyStandardConstants.STATO_COLUMN_KEY)) {
                return "InnerStato";
            } else if (orderProperty.equals(MyStandardConstants.NAME_COLUMN_KEY)) {
                return "Innername";
            } else return orderProperty;
        } else return orderProperty;

    }



    /**
     * Si impostano i filtri da aggiungere in query per le entità da mostrare in bacheca
     * @param query, query su cui aggiungere i filtri
     * @param defaultPrefix, prefisso di default per le entità
     * @param filter, filtro entità
     * @throws MyStandardException errore nella creazione della query
     */
    private static void getAllBachecaFilterByEntity(SelectBuilder query, String defaultPrefix, MyStandardFilter filter) throws ParseException {

        //Si evita di tornare record che dicono che tipo entità è named individual
        query.addFilter("?TipoEntita!=<" + OWL2.NamedIndividual +">");

        if (filter != null) {
            if (StringUtils.hasText(filter.getName())) {//Filtro nome in like
                query.addFilter("CONTAINS(LCASE(STR(?name)),'" + filter.getName().toLowerCase() + "')");
            }
            //Se dominio generale, non ci deve essere il filter (tutti sono generali), altrimenti inserire dominio
            if (StringUtils.hasText(filter.getDomain()) && !MyStandardConstants.DOMINIO_GENERALE.equals(filter.getDomain())) {
                query.addFilter("?InnerDominioBusiness = <" + defaultPrefix + filter.getDomain() +">");
            }
            if (StringUtils.hasText(filter.getType())) {//Filtro tipo entità
                query.addFilter("?TipoEntita = <" + defaultPrefix + filter.getType() +">");

            }

            //Se responsabile di dominio, allora deve vedere solo le sue classi.
            //Se filter getType ha già aggiunto un filtro, potrebbe essere che il responsabile di dominio non possa accedere alla classe del filtro.
            //Eventualmente Si aggiungono comunque due filtri contradditori per non tornare nulla

            if (filter.getResponsabileDominio()) {
                String tipoEntitaListAsString = filter.getClassDomain().stream().collect(Collectors.joining(">,<" + defaultPrefix, "<" + defaultPrefix, ">"));
                query.addFilter("?TipoEntita IN (" + tipoEntitaListAsString + ")");
            }


            //se l'utente è un operatore locale, allora si mostrano solo le entità definite dall'ipa dell'utente
            if (filter.getOpeLocaleEnte()) {
                query.addFilter("?IPAcode='" + filter.getUserIpa() + "'");
            }


            if (StringUtils.hasText(filter.getEnteNazionale())) {
                Boolean enteNazionale = Boolean.parseBoolean(filter.getEnteNazionale());
                if (enteNazionale) {
                    //Si devono tornare entità definite da un ente nazionale
                    query.addFilter("!bound(?IPAcode) ");
                } else {
                    //Si devono tornare entità definite da un ente locale generico
                    query.addFilter("bound(?IPAcode) ");
                }
            }

            //Filtro su una lista di stati possibili
            if (filter.getStateList() != null && filter.getStateList().size() > 0) {
                String stateListAsString = filter.getStateList().stream().collect(Collectors.joining(">,<" + defaultPrefix, "<" + defaultPrefix, ">"));
                query.addFilter("?Stato IN (" + stateListAsString + ")");
            } else {
                query.addFilter("?Stato IN ()");
            }

            //Filtro se lo stato è uno stato finale per la
            if (StringUtils.hasText(filter.getSpecializzazioneFinalState())) {
                Boolean specializzazioneFinalState = Boolean.parseBoolean(filter.getSpecializzazioneFinalState());
                if (specializzazioneFinalState) {
                    //Si devono tornare entità definite da un ente nazionale
                    query.addFilter("!bound(?Specializzazione) ");
                }
            }

        }

    }





    /**
     * Eventuale order by alla query
     * @param builder, selectBuilder
     * @param filter, oggetto filtro
     * @return order by alla query
     */
    private static void getAllBachecaSortFilter(SelectBuilder builder, MyStandardFilter filter) {
        if (StringUtils.hasText(filter.getSortField())) {
            //Temporary fix
            if (MyStandardConstants.LABEL_TIPO_ENTITA_COLUMN_KEY.equals(filter.getSortField())) filter.setSortField(MyStandardConstants.TIPO_ENTITA_COLUMN_KEY);

            if (StringUtils.hasText(filter.getSortDirection())) {
                Order direction = filter.getSortDirection().equals("desc") ? Order.DESCENDING : Order.ASCENDING;
                builder.addOrderBy(filter.getSortField(), direction);
            } else {
                builder.addOrderBy(filter.getSortField());
            }

        }
    }





    /**
     * Eventuale paginazione della query
     * @param builder, selectbuilder
     * @param filter, oggetto filtro
     * @return paginazione alla query
     */
    private static void getAllPaginationFilter(SelectBuilder builder, MyStandardFilter filter) {

        if (filter.getPageNum() != null && filter.getPageSize() != null) {

            builder.setLimit(filter.getPageSize());
            builder.setOffset((filter.getPageNum()-1) * filter.getPageSize());

        }

    }

    /**
     * Filtro per l'estrazione delle entità per il catalogo
     * @param query, query a cui aggiungere i filtri
     * @param defaultPrefix, prefisso owl
     * @param filter, filtri entità
     */
    private static void getCatalogoFilterByEntity(SelectBuilder query, String defaultPrefix, MyStandardMyPortalFilter filter) throws ParseException {

        //Si evita di tornare record che dicono che tipo entità è named individual
        query.addFilter("?InnerTipoEntita!=<" + OWL2.NamedIndividual +">");

        //Se dominio generale, non ci deve essere il filter (tutti sono generali), altrimenti inserire dominio
        if (StringUtils.hasText(filter.getDominio()) && !MyStandardConstants.DOMINIO_GENERALE.equals(filter.getDominio())) {
            query.addFilter("?DominioBusiness = <" + defaultPrefix + filter.getDominio() +">");
        }

        if (StringUtils.hasText(filter.getNome())) {
            query.addFilter("CONTAINS(LCASE(STR(?Innername)),'" + filter.getNome().toLowerCase() +"')");
        }
        if (StringUtils.hasText(filter.getCodice())) {
            query.addFilter("CONTAINS(LCASE(STR(?InnerCodiceEntita)),'" + filter.getCodice().toLowerCase() +"')");
        }
        if (filter.getVersione() != null) {
            query.addFilter("?InnerVersione='" + filter.getVersione() +"'");
        }

        if (filter.getTipiEntita() != null && filter.getTipiEntita().size() > 0) {
            String applicazioniListAsString = filter.getTipiEntita().stream().collect(Collectors.joining(">,<" + defaultPrefix, "<" + defaultPrefix, ">"));
            query.addFilter("?InnerTipoEntita IN (" + applicazioniListAsString + ")");
        }


    }


    /**
     * Eventuale paginazione della query
     * @param builder, selectbuilder
     * @param filter, oggetto filtro
     * @return paginazione alla query
     */
    private static void getCatalogoPaginationFilter(SelectBuilder builder, MyStandardMyPortalFilter filter) {

        if (filter.getPageNum() != null && filter.getPageSize() != null) {

            builder.setLimit(filter.getPageSize());
            builder.setOffset((filter.getPageNum()-1) * filter.getPageSize());

        }

    }



    private static void getBuilderDomainsFilter(SelectBuilder query, String entityPrefix, List<String> domains) throws ParseException {
        String domainFilter = "";
        if (domains == null) {
            query.addFilter("?DominioBusiness=<" + entityPrefix + MyStandardConstants.DOMINIO_GENERALE + "> ");
        } else if (domains.size() == 1) {
            query.addFilter("?DominioBusiness=<" + entityPrefix + domains.get(0) + "> ");
        } else {

            for (int index = 0; index < domains.size(); index++) {
                if (index != 0) domainFilter += " || ";
                domainFilter += "?DominioBusiness=<" + entityPrefix + domains.get(index) + "> ";
            }
            query.addFilter(domainFilter);

        }

    }

    /**
     * Get standard prefixes
     * @return
     */
    private static SelectBuilder getQueryPrefixesList() {
        return new SelectBuilder()
                .addPrefixes( PrefixMapping.Standard );
    }


    /**
     * Creazione query di test per vedere se funziona fuseki
     * @return
     */
    public static String getHealthFusekiQuery() {

        SelectBuilder query = getQueryPrefixesList()
                .addVar("?s")
                .addVar("?p")
                .addVar("?o");


        WhereBuilder whereBuilder = new WhereBuilder()
                .addWhere("?s","?p","?o");

        query.addWhere(whereBuilder);
        query.setLimit(1);

        return query.toString();


    }
}
