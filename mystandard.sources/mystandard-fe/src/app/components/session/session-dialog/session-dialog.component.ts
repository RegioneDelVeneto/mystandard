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
import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { CommunicationService } from 'src/app/services/communication.service';
import { environment } from 'src/environments/environment';
import { HttpService } from '../../../services/http.service';

@Component({
  selector: 'app-session-dialog',
  templateUrl: './session-dialog.component.html',
  styleUrls: ['./session-dialog.component.scss']
})
export class SessionDialogComponent {
  @ViewChild('ipas') ipas;

  public dataReference: any;
  public buttonsTexts = { national: '', local: ''};
  public messages = { role: '', ipa: ''};

  constructor(
    @Inject(MAT_DIALOG_DATA) data,
    public dialogRef: MatDialogRef<SessionDialogComponent>,
    public http: HttpService,
    public commService: CommunicationService,
    public alertsCenterService: AlertsCenterService,
    public authService: AuthenticationService
  ) {
    this.dataReference = data;
    this.messages.ipa = `Per quale Ente Locale vuoi operare?`;
    this.buttonsTexts.local = 'Ente Locale';

    if (data.hasNationalRole) {
      this.buttonsTexts.national = 'Ente Nazionale';
      if (data.localIpas.length > 1) {
        this.messages.role = `Vuoi operare per l'Ente Nazionale o per un Ente Locale?`;
      } else {
        this.messages.role = `Vuoi operare per l'Ente Nazionale o per l'Ente Locale?`;
      }
    } else {
      this.dataReference.roles.selected = 'local';
    }
  }

  public entitySelected(entityType: string): void {
    this.dataReference.roles.selected = entityType;
    if (entityType === 'national') {
      this.dataReference.ipaSelected = this.dataReference.nationalIpa;
      this.postIpa(this.dataReference.ipaSelected, [this.dataReference.roles.list[0].value]);
    } else if (this.dataReference.localIpas.length === 1){
      [this.dataReference.ipaSelected] = this.dataReference.localIpas;
      this.postIpa(this.dataReference.ipaSelected, [this.dataReference.roles.list[0].value]);
    }
  }

  private postIpa(ipa: string, role: string[]): void {
    //this.commService.setFetchingDataStatus(true);
    this.http.postIpa(ipa)
      .subscribe(data => {
        this.authService.setRolesBySubscription(role);
        //this.commService.setFetchingDataStatus(false);
        this.dialogRef.close('confirm');
        // this.authService.setRolesBySubscription()
      },
      error => {
        const errorMessage = environment.showErrors ? error.error : '';
        //this.commService.setFetchingDataStatus(false);
        this.alertsCenterService.showAlert({
          message: 'Si è verificato un errore nel caricamento della risorsa' + errorMessage,
          type: 'danger',
          autoClosable: true
        });
      });
  }

  onConfirmClick(): void {
    const ipa = this.ipas.selectedOptions.selected[0]?.value;
    this.dataReference.ipaSelected = ipa;
    this.postIpa(ipa, [this.dataReference.roles.list[0].value]);
  }

  onGoBackClick(): void {
    this.dataReference.roles.selected = '';
  }

}
