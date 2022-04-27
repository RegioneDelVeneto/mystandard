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
import { Component, OnInit, OnDestroy, ChangeDetectorRef, Input } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';

import { CommunicationService } from './services/communication.service';
import { EntityType } from './model/Model.myStandard';
import { HttpService } from './services/http.service';
import { UtilsService } from './services/utils.service';
import * as _ from 'lodash';
import { AuthenticationService } from './services/authentication.service';
import { environment } from '../environments/environment';
import { AlertsCenterService } from './services/alerts-center.service';
import { TranslateService } from '@ngx-translate/core';
/**
 *
 *
 * @export
 * @class AppComponent
 * @implements {OnInit}
 */

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit  {

  /**
   *
   *
   * @type {boolean}
   * @memberof AppComponent
   */
  public entitySelected: boolean;

  /**
   *
   *
   * @type {boolean}
   * @memberof AppComponent
   */
  public searchSelected: boolean;

  /**
   *
   *
   * @type {EntityType[]}
   * @memberof AppComponent
   */
  public entityTypes: EntityType[];

  private profile: any;

  /**
   *
   *
   * @type {boolean}
   * @memberof AppComponent
   */
  public isLogged = false;

  /**
   *
   *
   * @type {boolean}
   * @memberof AppComponent
   */
  public sidenavOpened: boolean;
  public secondSidenavOpened: boolean = false;
  public secondSidenavPaths: any = null;
  
  public paths: any;
  public routerSubscription: any;
  detailStatusSubscription: any;
  loginStatusSubscription: any;
  ipaSubscription: any;
  ipa = '';
  
  public pathClicked: string = "";
  public itemClicked: string = "";

  @Input() link: string;

  /**
   * Creates an instance of AppComponent
   * @param router
   * @param commService
   */
  public constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    public commService: CommunicationService,
    public http: HttpService,
    public utils: UtilsService,
    public alertsCenterService: AlertsCenterService,
    private authService: AuthenticationService,
    private ref: ChangeDetectorRef,
    private translate: TranslateService,
  ) {

    this.sidenavOpened = true;
    this.entitySelected = false;
    this.searchSelected = false;

    this.commService.setDetailStatus(true);
    this.commService.setModifyEnabledStatus(true);

    this.translate.setDefaultLang('it');

    this.ipaSubscription = this.commService.getIpaBySubscription().subscribe(ipa => {
      this.ipa = ipa;
    });

    this.subscribeToRouterEvents();

    this.getPaths();
    
  }

  public ngOnInit(): void {

    this.detailStatusSubscription = this.commService.getDetailStatusBySubscription().subscribe(status => {
      this.searchSelected = false;
      this.entitySelected = status;
    });

    this.loginStatusSubscription = this.commService.getLoginStatusBySubscription().subscribe(status => {
      this.isLogged = status;
      this.authService.isLogged = status;
      this.authService.getProfile().then(profile => {
        this.profile = profile;
        this.ref.detectChanges();
      });
    });
  }

  ngOnDestroy(): void {
    this.routerSubscription.unsubscribe();
    this.detailStatusSubscription.unsubscribe();
    this.loginStatusSubscription.unsubscribe();
    this.ipaSubscription.unsubscribe();
  }

  private subscribeToRouterEvents(): any {

    const setActualRoute = (url: any[], staticPaths: any[]): void => {

      const slashedUrl = '/' + url[0];
      const isStaticPath = _.includes(staticPaths, slashedUrl);
      const domain = isStaticPath ? url[0] : url[1];

      this.commService.setActualRouteDomain(domain);
      this.commService.setActualRouteSubDomain(url[2] ? url[2] : '');

    };

    this.routerSubscription = this.router.events.subscribe(ev => {

      if (ev instanceof NavigationEnd) {
        const url = this.utils.getSplittedPath(ev.url);

        if (url[0]) {
          let staticPaths = this.commService.getStaticPaths().map(el => el.url);
          if (staticPaths.length === 0) {
            this.getPaths().then(() => {
              staticPaths = this.commService.getStaticPaths().map(el => el.url);
              setActualRoute(url, staticPaths);
            });
          } else {
            setActualRoute(url, staticPaths);
          }
        }

      }

    });
  }

  private setDomainUrls(domains: any[]): void {
    _.forEach(domains, domain => {
      if (domain.items && domain.items[0]) {
        let domainUrl = domain.items[0].url;
        const idx = domainUrl.lastIndexOf('/');
        domainUrl = domainUrl.substring(0, idx);
        domain.url = domainUrl;
      }
    } );
  }

  public navigateToStaticDomain(domain: string): void {
    this.secondSidenavOpened=false;
    this.secondSidenavPaths=null;
    if(domain!=='/fileExport') {
      this.itemClicked=domain;
      if(domain==='/freequery' || domain==='/sparqlquery' || domain==='/semquery') domain = 'search/'+ domain;
      this.router.navigate([domain]);
    }
    else this.getOwl()
  }

  /**
   *
   *
   * @memberof AppComponent
   */
  public logout(): void {
    this.authService.logout();
  }

  /**
   *
   *
   * @memberof AppComponent
   */
  public login(): void {
    const location = window.location;
    window.open(`${environment.baseApiUrl}saml/login?callbackUrl=${location}`, "_self");
  }

  /**
   *
   *
   * @param {*} $event
   * @param {*} i
   * @memberof AppComponent
   */
  public onEntityTypeSelected($event, i): void {
    this.commService.setModifyEnabledStatus(true);
    this.entitySelected = false;
    this.searchSelected = false;
  }

  /**
   *
   *
   * @param {*} domain
   * @memberof AppComponent
   */
  public onDomainSelected(domain): void {
    if(this.secondSidenavPaths && this.secondSidenavPaths.label === domain.label) {
      this.secondSidenavPaths=null;
      this.secondSidenavOpened=false;
    }
    else {
      this.secondSidenavPaths=domain;
      this.secondSidenavOpened=true;
      this.pathClicked=domain.label
    }
  }

  public onSubDomainSelected(inputSubDomain: any): void {
    const splittedUrl = this.utils.getSplittedPath(inputSubDomain.url);
    this.router.navigate(['/', ...splittedUrl])
  }

  public getPaths(): Promise<any> {

    return this.http.getPaths().then(
      data => {
      data = data.items;
      this.commService.createMaps(data);
      this.setDomainUrls(data);
      this.commService.setBaseApiPath(data);
      this.paths = data;
    }, error => {
      const errorMessage = environment.showErrors ? error.error : '';
      this.alertsCenterService.showAlert({
        message: 'Si è verificato un errore nel caricamento della risorsa' + errorMessage,  type: 'danger', autoClosable: true });
    });

  }

  public mustHighlightDomain(obj: any): boolean {
    const domain = this.commService.getActualRoute().domain;
    const splittedPath = this.utils.getSplittedPath(obj.url);
    const objDomain = splittedPath ? splittedPath[1] : null;
    return domain === objDomain;
  }

  public mustHighlightSubDomain(subObj: any): boolean {
    const domain = this.commService.getActualRoute().domain;
    const subDomain = this.commService.getActualRoute().subDomain;
    const splittedPath = this.utils.getSplittedPath(subObj.url);
    const subObjDomain = splittedPath ? splittedPath[1] : null;
    const subObjSubDomain =  splittedPath ? splittedPath[2] : null;
    return domain === subObjDomain && subDomain === subObjSubDomain;
  }

  public get userName(): string {
    if (!this.profile) {
      return '';
    }
    return this.profile.nome + ' ' + this.profile.cognome;
  }

  public getOwl(): void {
    this.http.getOwl()
      .subscribe(data => {
        const newBlobText = new Blob([data], { type: 'application/rdf+xml' });
        const blobData = window.URL.createObjectURL(newBlobText);
        const link = document.createElement('a');
        link.href = blobData;
        link.download = 'owl';
        link.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
      });
  }

  public toggleSidenavs() {
    if(this.sidenavOpened) {
      if(this.secondSidenavOpened)
        {
          this.secondSidenavOpened=false;
          this.sidenavOpened=false;
          this.secondSidenavPaths=null;
        }
      else {
        this.sidenavOpened=false;
      }
    } else
    this.sidenavOpened=true;
  }
}
