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
import { HttpClient } from '@angular/common/http';
import { CommunicationService } from './communication.service';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';
import { Subject, from, Observable } from 'rxjs';
import { AlertsCenterService } from './alerts-center.service';
import { AutologinService } from './autologin.service';

/**
 *
 *
 * @export
 * @class AuthenticationService
 */
@Injectable({ providedIn: 'root' })
export class AuthenticationService {

  public profile: any;
  private ipas: any[];
  private role: string;
  private userDomains: any[];
  private isIpaSet: boolean;
  private roles: string[];
  private rolesSubj = new Subject();
  private userDomainsSubj = new Subject();
  private sessionIpaSubj = new Subject();
  private sessionIpa: string;

  private hasRoles: boolean = false;
  private firstTrip: boolean = true;

  public hasMultiTenant = false;

  public isLogged = false;
  public multinenantChoosen = false;

  /**
   * Creates an instance of AuthenticationService.
   * @memberof AuthenticationService
   */
  public constructor(
    private commService: CommunicationService,
    private http: HttpClient,
    private router: Router,
    private alertCenterService: AlertsCenterService,
    private autoLoginService: AutologinService
  ) {
    this.loadProfile();
  }

  public getRolesBySubscription(): Subject<any> {
    return this.rolesSubj;
  }

  public setRolesBySubscription(roles: string[]): void {
    this.rolesSubj.next(roles);
  }

  public loadProfile(): Promise<any> {
    return this.http.get(`${environment.baseApiUrl}userinfo`).toPromise()
      .then(result => {
        if (!Object.keys(result).length) {
          const status = this.commService.getLoginStatus();
          if (status) {
            this.commService.setLoginStatus(false);
          }
          return null;
        }

        this.profile = result;
        this.isIpaSet = this.profile.ipaScelto;
        this.sessionIpa = this.profile.ipa;
        if (this.isIpaSet) {
          this.commService.setIpa(this.profile.ipa);
        }
        this.ipas = this.profile.profile;
        
        if(this.userDomains)
          this.setUserDomainsBySubscription(this.userDomains);

        [this.role] = this.profile.roles;

        if (!this.profile.nome) {
          this.profile.nome = this.profile.givenName;
        }
        if (!this.profile.cognome) {
          this.profile.cognome = this.profile.familyName;
        }

        const status = this.commService.getLoginStatus();
        if (!status) {
          this.commService.setLoginStatus(true);
        }

        this.roles = this.profile.roles;
        if(this.roles && this.roles.length>0 && this.ipas){
          this.setRolesBySubscription(this.roles);
          this.autoLoginService.setHasMultipleRole(true);
          this.hasRoles = true;
        }
        else{
          this.hasRoles = false;
          this.commService.setWarningMessageAlertForUserWithNoRoles();
        }
        this.firstTrip = false;

        if (!this.profile.ipaScelto && this.roles.length>0) {
          this.autoLoginService.setHasMultipleRole(true);
          if(this.autoLoginService.hasUsedAlreadyAutologin().autologinDone){
            this.autoLoginService.setAutologinDone(this.autoLoginService.hasUsedAlreadyAutologin());
          }
          this.router.navigate(['/', 'session']);
        }
        
      }).catch(e => {
        console.error(e);
        const status = this.commService.getLoginStatus();
        if (status) {
          this.commService.setLoginStatus(false);
        }
        return null;
      });
  }

  public getHasRoles() {
    return this.hasRoles;
  }
  public getProfile(): Promise<any> {
    if (this.profile) {
      return Promise.resolve(this.profile);
    } else {
      return this.loadProfile();
    }
  }

  public getProfileFromServer(): Observable<any> {
    if(!this.hasRoles && !this.firstTrip){
      return from(new Promise(resolve => resolve(false)));
    }

    return from(this.http.get(`${environment.baseApiUrl}userinfo`).toPromise()
      .then(result => {
        if (!Object.keys(result).length) {
          return false;
        }
        return true;
      }).catch(e => {
        return false;
      })
    )
  }

  public logout(): any {
    const location = window.location;
    this.isLogged = false;
    this.autoLoginService.cleanLocalStorage();
    if(!this.roles || this.roles.length === 0) { //logout procedure for users with no roles
      this.commService.setLoginStatus(false);
      this.deleteCookie('MYSTANDARD_ACCESS_TOKEN');
      this.deleteCookie('session');
      location.reload();
      return;
    }
    this.http.post(
      `${environment.baseApiUrl}saml/logout?callbackUrl=${location}`,
      {}
    ).toPromise().then(result => {
      this.commService.setLoginStatus(false);
      location.reload();
    }).catch(error => {
      console.error(error);
      location.reload();
    });

  }

  private deleteCookie(name) {
    this.setCookie(name, '', -1);
  }

  private setCookie(name: string, value: string, expireDays: number, path: string = '') {
      let d:Date = new Date();
      d.setTime(d.getTime() + expireDays * 24 * 60 * 60 * 1000);
      let expires:string = `expires=${d.toUTCString()}`;
      let cpath:string = path ? `; path=${path}` : '';
      document.cookie = `${name}=${value}; ${expires}${cpath}`;
  }

  public getIpas(): any[] {
    return this.ipas;
  }

  public getSessionIpa(): string {
    return this.sessionIpa;
  }

  public getRole(): string {
    return this.role;
  }

  public getRoles(): string[] {
    return this.roles;
  }

  public getUserDomains(): any[] {
    return this.userDomains;
  }

  public getUserDomainsBySubscription(): Subject<any> {
    return this.userDomainsSubj;
  }

  public setUserDomainsBySubscription(domain: any[]): void {
    this.userDomainsSubj.next(domain);
  }

  public getSessionIpaBySubscription(): Subject<any> {
    return this.sessionIpaSubj;
  }

  public setSessionIpaBySubscription(sessionIpa: any): void {
    this.sessionIpaSubj.next(sessionIpa);
  }

  public getIsIpaSet(): boolean {
    return this.isIpaSet;
  }

}
