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
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { SnackbarComponent } from './snackbar/snackbar.component';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private baseUrl = '';

  constructor(private snackBar: MatSnackBar) {

    this.alertSubject.subscribe((val: { method: string, url: string, snackType: string }) => {
      const method = val.method;
      const url = val.url;
      const snackType = val.snackType;
      this.openSnackBar(this.getMessage(method, url, snackType), snackType);
    });
  }

  private endpointMsg: Object = {
    'acl-general-error': {
      POST: {
        Error: 'Non disponi delle autorizazazioni necessarie a questa operazione'
      }
    },
    '/invito/evento': {
      POST: {
        Error: `Errore durante l'inserimento dell'invito`,
        Success: `Inserimento dell'invito avvenuto con sucesso`
      }
    },
    '/utente-messaggi': {
      POST: {
        Error: `Errore durante l'inserimento del messaggio`,
        Success: `Inserimento del messaggio avvenuto con sucesso`
      },
      PUT: {
        Error: `Errore durante la modifica del messaggio`,
        Success: `Modifica del messaggio avvenuto con sucesso`
      },
      PATCH: {
        Error: `Errore durante la modifica del messaggio`,
        Success: `Modifica del messaggio avvenuto con sucesso`
      },
    },
    '/comunicazione': {
      POST: {
        Error: `Errore durante l'inserimento della communicazione`,
        Success: `Inserimento della communicazion avvenuto con sucesso`
      }
    },
    '/iscritto-evento': {
      POST: {
        Error: `Errore durante l'inserimento dell'iscrizione all'evento`,
        Success: `Iscrizione all'evento avvenuta con sucesso`
      },
      PATCH: {
        Error: `Errore durante la modifica dell'iscrizione all'evento`,
        Success: `Modifica all'iscrizione all'evento avvenuta con sucesso`
      },
    },
    '/iscritto-evento/operazione-massiva': {
      POST: {
        Error: `Errore durante l'operazione sugli iscritti`,
        Success: `Operazione sugli iscritti all'evento avvenuta con sucesso`
      },
    },
    '/iscritto-evento/{idEvento}/comunicazione': {
      POST: {
        Error: `Errore durante l'invio della comunicazione agli iscritti`,
        Success: `Invio della comunicazione agli iscritti all'evento avvenuta con sucesso`
      },
    }

  };

  private generalError = `Errore durante il completamento dell'operazione`;

  private alertSubject: Subject<{ method: string, url: string, snackType: string }> = new Subject<{ method: string, url: string, snackType: string }>();

  private progressBarSubject: Subject<{ progress: number, operation: string, id: string }> = new Subject<{ progress: number, operation: string, id: string }>();


  private openSnackBar(msg: string, snackType: string) {
    const _snackType: string =
      snackType !== undefined ? snackType : 'Success';
    if (!msg) { return null; }
    this.snackBar.openFromComponent(SnackbarComponent, {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      data: { msg, snackType },
      panelClass: ['msg-snackbar-' + snackType.toLocaleLowerCase()]
    });
  }

  public getAlertSubj() {
    return this.alertSubject;
  }

  public passDataToSubject(data: { method: string, url: string, snackType: string }) {
    this.alertSubject.next(data);
  }

  private getMessage(methodData: string, methodUrl: string, snackType: string): string {
    const method = methodData;
    const type = snackType;
    let url = methodUrl.split('?')[0].replace(this.baseUrl, '');
    if (url.split('/')[1] === 'iscritto-evento' && url.split('/').length > 3 && url.split('/')[url.split('/').length - 1] === 'comunicazione') {
      url = '/iscritto-evento/{idEvento}/comunicazione';
    }
    if (url && this.endpointMsg.hasOwnProperty(url.split('?')[0].replace(this.baseUrl, ''))
      && (this.endpointMsg[url] as Object).hasOwnProperty(method)
      && (this.endpointMsg[url][method] as Object).hasOwnProperty(type)) {
      return this.endpointMsg[url][method][type];
    }
    if (snackType === 'Error') {
      return this.generalError;
    }
    return null;
  }

  public getProgressBarSubject(): Subject<{ progress: number, operation: string, id: string }> {
    return this.progressBarSubject;
  }

  public passDataToProgressSubject(data: { progress: number, operation: string, id: string }): void {
    this.progressBarSubject.next(data);
  }

  public completeProgress(operaion: string, id: string) {
    this.progressBarSubject.next({ progress: 101, operation: operaion, id });
  }

}
