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
import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { 
  DataTableService,
  MrfFormComponent, 
  IForm,
} from '@eng/morfeo';

import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { CommunicationService } from 'src/app/services/communication.service';
import { 
  EntityType,
} from 'src/app/model/Model.myStandard';

import * as _ from 'lodash';
import { ViewChild } from '@angular/core';
import { DialogComponent } from './dialog-component/dialog.component'
import { HttpService } from 'src/app/services/http.service';
import { environment } from 'src/environments/environment';
/**
 *
 *
 * @export
 * @class TabsComponent
 */
@Component({
  selector: 'app-tabs',
  templateUrl: './tabs.component.html',
  styleUrls: ['./tabs.component.scss']
})
export class TabsComponent implements AfterViewInit{

  @Input() cancel: boolean;
  @Input() editState: boolean;
  @Input() tabsFields: any;
  @Input() type: string;
  @Output() onClose = new EventEmitter();
  @Output() onDeleteRow = new EventEmitter();
  @Output() onRestoreVersion = new EventEmitter();
  @ViewChild(MrfFormComponent) formsRef: MrfFormComponent;

  public tableOptions= {
    filterButtonsNoIcon: true,
  };

  /**
   *
   *
   * @type {*}
   * @memberof TabsComponent
   */
  public entityTypesMap: any;
  
  /**
   *
   *
   * @type {boolean}
   * @memberof TabsComponent
   */
  public isLogged: boolean;
  
  /**
   *
   *
   * @type {boolean}
   * @memberof AttachmentsComponent
   */
  public isNew: boolean;

  /**
   *
   *
   * @type {*}
   * @memberof FieldsComponent
   */
  public jsonFormRef: any;

  /**
   *
   *
   * @type {boolean}
   * @memberof TabsComponent
   */
  showTabsForm: boolean

  /**
   *
   *
   * @type {boolean}
   * @memberof TabsComponent
   */
  public doCheck: boolean;
  /**
   *
   *
   * @type {IForm}
   * @memberof TabsComponent
   */
  public tabsForm: IForm;
  
  /**
   *
   *
   * @type {*}
   * @memberof TabsComponent
   */
  public rangeAPIEndpointMap: any;

  /**
   *
   *
   * @type {*}
   * @memberof TabsComponent
   */
  public selectedEntity: any;

  /**
   * 
   * @param router 
   * @param tableService 
   * @param commService 
   */
  constructor(
    private router: Router,
    private http: HttpService,
    private tableService: DataTableService,
    public commService: CommunicationService,
    public alertsCenterService: AlertsCenterService,
    private route: ActivatedRoute,
    public dialog: MatDialog,
  ) {
    this.editState = false;
    this.isNew = this.route.snapshot.params.newEntity !== undefined;
    this.rangeAPIEndpointMap = {};
    this.doCheck = false;
    this.showTabsForm = true;
  }
  
  /**
   * 
   * @param changes 
   */
  public ngOnChanges(changes: SimpleChanges): void {
    
    if (changes.tabsFields && changes.tabsFields.currentValue) {
      if (
        this.tabsFields.components[0] &&
        this.tabsFields.components[0].components[1] &&
        this.tabsFields.components[0].components[1].components[0] &&
        this.tabsFields.components[0].components[1].components[0].components
      ) {

        const relationships = this.tabsFields.components[0].components[1].components[0].components;
        
        if (this.editState) {
          this.createNewRelationshipButtons(relationships);
          this.setRelationshipsKeysOnRows(relationships);
        }
        

        _.forEach(relationships, relationshipTab => {
          if (!!relationshipTab && !!relationshipTab.components && !!relationshipTab.components[0]) {

            delete relationshipTab.components[0].readOnly;
            delete relationshipTab.components[0].hidden;
            
            this.tableService.setCallback(
              [ relationshipTab.components[0].key, 'link', 'link', ],
              (el) => {
                const labelsMap = this.commService.getLabelsMap().domains;
                const domain = this.route.snapshot.params.domain;
                this.router.navigate(['/', this.commService.getBaseApiPath(), domain, el.TipoEntita, el.CodiceEntita, el.Versione]);
              },
            );

            this.tableService.setCallback(
              [ relationshipTab.components[0].key, 'addRelationship', 'edit', ],
              (el) => {
                this.onNewRelatioshipClick(el.value.state);
              },
            );

            // ranges mapping
            const rangeValue =  relationshipTab.range;
            const rangeKey = relationshipTab.key.replace('tab_relation_', '');
            if (!!rangeKey && !!rangeValue) {
              this.rangeAPIEndpointMap[rangeKey] = rangeValue;
            }

            if (this.editState && !!relationshipTab.components[1]) {
              this.tableService.setCallback(
                [ relationshipTab.components[1].key, 'delete', 'delete', ],
                (el) => {
                  this.onDeleteRow.emit({ selectedRelationship: el.relationshipKey, selectedEntity: el});
                },
              );
            }
          }
        });

      }

      if (
        this.tabsFields.components[0] &&
        this.tabsFields.components[0].components[0] &&
        this.tabsFields.components[0].components[0].components[0]
      ) {
        const historicTable = this.tabsFields.components[0].components[0].components[0];
        delete historicTable.readOnly; 
        delete historicTable.hidden;

        this.tableService.setCallback(
          [ historicTable.key, 'restore', 'restore', ],
          (el) => {
            this.onRestoreVersion.emit(el);
          }
        );

        this.tableService.setCallback(
          [ historicTable.key, 'link', 'link', ],
          (el) => {
            const domain = this.route.snapshot.params.domain;
            const subDomain = this.route.snapshot.params.subDomain;
            this.router.navigate(['/', this.commService.getBaseApiPath(), domain, subDomain, el.CodiceEntita, el.Versione]);
          },
        );

      }

      this.tabsForm = this.type === 'historical' ? this.tabsFields.components[0].components[0] : this.tabsFields.components[0].components[1];
    }
  }
  
  /**
   *
   *
   * @param {any[]} relationships
   * @memberof TabsComponent
   */
  public setRelationshipsKeysOnRows(relationships: any[]): void {

    _.forEach(relationships, rel => {

      const relationshipKey = rel.key.replace('tab_relation_', '');

      if (
        !rel.components ||
        !rel.components[1] ||
        !rel.components[1].data ||
        !rel.components[1].data.values
      ) return;

      _.forEach(rel.components[1].data.values, row => {
        row.relationshipKey = relationshipKey;
      });

    });
  }

  /**
   *
   *
   * @param {*} tabs
   * @returns {*}
   * @memberof TabsComponent
   */
  public createNewRelationshipButtons(tabs: any): any {
    const createNewRelationshipButton = (key) => {

      const json = {
        type: 'dataTable', key, dataSrc: 'values',
        data: {
          values: [{ value: {state: key} }],
          columns: [
            { value: 'state', label: '' },
            {
              value: 'addRelationship', label: '#NuovaRelazione',
              buttons: [{
                label: "NUOVA RELAZIONE",
                icon: 'download',
                action: 'edit',
                color: 'primary',
              }]
          }],
        }
      }

      return json;
    };

    _.forEach(tabs, tab => {
      if (
        !!tab.components &&
        tab.components[0].type === "button"
      ) {
        tab.components[0] = createNewRelationshipButton(tab.components[0].key);
      }
    });
  }

  /**
   *
   *
   * @param {*} node
   * @param {(node) => void} func
   * @memberof TabsComponent
   */
  public visitDom(node: any, func: (node) => void) {
    func(node);
    node = node.firstChild;
    while (!!node) {
      this.visitDom(node, func);
      node = node.nextSibling;
    }
  }

  /**
   *
   *
   * @param {*} selectedRelationship
   * @memberof TabsComponent
   */
  public onNewRelatioshipClick(selectedRelationship: any): void {
    let relationshipKey = this.route.snapshot.params.subDomain;
    if((selectedRelationship as string).includes('newRelationshipButton_')){
      relationshipKey = (selectedRelationship as string).replace('newRelationshipButton_','');
    }
    const form = this.createForm(relationshipKey);
    this.openDialog(form, selectedRelationship);

  }
  
  public setValues(formValuesField: any, values: any, selectedRelationship: string): void {

    const getTab = (selectedRelationship: string, existingTabs: any[]): any => {
      let selectedTab = null;

      _.forEach(existingTabs, tab => {

        if (!tab.key ) console.log("[tabs.component:setValues] error in morfeo json - dataTable error");
        if (tab.key.replace('newRelationshipButton_', '').replace('tab_relation_', '') === selectedRelationship) {
          selectedTab =  tab;
        }
        
      });

      return selectedTab;
    }

    const filterValues = (newValues: any[], existingValues: any[]): any[] => {
      const existingValuesMap = {};

      _.forEach(existingValues, value => {
        if (!existingValuesMap[value.CodiceEntita]) {
          existingValuesMap[value.CodiceEntita] = {};
        }
        existingValuesMap[value.CodiceEntita][value.Versione] = true;
      });

      const filtered = _.filter(newValues, value => {
        if (
          !existingValuesMap[value.CodiceEntita] ||
          !existingValuesMap[value.CodiceEntita][value.Versione]
        ) {
          return true;
        }
        return false;
      });

      return filtered;
    }

    if (
      !this.tabsFields ||
      !this.tabsFields.components ||
      !this.tabsFields.components[0] ||
      !this.tabsFields.components[0].components ||
      !this.tabsFields.components[0].components[1] ||
      !this.tabsFields.components[0].components[1].components ||
      !this.tabsFields.components[0].components[1].components[0] ||
      !this.tabsFields.components[0].components[1].components[0].components
    ) {
      console.log("[tabs.component:setValues] error in morfeo json - relationshipsTab")
      return;
    }
    
    const existingTabs = this.tabsFields.components[0].components[1].components[0].components;
    const clearedSelectedRelationship = selectedRelationship.replace('newRelationshipButton_', '').replace('tab_relation_', '');
    const selectedRelationshipTab = getTab(clearedSelectedRelationship, existingTabs);

    if (!selectedRelationshipTab) return;

    if (
      !selectedRelationshipTab.components ||
      !selectedRelationshipTab.components[1] ||
      !selectedRelationshipTab.components[1].data
    ) {
      console.log("[tabs.component:setValues] error in morfeo json - dataTable error")
      return;
    }

    const existingValues = selectedRelationshipTab.components[1].data.values;
    formValuesField.values = filterValues(values, existingValues);

  }

  /**
   *
   *
   * @param {IForm} relationshipsForm
   * @memberof TabsComponent
   */
  public openDialog(relationshipsForm: IForm, selectedRelationship: any): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      form: relationshipsForm,
      selectedEntity: this.selectedEntity,
    };
    dialogConfig.maxHeight = '500px';
    dialogConfig.width = '60vw';
    dialogConfig.disableClose = false;

    const dialogRef = this.dialog.open(DialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe( result => {

      if (!dialogConfig.data.selectedEntity) {
        return;
      }
      const selectedEntity = dialogConfig.data.selectedEntity;
      this.onClose.emit({ selectedRelationship, selectedEntity });

    });
  }

  /**
   *
   *
   * @param {*} node
   * @param {string} attribute
   * @param {string} value
   * @returns {*}
   * @memberof TabsComponent
   */
  public isRelationshipTab(node: any, attribute: string, value: string): any {
    const attributes = node.attributes;
    _.forEach(attributes, att => {
      if (att === attribute) {
        return true;
      }
    })
    return false;
  }

  /**
   *
   *
   * @memberof TabsComponent
   */
  public ngAfterViewInit() {
  }

  /**
   *
   *
   * @private
   * @returns {IForm}
   * @memberof DashboardComponent
   */
  private createForm(relationshipKey: string): IForm {
    let range = relationshipKey;
    if(this.rangeAPIEndpointMap[relationshipKey]) {
      range = this.rangeAPIEndpointMap[relationshipKey];
    } 
    const url = `/relazioni/range/${this.route.snapshot.params.domain}/${range}`+ 
    '?pageNum=$pageNum&pageSize=$pageSize&sortField=$sortField&sortDirection=$sortDirection&$filter';
    let newForm: IForm;
    newForm = {
      components: [
        {
          type: 'dataTable', key: 'newRelationshipTableKey', dataSrc: 'url',
          data: {
            url,
            columns: [
                  { value: 'CodiceEntita', label: 'Codice' },
                  { value: 'Versione', label: 'Versione' },
                  { value: 'name', label: 'Nome' },
                  { value: 'Stato', label: 'Stato' },
                  { value: 'tools', label: 'Aggiungi',
                    buttons: [
                      { label: 'Dettaglio', icon: 'add', action: 'edit', color: 'primary', style: 'icon' }
                    ]
                  },
            ],
            pagination: { sizeOptions: [ 10, 20, 50 ] },
            filter: {
              components: this.generateFilters(),
            }
          },
          validate: { custom: '' },
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
                  { label: 'Inserito', value: 'Inserito' },
                  { label: 'Pubblicato', value: 'Pubblicato' },
                  { label: '', value: '' }
                ]},
              },
              
            ],
            "width": 2
          }
        ]
      }
    ];

    return components;    
  }

}

