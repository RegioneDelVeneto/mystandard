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
0// tslint:disable
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { AttachmentsComponent } from './attachments/attachments.component';
import { AttachmentsTableComponent } from './attachments-table/attachments-table.component';
import { CommunicationService } from 'src/app/services/communication.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { FieldsComponent } from './fields/fields.component';
import { HttpService } from 'src/app/services/http.service';
import { TabsComponent } from './tabs/tabs.component';
import { OperationType } from 'src/app/model/Model.myStandard';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ConfirmDialogComponent } from './confirm-dialog/confirm-dialog.component';

import * as _ from 'lodash';
import { environment } from 'src/environments/environment';
import { UtilsService } from 'src/app/services/utils.service';
import { ConfirmOperationDialogComponent } from './confirm-operation-dialog/confirm-operation-dialog.component';
import {  debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { nextTick } from 'process';
import { AutologinService } from 'src/app/services/autologin.service';

/**
 *
 *
 * @export
 * @class DetailComponent
 * @implements {OnInit}
 */
@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss'],
})
export class DetailComponent implements OnInit, OnDestroy {

  @ViewChild(FieldsComponent) childFields: FieldsComponent;
  @ViewChild(AttachmentsComponent) childAttachments: AttachmentsComponent;
  @ViewChild(AttachmentsTableComponent) childTableAttachments: AttachmentsTableComponent;
  @ViewChild(TabsComponent) childTabs: TabsComponent;

  /**
   *
   *
   * @type {*}
   * @memberof DetailComponent
   */
  public attachments: any;

  /**
   *
   *
   * @type {*}
   * @memberof DetailComponent
   */
  public tableAttachments: any;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public cancel: boolean;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public editState: boolean;

  /**
   *
   *
   * @type {boolean}
   * @memberof AttachmentsComponent
   */
  public hasAttachments_old: boolean;

  /**
   *
   *
   * @type {boolean}
   * @memberof AttachmentsComponent
   */
  public hasAttachments: boolean;

  /**
   *
   *
   * @type {any[]}
   * @memberof DetailComponent
   */
  public hiddenComponents: any[];

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public isLogged: boolean;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public isModifyEnabled: boolean;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public isNew: boolean;

  /**
   *
   *
   * @private
   * @type {*}
   * @memberof DetailComponent
   */
  private keysMap: any;

  /**
   *
   *
   * @type {*}
   * @memberof DetailComponent
   */
  public masterFields: any;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public showAccordion: boolean;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public showAttachments_old = true;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public showAttachments = true;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public showForms: boolean;

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public showTabs: boolean;

  /**
   *
   *
   * @type {*}
   * @memberof DetailComponent
   */
  public tabsFields: any;

  /**
   *
   *
   * @type {any[]}
   * @memberof DetailComponent
   */
  public attachmentsInitialValues: any[];

  /**
   *
   *
   * @type {*}
   * @memberof DetailComponent
   */
  public attachmentsMap: any;

  /**
   *
   *
   * @type {*}
   * @memberof TabsComponent
   */
  public modifiedRelationships: any;

  /**
   *
   *
   * @type {*}
   * @memberof AttachmentsComponent
   */
  public initialAttachments: any;

  public restoredVersionSubj: any;
  public isRestoredVersion: any;
  public originalVersionKeysMap: any;
  public originalAttachments: any;

  public operations = {
    MODIFICA: false,
    ELIMINA: false,
    APPROVA: false,
    PUBBLICA: false,
    RIFIUTA: false,
    TRASMETTI: false,
    PUBBLICA_COME_STANDARD: false,
    RIFIUTA_COME_STANDARD: false,
    CREA_NUOVA_VERSIONE: false,
    SPECIALIZZA: false,
  };

  public operationsDictionary = {
    MODIFICA: 'Modifica',
    ELIMINA: 'Elimina',
    APPROVA: 'Approva',
    PUBBLICA: 'Pubblica',
    RIFIUTA: 'Rifiuta',
    TRASMETTI: 'Trasmetti',
    PUBBLICA_COME_STANDARD: 'Pubblica come standard',
    RIFIUTA_COME_STANDARD: 'Rifiuta come standard',
    CREA_NUOVA_VERSIONE: 'Crea nuova versione',
    SPECIALIZZA: 'Specializza',
  };


  public showOperationsInSelect = false;
  public operationsList = []
  public operationSelected: any;

  public specializedVersionOrigin: any;

  public attachmentPropsMap = {
    name: 'https://mystandard.regione.veneto.it/onto/BPO#nome_allegato',
    type: 'https://mystandard.regione.veneto.it/onto/BPO#tipoFile',
    id: 'https://mystandard.regione.veneto.it/onto/BPO#id_documento',
    lastModifiedDate: 'https://w3id.org/italia/onto/TI/date',
    lastModified: 'lastmodified',
    description: 'https://mystandard.regione.veneto.it/onto/BPO#descrizione_allegato',
    custom_ind_name: 'custom_ind_name',

    name_old: 'mystd_nome_allegato',
    type_old: 'mystd_tipoFile',
    lastModifiedDate_old: 'mystd_date',
    lastModified_old: 'lastmodified',
    description_old: 'mystd_descrizione_allegato',
  }

  /**
   *
   *
   * @type {boolean}
   * @memberof DetailComponent
   */
  public isNewVersion: boolean;
  public isSpecializedVersion: boolean;
  newUploadedAttachment_old: any;
  newUploadedAttachment: any;
  uploadedAttachments = [];
  deletedAttachments = [];
  params: any;
  entityState: string;
  entityStateSubscription: any;
  entityName: string;
  entityNameSubscription: any;
  isFormValid: any;
  formsValidationStatus = { fields: false, attachments: true };
  dialogModifiedEntities = {};

  public domain = '';
  public subDomain = '';
  private modificableEntity: any;
  public entitiesCreatingIpasSubscription: any;
  public entitiesCreatingIpas: any;

  private priorRestoredVersionAttachments;

  private code;
  private version;

  public summary;
  public summary_fields: any = []

  public newVersionHistoricalVersions: any;

  /**
   * Creates an instance of DetailComponent.
   * @param {ActivatedRoute} route
   * @param {HttpService} http
   * @param {AlertsCenterService} alertsCenterService
   * @param {CommunicationService} commService
   * @memberof DetailComponent
   */
  constructor(
    private route: ActivatedRoute,
    private http: HttpService,
    private alertsCenterService: AlertsCenterService,
    private router: Router,
    public dialog: MatDialog,
    public commService: CommunicationService,
    public authService: AuthenticationService,
    private utils: UtilsService,
    private autoLoginService: AutologinService
  ) {

    this.domain = this.route.snapshot.params.domain;
    this.subDomain = this.route.snapshot.params.subDomain;
    this.code = this.route.snapshot.params.code;
    this.version = this.route.snapshot.params.version;

    this.modifiedRelationships = {};
    this.hasAttachments_old = true;
    this.showAccordion = false;
    this.cancel = false;

    this.keysMap = this.resetMap();
    this.attachmentsInitialValues = [];
    this.hiddenComponents = [];
    this.showTabs = true;
    this.attachmentsMap = {};
    this.isNewVersion = false;
    this.isSpecializedVersion = false;
    this.entityState = '';
    this.entityStateSubscription = this.commService.getEntityStateBySubscription().subscribe(state => {
      this.entityState = state;
    });

    this.entityName = '';

    this.entityNameSubscription = this.commService.getEntityNameBySubscription().subscribe(name => {
      this.entityName = name;
    });

    this.entitiesCreatingIpas = this.commService.getEntitiesCreatingIpas();

    if (
      (this.entitiesCreatingIpas === undefined || this.entitiesCreatingIpas === '')
      && this.params) {
      this.http.getEntities(this.params.domain, this.params.subDomain)
        .subscribe(data => {
          const entitiesCreatingMap = {}
          _.forEach(data.records, entity => {
            entitiesCreatingMap[entity.CodiceEntita] = entity.DefinitaDa;
          });

          this.entitiesCreatingIpas = entitiesCreatingMap;

          this.commService.setEntitiesCreatingIpas(entitiesCreatingMap);
        });
    }

    this.entitiesCreatingIpasSubscription = this.commService.getEntitiesCreatingIpasBySubscription()
      .subscribe(entities => {
        this.entitiesCreatingIpas = entities;
      })

      this.router.routeReuseStrategy.shouldReuseRoute = () => false;
  }


  /**
   *
   *
   * @memberof DetailComponent
   */
  ngOnInit(): void {

    this.isLogged = this.commService.getLoginStatus();
    this.isNew = this.route.snapshot.params.newEntity && this.route.snapshot.params.newEntity === 'newEntity';

    this.commService.getLoginStatusBySubscription().subscribe(status => {
      this.isLogged = status;
      if (!this.isLogged) {
        this.editState = false;
      }
    }, error => {
      console.log(error);
    });


    this.commService.getModifyEnabledStatusBySubscription().subscribe(status => {
      this.isModifyEnabled = status;
    });

    this.isModifyEnabled = this.commService.getModifyEnabledStatus();

    this.route.params.subscribe(params => {
      if (!params || !params.domain || !params.subDomain) { return; }

      this.params = params;
      this.domain = this.params.domain;
      this.subDomain = this.params.subDomain;
      this.code = this.params.code;
      this.showForms = false;
      //this.commService.setFetchingDataStatus(true);
      this.editState = this.isNew;
      const version = this.isNew ? null : this.route.snapshot.params.version;

      this.commService.getBaseApiPathBySubscription() //aspetta il valore del base api path
        .pipe(
          debounceTime(100),
          distinctUntilChanged()) //serve a non ripetere il giro per valori uguali del base api path (senza, esplode)
        .subscribe(x => {
          const obj = {
            domain: this.domain,
            subDomain: this.subDomain,
            code: this.params.code,
            version: this.params.version,
            readOnly: false
          };

          if (obj.version) {
            this.http.getEntityByVersion(obj)
              .subscribe((entity: any) => {
                this.modificableEntity = entity;
              });
          }


          this.getEntity(this.isNew, true, version)
              .pipe(debounceTime(250),
                    distinctUntilChanged())
              .subscribe(
            data => {
              let stato = data.masterFields.components[1].components[0].columns[0].components[4].columns[0].components[0].defaultValue;
              stato = stato.split('#')[1]
              if (this.isLogged && !this.isNew && this.authService.getHasRoles()) { this.getOperations(); }

              if (this.isLogged && !this.authService.getHasRoles() && this.autoLoginService.hasDoneAutologinForFirstAlert()) { 
                this.alertsCenterService.showAlert({ message: 'Non hai ruoli per MyStandard, navigherai in modalità visualizzazione', type: 'warning', autoClosable: true })
              }

              if (this.isNew) { this.correctRelationshipsTabs(data); }
              else { this.correctRestoreColumn(data); }

              this.correctStateDefaultValue(data.masterFields.components);

              this.correctAttachmentsDownloadButton(data.attachments);

              // Necessario per conoscere il campo domain
              if (data.masterFields && data.masterFields.components) {
                this.mapMasterFieldsKeys(data.masterFields.components, this.keysMap.fields);
              }

              if (data.attachments && data.attachments.components) {
                this.mapAttachmentsKeys(data.attachments.components, this.keysMap.attachments);
              }

              if (
                data.attachments &&
                data.attachments.components
              ) {
                this.keysMap.initialAttachmentsByKey = this.createExistingAttachmentsMapByKeyForReadOnly(data.attachments.components);
              }

              if (data.tabsFields && data.tabsFields.components) {
                this.mapFunctionalProperties(data.tabsFields.components, this.keysMap.tabs, this.keysMap.relations);
              }

              this.extractSummaryFromMasterFields(data.masterFields, stato);

              this.correctMasterfieldsColumns(data.masterFields.components);
              this.masterFields = data.masterFields;
              this.tabsFields = data.tabsFields;
              this.attachments = data.attachments;
              let tableAttachments = _.filter(data.allElements, el => {
                return el && el.components && el.components[0]
                  && el.components[0].key === 'mystd_allegati' && el.components[0].type === "dataTable";
              });
              tableAttachments = tableAttachments.length > 0 ? tableAttachments[0] : undefined;

              if (this.isNew && tableAttachments.components) {
                tableAttachments.components[0].readOnly = false;
                tableAttachments.components.splice(0, 0,
                  {
                    "type": "button",
                    "key": "newAttachmentButton",
                    "label": "CARICA ALLEGATI",
                    "dataSrc": "values",
                    "action": "callback",
                    "icon": "file_upload",
                    "input": true,
                    "data": {},
                  });
                this.tableAttachments = tableAttachments;
              }

              this.tableAttachments = tableAttachments;

              if (!this.attachments || (this.attachments.components && this.attachments.components.length <= 0)) {
                this.hasAttachments_old = false;
              }

              this.showForms = true;
              this.showAccordion = true;
              // this.commService.setFetchingDataStatus(false);
              if (!this.isNew) {
                this.summary_fields = [];
                let regex = new RegExp(/\d/);
                if (this.summary[0] && this.summary[0].defaultValue !== null) {
                  let stato = { label: "Stato", value: this.summary[0] };
                  this.summary_fields.push(stato)
                }
                let index = this.summary[1].defaultValue.match(regex) ? this.summary[1].defaultValue.match(regex).index : this.summary[1].defaultValue.length
                if (this.summary[2]) {
                  let ente = { label: "Ente", value: this.summary[2].defaultValue };
                  this.summary_fields.push(ente)
                } else this.summary_fields.push({ label: "", value: "" })
                if (this.summary[1]) {
                  let nameDate = { label: "Ultima modifica", value: this.summary[1].defaultValue.slice(0, index - 1), data: this.summary[1].defaultValue.slice(index).replace(' ', ' - ') }
                  this.summary_fields.push(nameDate)
                }
                if (this.summary[3]) {
                  let spec = { label: "Specializzazione", value: this.summary[3].defaultValue };
                  this.summary_fields.push(spec)
                }
              }
            },
            (_error: any) => {
              this.dispatchErrors(_error);
            }

          );
        });
    });

    // })

  }

  public extractSummaryFromMasterFields(fields: any, stato = null) {
    for (let i = 0; i < fields.components.length; i++) {
      if (fields.components[i].key === "Riepilogo") {
        this.summary = JSON.parse(JSON.stringify(fields.components[i])).components[0].columns[0].components;
      }
    }
    this.summary.splice(0, 0, stato);
    fields.components = _.filter(fields.components, comp => {
      return comp.key !== 'Riepilogo';
    });
  }

  public updateDialogModifiedEntities(entities: any): void {
    this.dialogModifiedEntities = entities.dynamicDialogModifiedEntities;
  }

  public updateValidationState($event: any): void {
    this.formsValidationStatus[$event.child] = $event.value === 'VALID' ? true : false;
    this.isFormValid = this.formsValidationStatus.fields && this.formsValidationStatus.attachments ? true : false;
  }

  public createRelationshipTab(key: string, range: string, label: string, functionalProperty: string, hasNewButton: boolean): any {
    const component = {
      "key": "tab_relation_" + key,
      "label": label,
      "range": range,
      "funtionalProperty": functionalProperty,
    };

    const components = [];
    if (hasNewButton) {
      components.push({
        "key": "newRelationshipButton_" + key,
        "label": "Nuova Relazione",
        "type": "button",
        "dataSrc": "values",
      });
    }
    components.push(
      {
        "key": "datatable_" + key,
        "type": "columns",
        "dataSrc": "values",
        "data": {
          "columns": [
            {
              "label": "Tipo",
              "value": "TipoEntita"
            },
            {
              "label": "Versione",
              "value": "Versione"
            },
            {
              "label": "Codice",
              "value": "CodiceEntita"
            },
            {
              "label": "Nome",
              "value": "name"
            },
            {
              "label": "Stato",
              "value": "Stato"
            },
            {
              "label": "Elimina associazione",
              "value": "delete",
              "buttons": [
                {
                  "label": "Elimina associazione",
                  "icon": "delete",
                  "action": "delete",
                  "color": "primary",
                  "style": "icon"
                }
              ]
            }
          ],
          "values": [],
          "pagination": {
            "sizeOptions": [
              10,
              20,
              50
            ]
          }
        },
      }
    );

    component['components'] = components;

    return component;

  }

  private correctMasterfieldsColumns(components: any): void {

    const createColumns = (key: string, columnsNumber: number, elements: any[], hasDoubleCompByColumn: boolean) => {
      const finalBaseColumnsComps = [];
      for (let i = 0; i < elements.length; i = i + columnsNumber) {
        let j = i;
        const newCol = { key: `${key}_col${j}`, type: "columns", columns: [] }
        do {
          const components = hasDoubleCompByColumn ? elements[j] : [elements[j]];
          newCol.columns.push({ components });
          j++;
        } while (j < i + columnsNumber && elements[j]);

        finalBaseColumnsComps.push(newCol);
      }
      return JSON.parse(JSON.stringify(finalBaseColumnsComps));
    };

    const blocksToColumn = ['Dati di Base', 'Applicazione', 'API', 'Riepilogo']

    for (let i = 0; i < components.length; i++) {
      let component = components[i];
      if (!component) { return; }

      if (blocksToColumn.includes(component.key)) {
        const t = createColumns(component.key, 2, component.components[0].columns[0].components, false);
        components[i].components = t;

      } else if (component.key === 'Organizzazione') {
        const len = component.components[0].columns[0].components.length;
        let clonedComponentsToColumn = JSON.parse(JSON.stringify(component.components[0].columns[0].components)).splice(5, len - 1);
        const subColumns = [];

        for (let j = 0; j < clonedComponentsToColumn.length; j = j + 2) {
          let subCol = [];
          subCol.push(clonedComponentsToColumn[j].columns[0].components[0]);
          if (clonedComponentsToColumn[j + 1]) {
            subCol.push(clonedComponentsToColumn[j + 1].columns[0].components[0]);
          }
          subColumns.push(subCol);
        }

        const firstPart = JSON.parse(JSON.stringify(component.components[0].columns[0].components)).splice(0, 5);
        components[i].components = firstPart.concat(createColumns('organizzazione', 2, subColumns, true));

      }
    };

  }

  public ngOnDestroy(): void {
    this.entityStateSubscription.unsubscribe();
    this.entityNameSubscription.unsubscribe();
    if (this.entitiesCreatingIpasSubscription) { this.entitiesCreatingIpasSubscription.unsubscribe(); }

  }


  public getOperations(): void {
    if (this.isNew || !this.domain || !this.subDomain || !this.code || !this.version) { return; }
    _.forEach(Object.keys(this.operations), key => {
      this.operations[key] = false;
    });
    this.http.getOperations(
      this.domain,
      this.subDomain,
      this.code,
      this.version,
    ).subscribe(result => {
      this.operationsList = [];
      _.forEach(result, op => {
        this.operations[op.id] = true;
        this.operationsList.push(op);
      });
      this.showOperationsInSelect = this.operationsList.length > 2;
    }, error => {
      this.dispatchErrors(error);
    });;
  }

  private correctRestoreColumn(data: any): void {
    if (
      data.tabsFields &&
      data.tabsFields.components &&
      data.tabsFields.components[0] &&
      data.tabsFields.components[0].components &&
      data.tabsFields.components[0].components[0] &&
      data.tabsFields.components[0].components[0].components &&
      data.tabsFields.components[0].components[0].components[0] &&
      data.tabsFields.components[0].components[0].components[0].data &&
      data.tabsFields.components[0].components[0].components[0].data.columns
    ) {
      let columns = data.tabsFields.components[0].components[0].components[0].data.columns;
      const noRestoredColumns = _.filter(columns, col => col.value !== 'restore');
      data.tabsFields.components[0].components[0].components[0].data.columns = noRestoredColumns;
    }
  }

  private correctAttachmentsDownloadButton(attachments: any): void {
    _.forEach(attachments.components, component => {
      delete component.readOnly;
      delete component.hidden;
      if (!component.columns) { return; }
      const cols = component.columns;
      const lastCol = cols[cols.length - 1];
      delete lastCol.readOnly;
      delete lastCol.hidden;
      if (
        !cols[cols.length - 1].components ||
        !cols[cols.length - 1].components[0]
      ) { return; }
      const button = cols[cols.length - 1].components[0];
      delete button.readOnly;
      delete button.hidden;
    });
  }

  private correctStateDefaultValue(components: any[]): void {
    _.forEach(components, comp => {
      if (comp.type === 'select') {
        if (comp.defaultValue) {
          let finalValue = null;
          _.forEach(comp.data.values, val => {
            if (val.label === comp.defaultValue) { finalValue = val.value; }
          });
          if (!!finalValue) { comp.defaultValue = finalValue; }
        }
      } else if (!!comp.columns) {
        this.correctStateDefaultValue(comp.columns);
      } else if (!!comp.components) {
        this.correctStateDefaultValue(comp.components);
      }
    });
  }

  private correctRelationshipsTabs(data: any): void {
    if (
      data.tabsFields &&
      data.tabsFields.components &&
      data.tabsFields.components[0] &&
      data.tabsFields.components[0].components &&
      data.tabsFields.components[0].components[1] &&
      data.tabsFields.components[0].components[1].components &&
      data.tabsFields.components[0].components[1].components[0] &&
      data.tabsFields.components[0].components[1].components[0].components
    ) {

      const tabs_2_B__components = data.tabsFields.components[0].components[1].components[0].components;

      let newRelationships = [];

      _.forEach(tabs_2_B__components, relationship => {
        const key = relationship.key;
        const range = relationship.range;
        const label = relationship.label;
        const functionalProperty = relationship.funtionalProperty;
        const hasNewButton =
          relationship &&
          relationship.components &&
          relationship.components[0] &&
          relationship.components[0].components &&
          relationship.components[0].components.length > 1;
        const newRelationship = this.createRelationshipTab(key, range, label, functionalProperty, hasNewButton);
        newRelationships.push(newRelationship);
      });

      data.tabsFields.components[0].components[1].components[0].components = newRelationships;

    }
  }

  private correctEditableVersionRelationshipsTabs(data: any): void {
    if (
      data.tabsFields &&
      data.tabsFields.components &&
      data.tabsFields.components[0] &&
      data.tabsFields.components[0].components &&
      data.tabsFields.components[0].components[1] &&
      data.tabsFields.components[0].components[1].components &&
      data.tabsFields.components[0].components[1].components[0] &&
      data.tabsFields.components[0].components[1].components[0].components
    ) {

      const tabs_2_B__components = data.tabsFields.components[0].components[1].components[0].components;

      _.forEach(tabs_2_B__components, relationship => {

        const hasNewButton =
          relationship &&
          relationship.components &&
          relationship.components.length > 1;
        if (hasNewButton) {
          delete relationship.components[1].readOnly;
          delete relationship.components[1].hidden;
        } else {
          if (
            relationship.components[0] &&
            relationship.components[0].data &&
            relationship.components[0].data.columns
          ) {
            const columns = relationship.components[0].data.columns;
            let idx = 0;
            let counter = 0;
            _.forEach(columns, column => {
              if (column.value && column.value.toLowerCase() === 'delete') { idx = counter; }
              counter++;
            });
            columns.splice(idx, 1);
          }
        }
      });

    }
  }

  public updateRelationshipsTable(table: any, row: any): void {
    alert(table);
  }

  public resetMap(): {} {
    return {
      fields: {
        targetIndividualIRIsByDomain: {},
        customDomainsKeys: {},
        customRangeKeys: {},
        keys: {},
        customKeysSet: new Set(),
        customSubItems: {},
        originalKeysMap: {},
        selectBoxes: {
          keys: {},
          originalKeys: {},
        },
      },
      attachments: {
        customRangeKey: null,
        entityPropertyIri: null,
        originalKeysMap: {},
      },
      initialAttachments: {},
      initialAttachmentsByKey: {},
      relations: {},
      tabs: {},
    };
  }

  /**
   *
   *
   * @private
   * @param {*} data
   * @returns
   * @memberof DetailComponent
   */
  private jsonToFormData(data) {
    const formData = new FormData();

    this.buildFormData(formData, data);

    return formData;
  }

  /**
   *
   * @private
   * @param {*} formData
   * @param {*} data
   * @param {*} [parentKey]
   * @memberof DetailComponent
   */
  private buildFormData(formData, data, parentKey?) {
    if (
      data &&
      Array.isArray(data) &&
      !(data instanceof Date) &&
      !(data instanceof File)
    ) {
      Object.keys(data).forEach((key) => {
        this.buildFormData(
          formData,
          data[key],
          parentKey ? `${parentKey}[${key}]` : key
        );
      });
    } else if (
      data &&
      typeof data === "object" &&
      !(data instanceof Date) &&
      !(data instanceof File)
    ) {
      Object.keys(data).forEach((key) => {

        let parentKeyValue = null;
        if (!!parentKey) {
          parentKeyValue = parentKey.includes('dataProperty') ? `${parentKey}[${key}]` : `${parentKey}.${key}`;
        }

        this.buildFormData(
          formData,
          data[key],
          parentKey ? parentKeyValue : key,
        );
      });
    } else {
      const value = data;
      if (value !== null) { formData.append(parentKey, value); }
    }
  }

  private isJson(value: string): boolean {
    try {
      JSON.parse(value);
    } catch (e) {
      return false;
    }
    return true;
  }

  /**
   *
   *
   * @param {*} fields
   * @param {*} entity
   * @memberof DetailComponent
   */
  public setProperties(fields: any, entity: any): void {
    const fieldsKeys = Object.keys(fields);
    const customDomainsKeys = this.keysMap.fields.customDomainsKeys;
    const entityProperties = [];
    const auxEntityPropertyMap = {};

    const isCustomRepeatableItem = (dataPropertyKey: string): boolean => {
      return this.keysMap.fields.customRangeKeys[dataPropertyKey] !== undefined;
    }

    const getCustomOperation = (dataPropertyKey: string, defaultValue: string, finalValue: string): OperationType => {

      let customParentKey = _.filter(Object.keys(this.keysMap.fields.customSubItems), k => {
        let hasKey = false;
        _.forEach(Object.keys(this.keysMap.fields.customSubItems[k]), subKey => {
          if (subKey === dataPropertyKey) { hasKey = true; }
        });
        return hasKey;
      });
      if (
        !!customParentKey &&
        customParentKey.length === 1
      ) { customParentKey = customParentKey[0]; }

      const customParent = this.keysMap.fields.customSubItems[customParentKey];

      const siblings = _.filter(Object.keys(customParent), k => k !== dataPropertyKey);

      const haveSiblingsDefaultValue = _.filter(siblings, sib => {
        let siblingDefaultValue;
        if (this.isRestoredVersion) {
          siblingDefaultValue = this.originalVersionKeysMap.fields.keys[sib] && this.originalVersionKeysMap.fields.keys[sib].defaultValue ?
            this.originalVersionKeysMap.fields.keys[sib].defaultValue : undefined;
        } else {
          siblingDefaultValue = this.keysMap.fields.keys[sib] && this.keysMap.fields.keys[sib].defaultValue ?
            this.keysMap.fields.keys[sib].defaultValue : undefined;
        }
        return siblingDefaultValue === undefined;
      }).length === 0;

      if (this.isRestoredVersion) {
        if (defaultValue === undefined) {
          if (
            finalValue !== null &&
            finalValue !== undefined &&
            finalValue !== '') { return OperationType.ADD; }
          else { return OperationType.NOTHING; }
        } else if (
          finalValue === null ||
          finalValue === '' ||
          finalValue === undefined) {
          return dataPropertyKey === "mystd_dominio_di_business" ? OperationType.MODIFY : OperationType.REMOVE;
        }
        else {
          return defaultValue === finalValue ? OperationType.NOTHING : OperationType.MODIFY;
        }
      }

      if (!haveSiblingsDefaultValue || siblings.length === 0) {
        if (defaultValue === undefined && finalValue !== undefined && finalValue !== '') { return OperationType.ADD; }
        if (defaultValue !== undefined && (finalValue === undefined || finalValue === '')) {
          if (dataPropertyKey === "mystd_dominio_di_business" && !this.isSpecializedVersion) {
            return OperationType.MODIFY;
          } if (dataPropertyKey === "mystd_dominio_di_business" && this.isSpecializedVersion) {
            return OperationType.ADD;
          } else {
            return OperationType.REMOVE;
          }
        }
      }
      return this.isNew || this.isNewVersion || this.isSpecializedVersion ? OperationType.ADD : OperationType.MODIFY;

    };

    const getOperation = (dataPropertyKey: string, dataProperty: string): OperationType => {
      const isCustom = isCustomRepeatableItem(dataPropertyKey);
      const defaultValue = this.isRestoredVersion ?
        this.originalVersionKeysMap.fields.keys[dataPropertyKey].defaultValue :
        this.keysMap.fields.keys[dataPropertyKey].defaultValue;
      const finalValue = dataProperty[dataPropertyKey];

      if (isCustom) {
        return getCustomOperation(dataPropertyKey, defaultValue, finalValue);
      } else {

        if (defaultValue === undefined) {
          if (finalValue !== '' || finalValue !== null || finalValue !== undefined || this.isNew || this.isNewVersion) {
            return OperationType.ADD;
          }
        } else {
          if (finalValue !== '' && finalValue !== null) {
            return this.isNew || this.isNewVersion ? OperationType.ADD : OperationType.MODIFY;
          }
          return dataPropertyKey === "mystd_dominio_di_business" ? OperationType.MODIFY : OperationType.REMOVE;
        }

      }
    };

    // ADD ONLY CUSTOM REPEATABLE entityProperties
    _.forEach(fieldsKeys, key => {
      const domain = this.keysMap.fields.keys[key] ? this.keysMap.fields.keys[key].domain : null;
      if (
        !!domain &&
        !!customDomainsKeys[domain] &&
        domain === "Dominio" &&
        (fields[key] === undefined || fields[key] === null)
      ) {
        if (!auxEntityPropertyMap[domain]) {
          auxEntityPropertyMap[domain] = {};
        }
        auxEntityPropertyMap[domain][key] = '';
      } else if (
        !!domain &&
        !!customDomainsKeys[domain] &&
        fields[key] !== undefined &&
        fields[key] !== null
      ) {
        if (!auxEntityPropertyMap[domain]) {
          auxEntityPropertyMap[domain] = {};
        }
        auxEntityPropertyMap[domain][key] = fields[key]; // nuova dataProperty per quella entityProperty
      } else if (
        domain !== 'Stato' &&
        domain !== 'Status' &&
        domain !== 'Dominio'
      ) {
        entity.dataProperty[this.keysMap.fields.originalKeysMap[key]] = fields[key];
      }
    });

    _.forEach(Object.keys(auxEntityPropertyMap), domain => {
      const dataPropertyKey = Object.keys(auxEntityPropertyMap[domain])[0];

      const dataProperty = auxEntityPropertyMap[domain];
      const originalDataProperty = {};

      _.forEach(Object.keys(dataProperty), key => {
        const originalKey = this.isRestoredVersion ?
          this.originalVersionKeysMap.fields.originalKeysMap[key] :
          this.keysMap.fields.originalKeysMap[key];
        const isSelectBox = Object.keys(this.keysMap.fields.selectBoxes.keys).includes(key);
        if (isSelectBox) {
          const isJsonValid = this.isJson(dataProperty[key]);
          const parsedValue = isJsonValid ? JSON.parse(dataProperty[key]) : '';
          originalDataProperty[originalKey] = parsedValue !== '' ? parsedValue.join(',') : 'https://mystandard.regione.veneto.it/onto/BPO#Generale';
        } else {
          originalDataProperty[originalKey] = dataProperty[key];
        }
      });

      let _operation = getOperation(dataPropertyKey, dataProperty);
      const _entityPropertyIRI = customDomainsKeys[domain];
      const _originalKeyEntityPropertyIRI = this.isRestoredVersion ?
        this.originalVersionKeysMap.fields.originalKeysMap[_entityPropertyIRI] :
        this.keysMap.fields.originalKeysMap[_entityPropertyIRI];
      const _entityRangeIRI = this.isRestoredVersion ?
        this.originalVersionKeysMap.fields.customRangeKeys[dataPropertyKey] :
        this.keysMap.fields.customRangeKeys[dataPropertyKey];
      const targetIndividualIRIsByDomainMap = this.isRestoredVersion ?
        this.originalVersionKeysMap.fields.targetIndividualIRIsByDomain :
        this.keysMap.fields.targetIndividualIRIsByDomain;
      const _targetIndividualIRI = _operation === OperationType.ADD ? null : targetIndividualIRIsByDomainMap[domain];

      if (this.isRestoredVersion && _originalKeyEntityPropertyIRI === 'https://mystandard.regione.veneto.it/onto/BPO#stato') {
        _operation = OperationType.NOTHING;
      }

      if (_operation !== OperationType.NOTHING) {
        entityProperties.push({
          dataProperty: originalDataProperty,
          _operation,
          _entityPropertyIRI: _originalKeyEntityPropertyIRI,
          _entityRangeIRI,
          _targetIndividualIRI
        });
      }
    });

    if (this.isNew) {

      const _entityPropertyIRI = 'mystd_stato';
      const _originalKeyEntityPropertyIRI = this.keysMap.fields.originalKeysMap[_entityPropertyIRI];
      const inseritoState = 'https://mystandard.regione.veneto.it/onto/BPO#Inserito';
      const _entityRangeIRI = this.keysMap.fields.customRangeKeys[_entityPropertyIRI];

      entityProperties.push({
        dataProperty: {
          [_originalKeyEntityPropertyIRI]: inseritoState,
        },
        _operation: OperationType.ADD,
        _entityPropertyIRI: _originalKeyEntityPropertyIRI,
        _entityRangeIRI,
        _targetIndividualIRI: null,
      });
    }

    entity.entityProperty = entityProperties;
  }

  /**
   *
   *
   * @param {*} control
   * @returns {object}
   * @memberof DetailComponent
   */
  public getValuesFromControl(control: any): object {

    const idKeys = {
      codiceEntita: 'mystd_CodiceEntita',
      versione: 'mystd_Versione',
      idEntita: 'mystd_IdEntita',
    };

    const values: any = {};

    _.forEach(Object.keys(control), k => {

      if (
        k === 'custom_ind_name'
      ) { return; }

      if (
        !control[k].pristine
        || k === idKeys.versione
        || k === idKeys.codiceEntita
        || (this.isNewVersion && control[k].value !== null)
        || (this.isSpecializedVersion && control[k].value !== null)
        || this.isRestoredVersion
        ||
          (
            Object.keys(this.keysMap.fields.selectBoxes.keys).includes(k) &&
            control[k].touched
          )
      ) { values[k] = control[k].value; }

    });

    if (this.isNew) { values[idKeys.versione] = '1'; }
    else if (!this.isNewVersion) { values[idKeys.idEntita] = control[idKeys.codiceEntita].value + '_' + control[idKeys.versione].value; }

    _.forEach(Object.keys(this.dialogModifiedEntities), key => {
      values[key] = this.dialogModifiedEntities[key];
    });

    return values;

  }

  /**
   *
   *
   * @private
   * @returns {*}
   * @memberof DetailComponent
   */
  private getMasterFieldsInputData(childFieldsRef, entity): void {
    const fields = this.getValuesFromControl(childFieldsRef.form.controls);
    this.setProperties(fields, entity);
  }

  /**
   *
   *
   * @private
   * @param {*} childAttachments
   * @returns {*}
   * @memberof DetailComponent
   */
  private createExistingAttachmentsMap(attachments): any {

    const attachmentsMap = {};
    _.forEach(Object.keys(attachments), k => {
      const idx = k.substring(k.lastIndexOf(':') + 1, k.length);
      const key = k.substring(0, k.lastIndexOf(':'));
      if (!attachmentsMap[idx]) {
        attachmentsMap[idx] = {};
      }
      attachmentsMap[idx][key] = attachments[k];
    });

    let nullAttachment = null;
    _.forEach(Object.keys(attachmentsMap), att => {
      let isNull = true;
      _.forEach(Object.keys(attachmentsMap[att]), prop => {
        if (!!attachmentsMap[att][prop]) { isNull = false; }
      })
      if (isNull) { nullAttachment = att; }
    });

    delete attachmentsMap[nullAttachment];

    return attachmentsMap;
  };

  private createExistingAttachmentsMapByKey(attachments): any {

    const initialAttachmentsByKey = {};
    let counter = 0;
    _.forEach(attachments, att => {
      initialAttachmentsByKey[counter++] = att;
    });

    return initialAttachmentsByKey;
  }

  private createExistingAttachmentsMapByKeyForReadOnly(attachments): any {
    const initialAttachmentsByKey = {};
    _.forEach(attachments, att => {
      const values = {};
      const key = att.originalKey.replace('allegati_', '');
      _.forEach(att.columns, col => {
        const comp = col.components[0] ? col.components[0] : null;
        if (comp.label !== undefined) { values[comp.label] = comp.defaultValue; }
      });
      initialAttachmentsByKey[key] = values;
    });

    return initialAttachmentsByKey;
  }

  private getTableAttachmentsInputData(responseEntity: any): any {

    const finalAtts = this.tableAttachments.components[1].data.values;
    const idKey = this.attachmentPropsMap ? this.attachmentPropsMap.id : null;
    const defaulValueForId = "https://mystandard.regione.veneto.it/onto/BPO#id_documento";

    const isUploadedAttachment = (finalAtt) => finalAtt[idKey] === undefined;

    // UPLOADED ATTACHMENTS
    _.forEach(this.uploadedAttachments, att => {

      const dataProperty: any = {};
      const finalAtt = _.filter(finalAtts, tableAtt => tableAtt.feId === att.uui)[0];
      _.forEach(Object.keys(finalAtt), prop => {
        if (prop !== 'feId') dataProperty[prop] = finalAtt[prop];
      });
      dataProperty.idFile = att.file.idFile;

      const newEntity: any = {};
      newEntity.dataProperty = dataProperty;
      newEntity._operation = OperationType.ADD;
      newEntity._entityPropertyIRI = this.keysMap.attachments.entityPropertyIri;
      newEntity._entityRangeIRI = this.keysMap.attachments.customRangeKey;
      newEntity._targetIndividualIRI = null;

      responseEntity.entityProperty.push(newEntity);
    });

    if (!this.isRestoredVersion && !this.isNewVersion && !this.isSpecializedVersion) {

      // REMOVED ATTACHMENTS
      _.forEach(this.originalAttachments, originalAtt => {
        let found = false;
        _.forEach(finalAtts, finalAtt => {
          if (originalAtt[idKey] === finalAtt[idKey]) {
            found = true;
          }
        });
        if (!found) {
          const newEntity: any = {};
          if(idKey)
            newEntity.dataProperty = { [idKey]: originalAtt[idKey] };
          else
           newEntity.dataProperty = { [defaulValueForId]: originalAtt[defaulValueForId] };
           
          newEntity._operation = OperationType.REMOVE;
          newEntity._entityPropertyIRI = originalAtt._entityPropertyIRI;
          newEntity._entityRangeIRI = this.tableAttachments.components[1].range;
          newEntity._targetIndividualIRI = originalAtt._targetIndividualIRI;
          responseEntity.entityProperty.push(newEntity);
        }
      });

      // MODIFIED ATTACHMENTS
      if(this.attachmentPropsMap) {
        const name = this.attachmentPropsMap.name;
      const description = this.attachmentPropsMap.description;
      _.forEach(finalAtts, finalAtt => {
        let hasChanged = false;
        let originalAttRef;
        _.forEach(this.originalAttachments, originalAtt => {
          if (
            finalAtt[idKey] !== undefined // is not uploaded attachment
            && finalAtt[idKey] === originalAtt[idKey]
            && (
              finalAtt[name] !== originalAtt[name]
              || finalAtt[description] !== originalAtt[description]
            )
          ) {
            hasChanged = true;
            originalAttRef = originalAtt;
          }
        });
        if (hasChanged) {
          const newEntity: any = {};
          newEntity.dataProperty = { [idKey]: finalAtt[idKey] };

          if (finalAtt[name] !== originalAttRef[name]) {
            newEntity.dataProperty[name] = finalAtt[name];
          }
          if (finalAtt[description] !== originalAttRef[description]) {
            newEntity.dataProperty[description] = finalAtt[description];
          }

          newEntity._operation = OperationType.MODIFY;
          newEntity._entityPropertyIRI = finalAtt._entityPropertyIRI;
          newEntity._entityRangeIRI = finalAtts.range;
          newEntity._targetIndividualIRI = finalAtt._targetIndividualIRI;
          responseEntity.entityProperty.push(newEntity);
        }
      });
      }
      // newVersion & restoredVersion
    } else {

      let attachmentsMandatoryProps;
      
      if (this.attachmentPropsMap) {
        attachmentsMandatoryProps = [
         this.attachmentPropsMap.name,
         this.attachmentPropsMap.type,
         this.attachmentPropsMap.lastModifiedDate,
         this.attachmentPropsMap.description,
         this.attachmentPropsMap.id,
       ];
      }

      const attachmentsMandatoryProps_fix = ["custom_ind_name",
                                             "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                                             "https://mystandard.regione.veneto.it/onto/BPO#id_documento",
                                             "https://mystandard.regione.veneto.it/onto/BPO#nome_allegato",
                                             "https://mystandard.regione.veneto.it/onto/BPO#tipoFile",
                                             "https://w3id.org/italia/onto/TI/date"];

      const idDocumentoKey = 'https://mystandard.regione.veneto.it/onto/BPO#id_documento';

      // ADD PRIOR VERSION ATTACHMENTS
      _.forEach(finalAtts, finalAtt => {

        if (isUploadedAttachment(finalAtt) && finalAtt[idDocumentoKey] === undefined) {
          return
        }
        const newEntity: any = {};

        newEntity.dataProperty = {};
        _.forEach(attachmentsMandatoryProps, prop => {
          if (finalAtt[prop] !== undefined && finalAtt[prop] !== '') {
            newEntity.dataProperty[prop] = finalAtt[prop];
          }
        });
        //FIX OLD ATTACHMENTS BEING IGNORED IN NEW VERSIONS
        if(Object.keys(newEntity.dataProperty).length==0) {
          _.forEach(attachmentsMandatoryProps_fix, prop => {
            if (finalAtt[prop] !== undefined && finalAtt[prop] !== '') {
              newEntity.dataProperty[prop] = finalAtt[prop];
            }
          });
        }

        const code = this.route.snapshot.params.code;
        const version = this.keysMap.fields.keys.mystd_Versione.defaultValue;

        newEntity.dataProperty.idEntitaOrigine = code + '_' + version;
        newEntity._operation = OperationType.ADD;
        newEntity._entityPropertyIRI = this.keysMap.attachments.entityPropertyIri;
        newEntity._entityRangeIRI = this.keysMap.attachments.customRangeKey;
        newEntity._targetIndividualIRI = null;

        responseEntity.entityProperty.push(newEntity);
      });

      // REMOVE EXISTING ATTACHMENTS FROM LAST VERSION
      _.forEach(this.priorRestoredVersionAttachments, priorAtt => {
        const newEntity: any = {};
        newEntity.dataProperty = { [idKey]: priorAtt[idKey] };
        newEntity._operation = OperationType.REMOVE;
        newEntity._entityPropertyIRI = priorAtt._entityPropertyIRI;
        newEntity._entityRangeIRI = this.tableAttachments.components[1].range;
        newEntity._targetIndividualIRI = priorAtt._targetIndividualIRI;
        responseEntity.entityProperty.push(newEntity);
      });

    }
  }

  /**
   *
   *
   * @private
   * @param {*} responseEntity
   * @memberof DetailComponent
   */
  private getRelationshipsInputData(responseEntity: any): any {

    _.forEach(Object.keys(this.modifiedRelationships), rel => {
      _.forEach(Object.keys(this.modifiedRelationships[rel]), operation => {
        _.forEach(this.modifiedRelationships[rel][operation], item => {
          const newEntity: any = {};
          newEntity._operation = operation === 'added' ? OperationType.ADD : OperationType.REMOVE;
          newEntity._functionalPropertyIRI = this.isRestoredVersion ? rel : this.keysMap.relations[rel];
          newEntity._targetIndividualsIRI = item._targetIndividualsIRI;
          responseEntity.functionalProperty.push(newEntity);
        });
      });
    });

    if (this.isSpecializedVersion) {
      const newEntity: any = {};
      newEntity._functionalPropertyIRI = "https://mystandard.regione.veneto.it/onto/BPO#Specializza";
      newEntity._operation = OperationType.ADD;
      newEntity._targetIndividualsIRI = `https://mystandard.regione.veneto.it/onto/BPO_data#${this.specializedVersionOrigin.code}_${this.specializedVersionOrigin.version}`;
      responseEntity.functionalProperty.push(newEntity);
    }

  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public postEntity(): void {

    this.cancel = false;

    const formData = this.createFormData();

    //this.commService.setFetchingDataStatus(true);

    this.http
      .postEntity(
        this.domain,
        this.subDomain,
        formData
      ).subscribe((data: any) => {
        this.editState = false;
        //this.commService.setFetchingDataStatus(false);
        const messages = {
          isNew: 'Nuova entità creata correttamente',
          isNewVersion: 'Nuova versione creata correttamente',
        }
        const message = this.isNew || this.isSpecializedVersion ? messages.isNew : messages.isNewVersion;
        this.isNewVersion = false;
        this.isNew = false;
        this.commService.setResultMessage(true, { message, type: 'success', autoClosable: true });
        this.router.navigate(['/summary']);
      },
        (_error: any) => {
          this.editState = true;
          this.dispatchErrors(_error);
        });
  }

  private createFormData(): any {

    const responseEntity = {
      entityIRI: this.commService.getIriFromSubDomain(this.subDomain),
      dataProperty: {},
      entityProperty: [],
      functionalProperty: [],
    };

    this.getMasterFieldsInputData(this.childFields.jsonFormRef, responseEntity);
    this.getTableAttachmentsInputData(responseEntity);
    this.getRelationshipsInputData(responseEntity);
    const formData = this.jsonToFormData(responseEntity);

    _.forEach(this.uploadedAttachments, attachment => {
      formData.append('allegati', attachment.file, attachment.file.idFile);
    });

    return formData;
  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public putEntity(): void {

    this.isNew = false;
    this.editState = false;
    this.cancel = false;

    const formData = this.createFormData();

    //this.commService.setFetchingDataStatus(true);

    this.http
      .putEntity(
        this.domain,
        this.subDomain,
        formData
      ).subscribe((data: any) => {
        this.editState = false;
        this.isNewVersion = false;
        this.isNew = false;
        this.commService.setResultMessage(true, { message: 'Versione aggiornata correttamente', type: 'success', autoClosable: true });
        this.router.navigate(['/summary']);
        
      },
        (_error: any) => {
          this.editState = true;
          this.dispatchErrors(_error);
        });
  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public onSaveButtonClick(): void {
    if (this.isNew || this.isNewVersion || this.isSpecializedVersion) {
      this.postEntity();
    } else {
      this.putEntity();
    }
  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public onModifyButtonClick(): void {
    const lastVersion = this.route.snapshot.params.version;
    const originVersion = lastVersion;
    this.getEditableEntity(false, originVersion, lastVersion, false, false);
  }

  public onSpecializeButtonClick(): void {
    this.isSpecializedVersion = true;
    const originVersion = this.route.snapshot.params.version;
    this.specializedVersionOrigin = {
      code: this.route.snapshot.params.code,
      version: originVersion,
    };

    this.getEditableEntity(false, originVersion, '1', false, true, true);
  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public onNewVersionButtonClick(): void {
    this.isNewVersion = true;
    const originVersion = this.route.snapshot.params.version;

    const code = this.params.code;

    //this.commService.setFetchingDataStatus(true);

    this.http.getMaxVersion(this.domain, this.subDomain, code).subscribe(data => {
      const lastVersion = data.Versione;
      //this.commService.setFetchingDataStatus(false);
      this.http.getHistorical(this.domain, this.subDomain, code, (parseInt(lastVersion) + 1).toString()).subscribe(result => {
        this.newVersionHistoricalVersions = result;
        this.getEditableEntity(true, originVersion, lastVersion, false, false, true);
      }, error => {
        const errorMessage = environment.showErrors ? error.error : '';
        this.alertsCenterService.showAlert({
          message: 'Si è verificato nel caricamento dei dati' + errorMessage, type: 'danger', autoClosable: true
        });
      });
    },
      error => {
        this.dispatchErrors(error);
      });
  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public onCancelButtonClick(): void {
    if (this.isNew) {
      // this.navigateToSubDomainList();
      this.router.navigate(['/summary']);
    } else {
      this.reloadOperationsSelect();
      this.getReadOnlyEntity();
    }
  }

  public navigateToSubDomainList(): void {
    const baseApiPath = this.commService.getBaseApiPath();
    this.router.navigate(['/', baseApiPath, this.domain, this.subDomain]);
  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public onDeleteButtonClick(): void {
    this.deleteEntity();
  }

  /**
   *
   *
   * @returns {*}
   * @memberof DetailComponent
   */
  public deleteEntity(): any {

    //this.commService.setFetchingDataStatus(true);

    this.http
      .deleteEntity({
        domain: this.domain,
        subDomain: this.subDomain,
        code: this.params.code,
        version: this.params.version,
      })
      .subscribe((data: any) => {

        this.showForms = true;
        this.showAccordion = true;

        this.editState = false;
        this.isNewVersion = false;
        this.isNew = false;
        this.commService.setResultMessage(true, { message: 'Entità eliminata correttamente', type: 'success', autoClosable: true });
        this.router.navigate(['/summary']);
      },
        (_error: any) => {
          this.dispatchErrors(_error);
        }
      );
  }

  private getIsNew(): boolean {
    if (!this.params.newEntity) { return false; }
    return this.params.newEntity === 'newEntity';
  }

  /**
   *
   *
   * @returns {*}
   * @memberof DetailComponent
   */
  public getEntity(isNew: boolean, readOnly: boolean, version: string, clone?: boolean): any {
    isNew = this.getIsNew();

    if (isNew) {
      return this.http.getNewEntity(this.domain, this.subDomain);
    }

    const obj = {
      domain: this.domain,
      subDomain: this.subDomain,
      code: this.params.code,
      version,
      readOnly,
    };
    return this.http.getEntityByVersion(obj, clone);
  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public getReadOnlyEntity(): void {
    this.editState = false;
    this.cancel = false;

    //this.commService.setFetchingDataStatus(true);
    this.showForms = false;
    const lastVersion = this.route.snapshot.params.version;
    this.getEntity(false, true, lastVersion)
      .subscribe((data: any) => {

        this.keysMap = this.resetMap();

        if (data.masterFields && data.masterFields.components) {
          this.mapMasterFieldsKeys(data.masterFields.components, this.keysMap.fields);
        }

        this.extractSummaryFromMasterFields(data.masterFields);

        this.correctMasterfieldsColumns(data.masterFields.components);

        this.masterFields = data.masterFields;
        this.attachments = data.attachments;
        this.tabsFields = data.tabsFields;

        if (!this.attachments || (this.attachments.components && this.attachments.components.length <= 0)) {
          this.hasAttachments_old = false;
        }

        this.showForms = true;
        this.showAccordion = true;
        //this.commService.setFetchingDataStatus(false);

      },
        (_error: any) => {
          this.dispatchErrors(_error);
        }
      );
  }

  /**
   * 
   *
   * @param {*} components
   * @memberof DetailComponent
   */
  public setReadOnlyFields(components: any): void {
    for (let i = 0; i < components.length; i++) {
      const comp = components[i];

      if (
        comp.key === 'mystd_CodiceEntita' ||
        comp.key === 'mystd_stato' ||
        comp.key === 'mystd_Versione'
      ) {
        comp.readOnly = true;
      } else if (!!comp.columns) {
        this.setReadOnlyFields(comp.columns);
      } else if (!!comp.components) {
        this.setReadOnlyFields(comp.components);
      }
    }
  }

  private clearFunctionalProperties(): void {
    _.forEach(Object.keys(this.keysMap.tabs), functionalPropertyIRI => {
      _.forEach(this.originalVersionKeysMap.tabs[functionalPropertyIRI], tab => {
        this.registerModifiedRelationship(functionalPropertyIRI, tab, 'deleted');
      });
      _.forEach(this.keysMap.tabs[functionalPropertyIRI], tab => {
        this.registerModifiedRelationship(functionalPropertyIRI, tab, 'added');
      });
    });
  }

  /**
   *
   *
   * @memberof DetailComponent
   */
  public getEditableEntity(isNewVersion: boolean, originVersion: string, lastVersion: string, isRestored: boolean, isSpecializedVersion: boolean, clone? : boolean): void {
    this.editState = true;
    this.cancel = false;

    this.originalVersionKeysMap = JSON.parse(JSON.stringify(this.keysMap));
    this.isRestoredVersion = isRestored;

    //this.commService.setFetchingDataStatus(true);
    this.showForms = false;

    let savedHistoricalValues = {}; // fix visualizzazione storico dopo aver cliccato su restore version

    if (this.isRestoredVersion) {
      savedHistoricalValues = this.tabsFields.components[0].components[0].components[0];  
    }

    this.getEntity(false, false, originVersion, clone)
      .subscribe((data: any) => {

        this.getOperations();

        this.setReadOnlyFields(data.masterFields.components);

        this.correctEditableVersionRelationshipsTabs(data);

        this.keysMap = this.resetMap();

        if (data.masterFields && data.masterFields.components) {
          this.mapMasterFieldsKeys(data.masterFields.components, this.keysMap.fields);
        }

        if (data.attachments && data.attachments.components) {
          this.keysMap.attachments = {};
          this.mapAttachmentsKeys(data.attachments.components, this.keysMap.attachments);
          if (
            data.attachments &&
            data.attachments.components &&
            data.attachments.components[0] &&
            data.attachments.components[0].data &&
            data.attachments.components[0].data.values
          ) {
            this.keysMap.initialAttachmentsByKey = this.createExistingAttachmentsMapByKey(data.attachments.components[0].data.values);
          }
        }

        if (data.tabsFields && data.tabsFields.components) {
          this.mapFunctionalProperties(data.tabsFields.components, this.keysMap.tabs, this.keysMap.relations);
          if (isNewVersion) {
            this.replaceHistoricalVersions(data.tabsFields.components);
          } else if (isSpecializedVersion) {
            this.deleteHistoricalVersions(data.tabsFields.components);
          }
        }

        if (this.isRestoredVersion) { this.clearFunctionalProperties(); }

        if (isNewVersion) {
          this.isNewVersion = true;
          this.setBaseParams(data.masterFields, lastVersion, false);
        } else if (isSpecializedVersion) {
          this.isNewVersion = false;
          this.setBaseParams(data.masterFields, 0, true);
        } else {
          this.isNewVersion = false;
          this.setBaseParams(data.masterFields, _.toNumber(lastVersion) - 1, false);
        }

        if (!data.attachments || (data.attachments.components && data.attachments.components.length <= 0)) {
          this.hasAttachments_old = false;
        } else { this.hasAttachments_old = true; }

        this.extractSummaryFromMasterFields(data.masterFields);

        this.correctMasterfieldsColumns(data.masterFields.components);

        if (this.isRestoredVersion) { //ripristino lo storico se sto facendo un restore versione
          data.tabsFields.components[0].components[0].components[0]= savedHistoricalValues;
        }

        this.masterFields = data.masterFields;
        this.tabsFields = data.tabsFields;


        let tableAttachments = data.attachments;

        if (!!tableAttachments.components) {
          tableAttachments.components[0].readOnly = false;
          tableAttachments.components.splice(0, 0,
            {
              "type": "button",
              "key": "newAttachmentButton",
              "label": "CARICA ALLEGATI",
              "dataSrc": "values",
              "action": "callback",
              "input": true,
              "data": {},
              "icon": "file_upload"
            });
        }

        this.originalAttachments = data.attachments.components[1].data.values;
        this.tableAttachments = tableAttachments;

        this.showForms = true;
        this.showAccordion = true;
        //this.commService.setFetchingDataStatus(false);

      },
        (_error: any) => {
          this.dispatchErrors(_error);
        }
      );
  }

  public replaceHistoricalVersions(components: any): void {
    if (
      !components ||
      !components[0] ||
      !components[0].components ||
      !components[0].components[0] ||
      !components[0].components[0].components ||
      !components[0].components[0].components[0]
    ) {
      return;
    }

    const historical = components[0].components[0].components[0];
    
    if (!historical.data) {
      historical.data = {
      columns: [
          { label: 'Versione', value: 'Versione' },
          { label: 'Codice', value: 'CodiceEntita' },
          { label: 'Nome', value: 'name' },
          { label: 'Stato', value: 'Stato' },
          { label: 'Azioni', value: 'restore', buttons: [   
            { label: 'Ripristina versione', icon: 'restore', action: 'restore', color: 'primary', style: 'icon'} ]}
        ],
      values: [],
      pagination: {
        sizeOptions: [ 10, 20, 50 ]
        }
      }
    }

    historical.data.values = this.newVersionHistoricalVersions;
  }

  public deleteHistoricalVersions(components: any): void {
    if (
      !components ||
      !components[0] ||
      !components[0].components ||
      !components[0].components[0] ||
      !components[0].components[0].components ||
      !components[0].components[0].components[0]
    ) {
      return;
    }

    const historical = components[0].components[0].components[0];
    historical.data = null;

  }

  public updateTableAttachmentStatus($event): void {
    this.showAttachments = false;

    if ($event.old) return;

    // update uploaded attachments
    if ($event.uploadedAttachment) {
      this.uploadedAttachments.push($event.uploadedAttachment);
    } else if ($event.deletedUploadedAttachment) {
      this.uploadedAttachments = _.filter(this.uploadedAttachments, att => {
        return att.uui !== $event.deletedUploadedAttachment.feId;
      });
    }

    setTimeout(() => {
      this.showAttachments = true;
      this.tableAttachments = $event.attachmentsState
    });

  }

  /**
   * 
   * @param json
   */
  private setBaseParams(json: any, lastVersion: any, isSpecializedVersion: boolean): void {
    let datiDiBase;
    const versionIRI = 'https://mystandard.regione.veneto.it/onto/BPO#Versione';
    const codeIRI = 'https://mystandard.regione.veneto.it/onto/BPO#CodiceEntita';
    const stateIRI = 'https://mystandard.regione.veneto.it/onto/BPO#stato';

    _.forEach(json.components, comp => {
      if (comp.key === 'Dati di Base') {
        datiDiBase = comp.components[0].columns[0].components;
      }
    });

    _.forEach(datiDiBase, d => {
      if (d.originalKey === versionIRI) {
        const newVersionNumber = _.toNumber(lastVersion) + 1;
        d.defaultValue = newVersionNumber.toString();
      } else if (d.originalKey === stateIRI) {
        if (
          !d.columns ||
          !d.columns[0] ||
          !d.columns[0].components ||
          !d.columns[0].components[0]
        ) { return; }
        d.columns[0].components[0].defaultValue = 'https://mystandard.regione.veneto.it/onto/BPO#Inserito';
      } else if (d.originalKey === codeIRI && isSpecializedVersion) {
        d.defaultValue = '';
        d.readOnly = false;
      }
    });
  }

  /**
   *
   *
   * @param {*} $event
   * @param {string} method
   * @returns {void}
   * @memberof DetailComponent
   */
  public updateRelationshipTab($event: any, method: string): void {
    const clearKey = (key: string) => {
      return key.replace('tab_relation_', '').replace('newRelationshipButton_', '')
    }

    const deleteRow = (values: any[], rowElement: any): void => {
      const relationshipKey = rowElement.relationshipKey;
      for (let i = 0; i < values.length; i++) {

        const value = values[i];

        if (
          !value ||
          !value.CodiceEntita ||
          !value.Versione ||
          !value.relationshipKey
        ) {
          console.log('[detail.component:updateRelationshipTab:deleteRow] error in morfeo json - row deletion');
          return;
        }

        if (
          value.CodiceEntita === rowElement.CodiceEntita &&
          value.Versione === rowElement.Versione &&
          value.relationshipKey === rowElement.relationshipKey
        ) {
          values.splice(i, 1);
          i--;
        }

      };
    };

    this.showTabs = false;

    const clearedRelationshipKey = clearKey($event.selectedRelationship);
    const entity = $event.selectedEntity;
    const tabsFieldsReference = this.tabsFields;  // cannot use JSON.parse(JSON.strigify) because object is a cyclic graph
    this.tabsFields = null;

    if (
      !tabsFieldsReference ||
      !tabsFieldsReference.components ||
      !tabsFieldsReference.components[0] ||
      !tabsFieldsReference.components[0].components ||
      !tabsFieldsReference.components[0].components[1] ||
      !tabsFieldsReference.components[0].components[1].components ||
      !tabsFieldsReference.components[0].components[1].components[0] ||
      !tabsFieldsReference.components[0].components[1].components[0].components
    ) {
      console.log("[detail.component:updateRelationshipTab] error in morfeo json - relationshipsTab")
      this.showTabs = true;
      return;
    }

    const existingTabs = tabsFieldsReference.components[0].components[1].components[0].components;

    let clearedKey = null;
    let tab = null;

    _.forEach(existingTabs, thisTab => {
      const thisClearedKey = clearKey(thisTab.key);
      if (clearedRelationshipKey === thisClearedKey) {
        clearedKey = thisClearedKey;
        tab = thisTab;
      }
    });

    // Sets null label values, clears readOnly & hidden props
    _.forEach(existingTabs, tab => {
      tab.label = !!tab.label ? tab.label : '';
      if (!tab.components) {
        return;
      };
      _.forEach(tab.components, comp => {
        comp.label = !!comp.label ? comp.label : '';
        delete comp.readOnly;
        delete comp.hidden;
      });
    });

    if (!!clearedKey && !!tab) {

      const dataTables = _.filter(tab.components, comp => {
        const keySuffix = comp.key.substring(0, 9);
        if (!keySuffix || keySuffix !== 'datatable') {
          return false;
        }
        return true;
      });

      const dataTable = dataTables.length === 1 ? dataTables[0] : undefined;

      if (
        !dataTable ||
        !dataTable.data
      ) {
        console.log("[detail.component:updateRelationshipTab] error in morfeo json - data.values")
        this.showTabs = true;
        return;
      }

      if (!dataTable.data.values) {
        dataTable.data.values = [];
      }

      setTimeout(() => {
        this.showTabs = true;
        if (method === 'push') {
          if (!!dataTable.parent.key.match(/(https:\/\/)(?<=).*/)) {
            entity.relationshipKey = dataTable.parent.key.match(/(https:\/\/)(?<=).*/)[0];
          } else {
            entity.relationshipKey = dataTable.parent.funtionalProperty;
          }
          dataTable.type = "dataTable";
          this.registerModifiedRelationship(clearedKey, entity, 'added');
          dataTable.data.values.push(entity);
        } else if (method === 'delete') {
          this.registerModifiedRelationship(clearedKey, entity, 'deleted');
          deleteRow(dataTable.data.values, entity);
        }
        this.tabsFields = tabsFieldsReference;
      }, 10);

    }
  }

  /**
   *
   *
   * @private
   * @param {string} key: functionalEntityIRI
   * @param {*} entity: targetEntityIRI
   * @param {string} operation
   * @memberof DetailComponent
   */
  private registerModifiedRelationship(key: string, entity: any, operation: string): any {

    const updateInverseOperationRelationships = (operation) => {
      const filtered = _.filter(this.modifiedRelationships[key][operation], (rel: any) => {
        if (
          rel.CodiceEntita === entity.CodiceEntita &&
          rel.Versione === entity.Versione
        ) {
          return false;
        }
        return true;
      });
      this.modifiedRelationships[key][operation] = filtered;
    };

    const hasInverseOperation = (inverseOperation) => {
      let hasInverse = false
      _.forEach(this.modifiedRelationships[key][inverseOperation], (rel: any) => {
        if (
          rel.CodiceEntita === entity.CodiceEntita &&
          rel.Versione === entity.Versione
        ) {
          hasInverse = true;
        }
      });
      return hasInverse;
    };

    this.modifiedRelationships[key] = !!this.modifiedRelationships[key] ? this.modifiedRelationships[key] : {};
    this.modifiedRelationships[key][operation] =
      !!this.modifiedRelationships[key][operation] ? this.modifiedRelationships[key][operation] : [];

    this.modifiedRelationships[key][operation].push(entity);

    const inverseOperation = operation === 'added' ? 'deleted' : 'added';

    if (hasInverseOperation(inverseOperation)) {
      updateInverseOperationRelationships('added');
      updateInverseOperationRelationships('deleted');
    }

  }

  /**
   *
   *
   * @param {*} components
   * @param {*} map
   * @returns {void}
   * @memberof DetailComponent
   */
  public mapFunctionalProperties(components: any, map: any, relationsMap: any): void {
    if (
      !components ||
      !components[0] ||
      !components[0].components ||
      !components[0].components[1] ||
      !components[0].components[1].components ||
      !components[0].components[1].components[0] ||
      !components[0].components[1].components[0].components
    ) {
      return;
    }

    const tabs = components[0].components[1].components[0].components;

    _.forEach(tabs, tab => {
      const key = tab.key.replace('tab_relation_', '');
      map[key] = tab.components[tab.components.length - 1].data.values;
      relationsMap[key] = tab.funtionalProperty;

      if (this.isNewVersion || this.isSpecializedVersion) {
        if (
          !tab.components ||
          !tab.components[1] ||
          !tab.components[1].data ||
          !tab.components[1].data.values
        ) { return; }
        const relationships = tab.components[1].data.values;
        _.forEach(relationships, rel => {
          this.registerModifiedRelationship(key, rel, 'added');
        });
      }
    });

  }

  /**
   *
   *
   * @param {*} components
   * @param {*} map
   * @returns {void}
   * @memberof DetailComponent
   */
  public mapAttachmentsKeys(components: any, map: any): void {
    if (!components) {
      return;
    }
    if (!this.keysMap.attachments.originalKeysMap) { this.keysMap.attachments.originalKeysMap = {}; }
    _.forEach(components, comp => {
      if (!map.customRangeKey) {
        map.customRangeKey = comp.range;
      }
      if (!map.entityPropertyIri) {
        // map.entityPropertyIri = comp.key;
        map.entityPropertyIri = comp.originalKey;
      }
      if (
        !comp.components ||
        !comp.components[0] ||
        !comp.components[0].columns ||
        comp.components[0].columns.length < 1
      ) { return; }
      _.forEach(comp.components[0].columns, col => {
        if (
          !col.components ||
          !col.components[0] ||
          !col.components[0].originalKey ||
          !col.components[0].key
        ) { return; }
        const originalKey = col.components[0].originalKey;
        const key = col.components[0].key;
        this.keysMap.attachments.originalKeysMap[key] = originalKey;
      });
    });
  }

  /**
   *
   *
   * @param {any[]} components
   * @param {*} map
   * @memberof DetailComponent
   */
  public mapMasterFieldsKeys(components: any[], map: any): void {
    this.mapSelectBoxes(components, map.selectBoxes);
    this.mapDataPropertyKeys(components, map.keys, map.customSubItems);
    this.mapCustomAndEntityPropertyKeys(map);
    this.deleteHiddenComponents(components);
  }

  private mapSelectBoxes(components: any[], selectBoxes: any): void {
    _.forEach(components, comp => {
      if (comp.type === 'selectboxes') {
        selectBoxes.keys[comp.key] = comp.defaultValue;
        selectBoxes.originalKeys[comp.originalKey] = comp.defaultValue;
      } else if (!!comp.columns) {
        this.mapSelectBoxes(comp.columns, selectBoxes);
      } else if (!!comp.components) {
        this.mapSelectBoxes(comp.components, selectBoxes);
      }
    });
  }

  /**
   *
   *
   * @param {*} comp
   * @memberof DetailComponent
   */
  public setRangeKey(comp: any): void {
    if (
      !!comp.domain &&
      !!comp.range
    ) {
      const columns = comp.columns;
      _.forEach(columns, c => {
        if (
          !!c.components &&
          !!c.components[0] &&
          !!c.components[0].key &&
          c.components[0].key !== 'custom_ind_name'
        ) {
          const key = c.components[0].key;
          this.keysMap.fields.customRangeKeys[key] = comp.range;
        }
      });
    }
  }

  private mapCustomRepeatableSubElements(comp: any, keys: any): void {
    const newMap = {};
    let hasCustomRepeatable = false;

    _.forEach(comp.columns, col => {
      if (
        !!col.components &&
        !!col.components[0] &&
        col.components[0].key === 'custom_ind_name'
      ) { hasCustomRepeatable = true; };
    });

    if (hasCustomRepeatable) {
      _.forEach(comp.columns, col => {
        if (
          !!col.components &&
          !!col.components[0] &&
          col.components[0].key !== 'custom_ind_name'
        ) { newMap[col.components[0].key] = col.components[0].defaultValue; }
      });
      keys[comp.key] = newMap;
    }
  }

  /**
   * 
   *
   * @param {any[]} components
   * @param {*} keys
   * @memberof DetailComponent
   */
  public mapDataPropertyKeys(components: any[], keys: any, customSubItems: any): void {
    for (let i = 0; i < components.length; i++) {
      const comp = components[i];
      if (!!comp.range) {
        this.setRangeKey(comp);
      }
      let key = comp.key;
      if (!!key) {
        if (!!comp.originalKey) {
          this.keysMap.fields.originalKeysMap[key] = comp.originalKey;
        }
        if (comp.columns) {
          this.mapCustomRepeatableSubElements(comp, customSubItems);
        };
        if (key === 'custom_ind_name' && comp.domain) { // genera unique key per le customDomainsKeys concatenando il domain
          if (
            !this.keysMap.fields.targetIndividualIRIsByDomain[comp.domain]
            // && !!comp.defaultValue
          ) { this.keysMap.fields.targetIndividualIRIsByDomain[comp.domain] = !!comp.defaultValue ? comp.defaultValue : null; }
          key = key + '_$#$_' + comp.domain;
        }
        const entityPropertyDomain = this.getEntityPropertyDomain(comp.columns);
        key = !!entityPropertyDomain ? key + '_$#$_' + entityPropertyDomain : key; // idem per entityProperties
        keys[key] = {};
        _.forEach(Object.keys(comp), k => {
          if (k !== 'columns' && k !== 'components') {
            keys[key][k] = comp[k];
          } else {
            this.mapDataPropertyKeys(comp[k], keys, customSubItems);
          }
        });
      } else if (!!comp.columns) {
        this.mapDataPropertyKeys(comp.columns, keys, customSubItems);
      } else if (!!comp.components) {
        this.mapDataPropertyKeys(comp.components, keys, customSubItems);
      }
    }
  }

  /**
   * Toglie ricorsivament gli elementi settati a hidden
   * @param components 
   */
  public deleteHiddenComponents(components: any[]): void {

    const deleteElement = (elements: any, idx: number): void => {
      const spliced = elements.splice(idx, 1);
      if (spliced.length === 1) {
        this.hiddenComponents.push(spliced[0]);
      }
    }

    for (let i = 0; i < components.length; i++) {
      const comp = components[i];

      const container = !!comp.columns ? comp.columns : comp.components;

      if (!!container) {
        for (let j = 0; j < container.length; j++) {
          const subComponent = !!container[j].columns ? container[j].columns : container[j].components;

          if (
            !!container[j].hidden ||
            (!!subComponent && subComponent.length === 1 && subComponent[0].hidden)
          ) {
            deleteElement(container, j);
            j--;
          }
        }
        this.deleteHiddenComponents(container);
      }

      if (!!comp.hidden) {
        deleteElement(components, i);
        i--;
      }
    }
  }

  /**
   * Controlla se almeno una delle colonne è una custom_ind_name per determinare se è entityProperty
   *
   * @private
   * @param {[]} columns
   * @returns {boolean}
   * @memberof DetailComponent
   */
  private getEntityPropertyDomain(columns: []): string {
    let compDomain;
    if (!!columns && columns.length > 0) {
      _.forEach(columns, col => {
        if (!!col.components && col.components.length > 0) {
          _.forEach(col.components, comp => {
            if (!!comp.key && comp.key === 'custom_ind_name') {
              compDomain = comp.domain;
            }
          });
        }
      });
    }
    return compDomain;
  }

  /**
   *
   *
   * @param {*} masterFieldsKeysMap
   * @memberof DetailComponent
   */
  public mapCustomAndEntityPropertyKeys(masterFieldsKeysMap: any): void {
    const auxMap = {};
    const auxDomainsKeys = [];

    _.forEach(Object.keys(masterFieldsKeysMap.keys), k => {
      if (!!k && k.includes('_$#$_')) {
        const splitted = k.split('_$#$_');
        if (splitted.length > 1) {
          const suffix = splitted[0];
          const domain = splitted[1];
          if (!!suffix && suffix === 'custom_ind_name') {
            auxMap[domain] = {};
          } else {
            auxDomainsKeys.push(k);
          }
        }
        delete masterFieldsKeysMap.keys[k];
      }
    });

    _.forEach(auxDomainsKeys, k => {
      const splitted = k.split('_$#$_');
      if (splitted.length > 1) {
        const domainKey = splitted[0];
        const domain = splitted[1];
        if (!!auxMap[domain]) {
          auxMap[domain] = domainKey;
        }
      }
    })

    _.forEach(Object.keys(auxMap), k => {
      this.keysMap.fields.customKeysSet.add(auxMap[k]);
    });

    this.keysMap.fields.customDomainsKeys = auxMap;
  }


  public openConfirmationDialog(params: any, isRestored: boolean): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.height = '20vh';
    dialogConfig.minHeight = '200px';
    dialogConfig.width = '40vw';
    dialogConfig.minWidth = '500px';
    dialogConfig.disableClose = false;
    dialogConfig.data = { message: params.message, action: params.action };
    const dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (!!result && result === 'confirm') {
        if (params.action === 'delete') { this.onDeleteButtonClick(); }
        if (params.action === 'newVersion') { this.getEditableEntity(true, params.originVersion, params.lastVersion, false, false); }
        if (params.action === 'restoreVersion') {
          this.registerPriorVersionAttachments(this.tableAttachments);
          this.getEditableEntity(false, params.originVersion, params.lastVersion, isRestored, false, true);
        }
      }
    });
  }

  public openConfirmOperationDialog(id: any): void {
    this.reloadOperationsSelect();

    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.height = '60vh';
    dialogConfig.minHeight = '200px';
    dialogConfig.width = '40vw';
    dialogConfig.minWidth = '400px';
    dialogConfig.disableClose = true;
    dialogConfig.data = { title: this.operationsDictionary[id], note: '' };

    const dialogRef = this.dialog.open(ConfirmOperationDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      if (!!result && result === 'confirm') {
        const params = {
          operation: id,
          domain: this.domain,
          subDomain: this.subDomain,
          code: this.code,
          version: this.version,
        }

        this.http.postOperation(params, dialogConfig.data.note).subscribe(response => {
          this.commService.setResultMessage(true, { message: response.message, type: 'success', autoClosable: true });
          this.router.navigate(['/summary']);

        }, (_error: any) => {
          this.dispatchErrors(_error);
        });
      }
    });
  }

  public registerPriorVersionAttachments(tableAttachments: any): void {
    tableAttachments = tableAttachments.components[1].data.values;
    tableAttachments = _.filter(tableAttachments, att => {
      let isUploaded: boolean;
      if (att.feId === undefined) {
        isUploaded = false;
      } else {
        isUploaded = true;
      }
      return !isUploaded;
    });
    this.uploadedAttachments = [];
    this.priorRestoredVersionAttachments = JSON.parse(JSON.stringify(tableAttachments));
  }

  public openConfirmDeletionDialog(): void {
    this.reloadOperationsSelect();
    const params = {
      action: 'delete',
      message: 'Sei sicuro di voler eliminare questa versione?',
    };
    this.openConfirmationDialog(params, false);
  }

  public openConfirmNewVersionDialog(originVersion: number, lastVersion: number): void {
    const newVersion = _.toNumber(lastVersion) + 1;
    const message = `Esiste la versione ${lastVersion} in stato 'Pubblicato'. Verrà creata la versione ${newVersion}`;
    const params = {
      action: 'newVersion',
      message,
      originVersion,
      lastVersion
    };
    this.openConfirmationDialog(params, false);
  }

  public openConfirmRestoreVersionDialog(originVersion: number, lastVersion: number): void {
    const message = `Esiste la versione ${lastVersion} in stato 'Inserito'. Vuoi ripristinare sull'ultima versione inserita i dati della versione ${originVersion}?`;
    const params = {
      action: 'restoreVersion',
      message,
      originVersion,
      lastVersion
    };
    this.openConfirmationDialog(params, true);
  }

  public handleRestoreVersion($event): void {
    const code = this.route.snapshot.params.code;

    //this.commService.setFetchingDataStatus(true);

    this.http.getMaxVersion(this.domain, this.subDomain, code)
      .subscribe(data => {

        //this.commService.setFetchingDataStatus(false);

        if (data.Stato === 'Pubblicato') {
          this.openConfirmNewVersionDialog($event.Versione, data.Versione);
        } else {
          this.openConfirmRestoreVersionDialog($event.Versione, data.Versione);
        }
      },
        error => {
          this.dispatchErrors(error);
        });
  }

  public onOperationSelected(): void {
    if (this.operationSelected === 'MODIFICA') {
      this.onModifyButtonClick()
    } else if (this.operationSelected === 'CREA_NUOVA_VERSIONE') {
      this.onNewVersionButtonClick();
    } else if (this.operationSelected === 'SPECIALIZZA') {
      this.onSpecializeButtonClick();
    } else if (this.operationSelected === 'ELIMINA') {
      this.openConfirmDeletionDialog();
    } else {
      this.openConfirmOperationDialog(this.operationSelected)
    }
  }

  public reloadOperationsSelect(): void {
    this.operationSelected = undefined;
    this.editState = true;
    setTimeout(() => this.editState = false);
  }

  public dispatchErrors(_error) {
    let errors = this.alertsCenterService.errorsHandler(_error)
    if(errors.length===0) {
      const errorMessage = environment.showErrors ? JSON.stringify(_error.error) : '';
      this.alertsCenterService.showAlert({
        message: 'Si è verificato unerrore nel salvataggio della nuova entità' + errorMessage, type: 'danger', autoClosable: true
      });
    }
    else {
      errors.forEach(errorMessage => {
        this.alertsCenterService.showAlert({
          message: errorMessage, type: 'warning', autoClosable: true
        });
      });
    }
  }

  public clickOnFirstRelationshipTab(event) {
    if(event.index===2) {
      let tab;
      let tabJ;
      let firstElement=false;
      /* 
        serve per simulare il click del primo tab interno al tab relazioni dei dettagli, altrimenti non viene
        visualizzato attivo il primo tab dopo la prima apertura. Esso non funzionava per contrasti tra Material e Morfeo.
        Gli id del tab label vanno in ordine crescente essendo dinamici per le logiche di Morfeo, quindi non è possibile
        simulare il click su un id preciso. Al momento è stato settato un limite massimo di 1000 (quindi è possibile cambiare
        entità circa 400 volte prima che non si selezioni più il primo tab). In ogni caso dopo aver ricaricato la pagina
        riparte da 1.
       */
      for(let i = 0; i < 1000; i++) {
        let string = "mat-tab-label-"+i+"-0"
        if(document.getElementById(string)) {
          if(firstElement) {
            tab = document.getElementById(string); 
            tabJ = $("#"+string)
            break;
          }
          firstElement=true;
        }
      }
      if(tab) {
        tabJ.next().trigger("click")
        setTimeout(function(){
            tab.click()
            tabJ.trigger("blur"); 
        }, 0);
      }
    }
  }

  public getColspan(i: number): number {
    const widths = {
      0: 2,
      1: 2,
      2: 3,
      3: 3,
    }
    return widths[i];
  }
}
