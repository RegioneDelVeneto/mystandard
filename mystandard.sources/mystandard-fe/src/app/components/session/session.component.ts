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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { SessionDialogComponent } from './session-dialog/session-dialog.component';
import { AuthenticationService } from '../../services/authentication.service';
import { CommunicationService } from '../../services/communication.service';
import _ from 'lodash';
import { Router } from '@angular/router';
import { AutologinService } from 'src/app/services/autologin.service';

@Component({
  selector: 'app-session',
  templateUrl: './session.component.html',
  styleUrls: ['./session.component.scss']
})
export class SessionComponent implements OnDestroy {

  rolesSubscription: any;
  isLoggedSubscription: any;
  roles: string[];
  props: any;
  dialogOpened = false;

  constructor(
    public dialog: MatDialog,
    public authService: AuthenticationService,
    public commService: CommunicationService,
    public router: Router,
    public autologinService: AutologinService
  ) {

    this.roles = this.authService.getRoles();

    this.props = {
      hasNationalRole: false,
      hasLocalRole: false,
      ipaSelected: '',
      roles: {
        list: [],
        selected: '',
      },
      localIpas: [],
      nationalIpa: '',
    };

    if(this.autologinService.getAutologinDone().autologinDone){
      this.authService.isLogged=true;
    }
    if(this.authService.multinenantChoosen) {
      window.location.replace('/summary');
    }

    if (!this.roles && this.authService.isLogged) {
      this.rolesSubscription = this.authService.getRolesBySubscription()
        .subscribe(roles => {
          this.roles = roles;
          this.setProps();
          if (this.hasOptions() && !this.authService.multinenantChoosen) { 
            this.openDialog(this.props); }
          else{
            router.navigate(['/summary']);
          }
        },
        error => {
          console.log(error);
        });
    } else {
      if(this.authService.isLogged)
         this.setProps();
      if (this.hasOptions() && !this.authService.multinenantChoosen) { 
        this.openDialog(this.props); }
        else{
          router.navigate(['/summary']);
        }
    }

    if((!this.autologinService.getAutologinDone().autologinDone || 
       !this.autologinService.hasMultipleRole()) && !this.dialogOpened && !this.hasOptions() && this.roles){
        router.navigate(['/summary']);
       }

    if(this.autologinService.hasUsedAlreadyAutologin().autologinDone && this.authService.getIsIpaSet() && !this.dialogOpened){
      window.location.replace('/summary');
    }   
   
    if(this.dialogOpened && !this.hasOptions()){
      window.location.replace('/summary');
    }
    if(this.authService.getIsIpaSet()){
      window.location.replace('/summary');
    }

  }

  private hasOptions(): boolean {
    if (this.authService.getIsIpaSet()) { return false; }
    return (this.props.hasNationalRole && this.props.hasLocalRole) || (this.props.hasLocalRole && this.props.localIpas.length > 1);
  }

  public ngOnDestroy(): void {
    if (!!this.rolesSubscription) { this.rolesSubscription.unsubscribe(); }
  }

  private setProps(): any {
    this.props.roles.list = this.roles.map(role => {
      const name = role.includes("EE_LL") ? 'Ente Locale' : 'Ente Nazionale';
      if (name === 'Ente Nazionale') { this.props.hasNationalRole = true; }
      if (name === 'Ente Locale') { this.props.hasLocalRole = true; }

      return { value: role, name };
    });
    this.props.localIpas = _.filter(this.authService.getIpas(), ipa => !ipa.nazionale).map(ipa => ipa.ipa);
    [this.props.nationalIpa] = _.filter(this.authService.getIpas(), ipa => ipa.nazionale).map(ipa => ipa.ipa);
    const tmp = 1;
  }

  public openDialog(params: any): void {
    this.authService.hasMultiTenant = true;
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.height = 'auto';
    dialogConfig.minHeight = '200px';
    dialogConfig.width = '50vw';
    dialogConfig.minWidth = '500px';
    dialogConfig.disableClose = true;
    dialogConfig.data = params;
    const dialogRef = this.dialog.open(SessionDialogComponent, dialogConfig);
    this.dialogOpened = true;
    dialogRef.afterClosed().subscribe( result => {
      if (!!result && result === 'confirm') {
        this.authService.hasMultiTenant = false;
        this.dialogOpened = false;
        this.commService.setIpa(dialogConfig.data.ipaSelected);
        this.authService.multinenantChoosen = true;
        if(this.autologinService.getAutologinDone().autologinDone || 
           this.autologinService.hasUsedAlreadyAutologin().autologinDone) {
          window.location.replace(this.autologinService.getAutologinDone().callBackUrl);
        }
        else{
          window.location.replace('/summary');
          window.location.reload();  
        }
        
      }
    });
  }

}
