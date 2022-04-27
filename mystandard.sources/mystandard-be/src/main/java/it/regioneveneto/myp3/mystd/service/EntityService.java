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

import it.regioneveneto.myp3.mystd.bean.MyStandardRequest;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntity;
import it.regioneveneto.myp3.mystd.exception.MyStandardException;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface EntityService {

    /**
     * Si estraggono tutte le entità per tipo
     * @param dominio, dominio entità
     * @param entityType, tipo entità da estrarre
     * @param filter, filtro di ricerca
     * @param user, utente autenticato
     * @return lista entità
     * @throws MyStandardException in caso di errore nell'estrazione degli entità

     */
    Object findAll(String dominio, String entityType, MyStandardFilter filter, UserWithAdditionalInfo user) throws MyStandardException, IOException, URISyntaxException;

    /**
     *  Si estraggono tutte le entità per tipo, e per codice
     * @param entityType, tipo entità da estrarre
     * @param codice, codice con cui estrarre le entità
     * @param versione , versione da NON estrarre
     * @return lista entità con tipo e codice richiesti
     * @throws MyStandardException  in caso di errore nell'estrazione delle entità
     */
    List<JSONObject> findAllByCodice(String entityType, String codice, Integer versione) throws MyStandardException;

    /**
     * Si estrae entità per tipo,  con un codice e una versione
     * @param dominio, dominio entità
     * @param entityType, tipo entità da estrarre
     * @param codice, codice con cui estrarre le entità
     * @param versione, versione che devono avere le entità
     * @param readonly, indica se la form sarà readonly o no
     * @param user, utente autenticato
     * @return lista entità con tipo e codice e versione richiesti
     * @throws MyStandardException  in caso di errore nell'estrazione delle entità
     */
    Object findByCodiceAndVersione(String dominio, String entityType, String codice, Integer versione, boolean readonly, UserWithAdditionalInfo user, Boolean skipDataInInverseTabs) throws MyStandardException, IOException, URISyntaxException;

    /**
     * Si estrae entità per tipo,con un codice e una versione
     * @param entityType, tipo entità da estrarre
     * @param codice, codice con cui estrarre le entità
     * @param versione, versione che devono avere le entità
     * @return Json dell'entità con tipo e codice e versione richiesti
     * @throws MyStandardException  in caso di errore nell'estrazione delle entità
     */
    JSONObject getRawEntityByCodiceAndVersione(String entityType, String codice, Integer versione) throws MyStandardException, IOException, URISyntaxException;

    /**
     * Inserimento di un nuova entità
     * @param domain, dominio da inserire
     * @param entityType, tipo entità da inserire
     * @param entity, entità da inserire
     * @param allegati, allegati da inserire
     * @param user, utente autenticato
     */
    void insertEntityByType(String domain, String entityType, MyStandardEntity entity, List<MultipartFile> allegati,UserWithAdditionalInfo user) throws MyStandardException, IOException, URISyntaxException;


    /**
     * Aggiornamento di una entità
     * @param entityType, tipo entità da aggiornare
     * @param entity, entità da aggiornare
     * @param allegati, allegati da modificare
     * @param user, utente autenticato
     */
    Object updateEntityByType(String dominio, String entityType, MyStandardEntity entity, List<MultipartFile> allegati, UserWithAdditionalInfo user) throws MyStandardException, IOException, URISyntaxException;

    /**
     * Get dati per creazione nuova entità
     * @param type, tipo entità di cui ritornare i dati
     * @param dominio, dominio dell'entità
     * @param user, utente autenticato
     * @return, dati per creazione nuova entità
     */
    Object getNewEntity(String dominio, String type, UserWithAdditionalInfo user) throws IOException, MyStandardException;


    /**
     * Eliminazione di un'entità
     * @param type, tipo entità da eliminare
     * @param codice, codice entità da eliminare
     * @param versione, versione entità da eliminare
     * @param dominio, dominio dell'entità
     * @param user, utente autenticato
     * @param myStandardRequest, oggetto contenente parametri di request come le note
     */
    void deleteEntity(String dominio, String type, String codice, Integer versione, UserWithAdditionalInfo user, MyStandardRequest myStandardRequest) throws MyStandardException, IOException, URISyntaxException;

    /**
     * Si ritorna elenco relazioni di entità associabili ad ente con codice e versione
     * @param entita, tipo relazioni da ritornare
     * @param codice, codice entità su cui ricercare relazioni associabili
     * @param versione, versione entità su cui ricercare relazioni associabili
     * @param filter, filtro di ricerca
     * @return lista relazioni
     */
    Object findRelazioniEntitaByCodiceAndVersione(String entita, String codice, Integer versione, MyStandardFilter filter) throws MyStandardException;

    /**
     * Si ritorna elenco relazioni di entità associabili
     * @param entita, tipo relazioni da ritornare
     * @param dominio, dominio entità
     * @param filter, filtro di ricerca
     * @return lista relazioni
     */
    Object findAllRelations(String entita, String dominio, MyStandardFilter filter) throws MyStandardException;

    /**
     * Si ritorna informazioni sul menu dell'applicativo
     * @return menu
     */
    JSONObject getMenuInfo() throws MyStandardException;

    /**
     * Si ritornano i dati da vocabolario per il tipo
     * @param type, tipo di cui ritornare i dati
     * @return dati vocabolario
     */
    JSONObject findDatiVocabolario(String type) throws MyStandardException;

    /**
     * Si ritorna il prossimo numero di versione da utilizzare
     * @param entita, tipo entita da cercare
     * @param codice, codice entità di cui calcolare il max versione
     * @return max versione
     */
    JSONObject findMaxVersioneByCodice(String entita, String codice) throws MyStandardException;

    /**
     * Si cerca se l'ente che definisce l'entità
     * @param idEntita, id entità di cui cercare da che entità è definita
     * @return ente che definisce entità
     */
    String isEntityDefinedByAnotherEnte( String idEntita) throws MyStandardException;

    JSONObject getDynamicEntitesByDomain() throws MyStandardException;

    Object findAll(String dominio, String entityType, MyStandardFilter filter) throws MyStandardException, IOException, URISyntaxException;

    /**
     * Si ottengono le operazioni possibili da mostrare nella pulsantiera per l'entità
     * @param entityType, tipo entità
     * @param codice, codice dell'entità
     * @param versione, versione dell'entità
     * @param user, utente che ha fatto la richiesta
     * @return
     * @throws MyStandardException
     */
    Object findAllOperations(String entityType, String codice, Integer versione, UserWithAdditionalInfo user) throws MyStandardException, IOException, URISyntaxException;

    /**
     * Esecuzione di un'operazione sull'entità che porta ad un cambio di stato
     * @param operazione, operazione da eseguire
     * @param dominio, dominio entità
     * @param entita, tipo entità  su cui eseguire l'operazione
     * @param codice, codice entità  su cui eseguire l'operazione
     * @param versione, versione entità  su cui eseguire l'operazione
     * @param user, chi sta eseguendo operazione
     * @param myStandardRequest, campi aggiuntivi alla richiesta
     * @return, dettaglio readonly dell'entità
     */
    Object genericUpdateEntityState(String operazione, String dominio, String entita, String codice, Integer versione, UserWithAdditionalInfo user, MyStandardRequest myStandardRequest) throws MyStandardException, IOException, URISyntaxException;

    /**
     * Si ritorna la lista di entità da mostrare in bacheca in relazione al ruolo dell'utente ed eventualmente alla operazione da eseguire
     * @param operazione, operazione da eseguire
     * @param filter, filtro di ricerca delle entità
     * @param user, dati utente autenticato
     * @return lista entità da mostrare in bacheca
     */
    Object findAllBacheca(MyStandardFilter filter, UserWithAdditionalInfo user, String operazione) throws MyStandardException;


    /**
     * Metodo per ottenere i dati dello storico di una entità
     * @param entita, tipo entita
     * @param codice, codice entita
     * @param versione, versinoe entita
     * @return
     */
    Object findAllStorico(String entita, String codice, Integer versione) throws MyStandardException;
}

