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
// tslint:disable
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { environment } from '../../../environments/environment';

import {
  DataTableService,
  MrfFormComponent,
  IForm
} from '@eng/morfeo';

import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { CommunicationService } from '../../services/communication.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EntityType } from 'src/app/model/Model.myStandard';
import { HttpService } from 'src/app/services/http.service';

import * as _ from 'lodash';
import { AutologinService } from 'src/app/services/autologin.service';

/**
 *
 *
 * @export
 * @class DashboardComponent
 * @implements {OnInit}
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  @ViewChild(MrfFormComponent) formRef: MrfFormComponent;

  public baseApiPath: string;
  public entitySelected: EntityType;
  public formJson: IForm;
  public isLogged: boolean = null;
  public previousUrl: string[] = null;
  public sidenavOpened: boolean = true;
  public params: any;
  public selectedEntityState = '';
  private baseApiPathSub: any;
  public entityName = '';
  public jsonFormRef: any;
  public rolesSubscription: any;
  public isLoggedSubscription: any;
  public domain: string;
  public subDomain: string;
  public option = true;
  public tableOptions= {
    filterButtonsNoIcon: true,
  };
  public entitiesCreatingIpasSubscription: any;
  public entitiesCreatingIpas: any;
  public isStructuredEntity: boolean;

  public mustShowIpa: boolean;
  public userRoles: string[];

  /**
   * 
   * @param httpService 
   * @param route 
   * @param router 
   * @param tableService 
   * @param alertsCenterService 
   * @param commService 
   */
  public constructor(
    private httpService: HttpService,
    private route: ActivatedRoute,
    private router: Router,
    private tableService: DataTableService,
    public alertsCenterService: AlertsCenterService,  
    public commService: CommunicationService,
    public activatedRoute: ActivatedRoute,
    public authService: AuthenticationService,
    public autologinService : AutologinService
  ) {
    this.activatedRoute.params.subscribe(params => {
    });

    this.entitiesCreatingIpasSubscription = this.authService.getSessionIpaBySubscription()
      .subscribe(entities => {
        this.entitiesCreatingIpas = entities;
      })
  }
  
  /**
   * 
   */
  public ngOnInit() {

    this.route.params.subscribe(params => {
      
      if (!params || !params.domain || !params.subDomain) return; 
      //this.commService.setFetchingDataStatus(true);

      this.domain = params.domain;
      this.subDomain = params.subDomain;

      this.baseApiPath = this.commService.getBaseApiPath();

      if ((this.baseApiPath === '' || !this.baseApiPath) && !this.authService.hasMultiTenant) {
        this.baseApiPathSub = this.commService.getBaseApiPathBySubscription().subscribe( baseApiPath => {
          this.baseApiPath = baseApiPath;
          this.getEntities(baseApiPath, params);
        });
      } else {
        this.getEntities(this.baseApiPath, params);
      }

      this.isLogged = this.commService.getLoginStatus();
      this.isLoggedSubscription = this.commService.getLoginStatusBySubscription().subscribe(status => {
        this.isLogged = status;
      });
    })

    this.tableService.setCallback(['table1', 'tools', 'edit'], (row) => {
      this.selectedEntityState = row.Stato;
      this.commService
      this.router.navigate(['/', this.commService.getBaseApiPath(), this.params.domain, this.params.subDomain, row.CodiceEntita, row.Versione]);
      this.commService.setDetailStatus(true);
    });

  }

  public ngOnDestroy(): void {
    if (this.baseApiPathSub) { this.baseApiPathSub.unsubscribe(); }
    if (this.rolesSubscription) { this.rolesSubscription.unsubscribe(); }
    if (this.isLoggedSubscription) { this.isLoggedSubscription.unsubscribe(); }
    if (this.entitiesCreatingIpasSubscription) { this.entitiesCreatingIpasSubscription.unsubscribe(); }
  }

  public showIpaFilter(): boolean {
    if (!this.isLogged) { return false; }

    let roles = this.authService.getRoles();
    this.userRoles = roles;

    const mustShow = () => {
      if (!roles.includes('ROLE_OPERATORE_EE_LL')) { return false; }
      else { return true; }
    }

    if (!roles) {
      this.rolesSubscription = this.authService.getRolesBySubscription()  // all profile props are available after roles have been set
        .subscribe(data => {
          roles = data;
          return mustShow();
        });
    } else { return mustShow(); }

  }

  public getEntities(baseApiPath, params): void {
    this.params = params;

    this.httpService
      .getEntities(params.domain, params.subDomain)
      .subscribe(
        data => {

          const entitiesCreatingMap = {}
          _.forEach(data.records, entity => {
            entitiesCreatingMap[entity.CodiceEntita] = entity.DefinitaDa;
          });

          this.commService.setEntitiesCreatingIpas(entitiesCreatingMap);

          const messageObj = this.commService.getResultMessage();
          if (messageObj.mustShow && messageObj.message.origin !== 'query') {
            const message = messageObj.message.message;
            this.alertsCenterService.showAlert({ message, type: 'success', autoClosable: true });
            this.commService.setResultMessage(false, null);
          }
          
          this.formJson = this.createForm(baseApiPath, params);
        },
        error => {
          const errorMessage = environment.showErrors ? error.error : '';
          this.alertsCenterService.showAlert({ message: 'Si è verificato un errore nel caricamento della risorsa' + errorMessage,  type: 'danger', autoClosable: true });
        }
      );
  }

  public navigateToFreeQuery(): void {
    this.commService.setFreeSearchType(this.params.subDomain);
    this.router.navigate(['/', 'freequery']);
  }

  public ngAfterViewInit(): void {
    this.formRef.formReadyEvent.subscribe(f => {
      this.jsonFormRef = f;
      //this.commService.setFetchingDataStatus(false);
    },
    error => {
      const errorMessage = environment.showErrors ? error.error : '';
      //this.commService.setFetchingDataStatus(false);
      this.alertsCenterService.showAlert({ message: 'Si è verificato un errore nel caricamento della risorsa' + errorMessage,  type: 'danger', autoClosable: true });
    });
  }

  /**
   *
   *
   * @private
   * @returns {IForm}
   * @memberof DashboardComponent
   */
  private createForm(baseApiPath, params: any): IForm {
    const url = `${environment.baseApiUrl}${baseApiPath}/${params.domain}/${params.subDomain}` + 
      '?pageNum=$pageNum&pageSize=$pageSize&sortField=$sortField&sortDirection=$sortDirection&$filter';

    let newForm: IForm;
    newForm = {
      components: [
        {
          type: 'dataTable',
          key: 'table1',
          dataSrc: 'url',
          data: {
            url,
            columns: [
                  { value: 'CodiceEntita', label: 'Codice', sortable: true },
                  { value: 'Versione', label: 'Versione', sortable: true },
                  { value: 'name', label: 'Nome', sortable: true },
                  { value: 'Stato', label: 'Stato', sortable: true },
                  { value: 'tools', label: 'Azioni',
                    buttons: [
                      { label: 'Dettaglio', icon: 'arrow_right_alt', action: 'edit', color: 'primary', style: 'icon' }
                    ]
                  },
            ],
            pagination: { sizeOptions: [ 10, 20, 50 ] },
            filter: {
              components: this.generateFilters(),
            },
          },
          input: true,
        }
      ]
    }
    return newForm;
  }

  /**
   *
   *
   * @private
   * @returns {any[]}
   * @memberof DashboardComponent
   */
  private generateFilters(): any[] {

    const components = [
      {
        "type": "columns",
        "columns": [
          {
            "components": [{ key: 'CodiceEntita', type: 'textfield', label: 'Codice' }],
            "width": 6
          },
          {
            "components": [{ key: 'Versione', type: 'textfield', label: 'Versione' }],
            "width": 2
          }
        ],
      },
      {
        "type": "columns",
        "columns": [
          {
            "components": [{ key: 'name', type: 'textfield', label: 'Nome' }],
            "width": 6
          },
          {
            "components": [
              {
                label: 'Stato', key: 'Stato', type: 'select',
                data: { values: [
                  { label: 'Pubblicato', value: 'Pubblicato' },
                  { label: 'Pubblicato Ente', value: 'Pubblicato_Ente' },
                  { label: '', value: '' }
                ]},
              },
              
            ],
            "width": 2
          }
        ]
      }
    ];

    this.mustShowIpa = this.getIsStructuredEntity();
    this.userRoles = this.authService.getRoles();
    
    if (!this.isLogged || !this.mustShowIpa) { return components; }

    if (this.authService.getIsIpaSet() && this.showIpaFilter()) {
      const checkbox: any = {
        label: 'Solo il mio Ente',
        key: 'ipa',
        type: 'checkbox'
      };
      components.push(checkbox);
    }
    

    return components;

  }

  private getIsStructuredEntity(): boolean {
    const ipasFilterMap = this.commService.getIpasFilterMap();
    if (!this.subDomain) {
      const tmpSubscription = this.route.params.subscribe(params => {
        this.subDomain = params.subDomain;
        this.domain = params.domain; 
        const isStructuredEntity = ipasFilterMap[this.domain].includes(this.subDomain);
        this.isStructuredEntity = isStructuredEntity;
        tmpSubscription.unsubscribe();
        return isStructuredEntity;  
      });
    } else {
      const isStructuredEntity = ipasFilterMap[this.domain].includes(this.subDomain);
        this.isStructuredEntity = isStructuredEntity;
      return isStructuredEntity;
    }
  }
  
  /**
   *
   *
   * @memberof DashboardComponent
   */
  public getNewEntity(): void {
    this.commService.setDetailStatus(true);
    this.router.navigate(['/', this.commService.getBaseApiPath(), this.params.domain, this.params.subDomain, 'newEntity']);
  }

}
