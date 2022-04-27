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
package it.regioneveneto.myp3.mystd.bean.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "storico_stati")
public class StoricoDocument {

    @Id
    private String id;

    private String operation;
    private String idEntita;
    private String statoOrigine;
    private String statoDestinazione;
    private String operationDate;
    private String operationUser;
    private String note;

    public StoricoDocument(String operation, String idEntita, String statoOrigine, String statoDestinazione, String operationDate, String operationUser, String note) {
        this.operation = operation;
        this.idEntita = idEntita;
        this.statoOrigine = statoOrigine;
        this.statoDestinazione = statoDestinazione;
        this.operationDate = operationDate;
        this.operationUser = operationUser;
        this.note = note;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getIdEntita() {
        return idEntita;
    }

    public void setIdEntita(String idEntita) {
        this.idEntita = idEntita;
    }

    public String getStatoOrigine() {
        return statoOrigine;
    }

    public void setStatoOrigine(String statoOrigine) {
        this.statoOrigine = statoOrigine;
    }

    public String getStatoDestinazione() {
        return statoDestinazione;
    }

    public void setStatoDestinazione(String statoDestinazione) {
        this.statoDestinazione = statoDestinazione;
    }

    public String getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(String operationDate) {
        this.operationDate = operationDate;
    }

    public String getOperationUser() {
        return operationUser;
    }

    public void setOperationUser(String operationUser) {
        this.operationUser = operationUser;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
