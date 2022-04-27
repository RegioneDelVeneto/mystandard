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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';

import { 
  MrfFormComponent,
  IForm,
  DataTableService,
} from '@eng/morfeo';

import { CommunicationService } from 'src/app/services/communication.service';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { DynamicDialogComponent } from './dynamic-dialog-component/dynamic-dialog.component';
import * as _ from 'lodash';
import { ActivatedRoute } from '@angular/router';
import { HttpService } from 'src/app/services/http.service';
import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { environment } from 'src/environments/environment';

declare var $: any;

/**
 *
 *
 * @export
 * @class FieldsComponent
 * @implements {OnChanges}
 */
@Component({
  selector: 'app-fields',
  templateUrl: './fields.component.html',
  styleUrls: ['./fields.component.scss']
})
export class FieldsComponent implements OnChanges {
  @Input() editState: boolean;
  @Input() masterFields: any;
  @ViewChild(MrfFormComponent) formRef: MrfFormComponent;
  @Output() onClose = new EventEmitter();
  @Output() onValidationChangeState = new EventEmitter();

  /**
   *
   *
   * @type {*}
   * @memberof FieldsComponent
   */
  public entityStates: any;
  
  /**
   *
   *
   * @type {boolean}
   * @memberof FieldsComponent
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
   * @type {IForm}
   * @memberof FieldsComponent
   */
  public jsonForm: IForm;

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
   * @memberof FieldsComponent
   */
  public showReadOnlyForms = true;

  /**
   *
   *
   * @type {string}
   * @memberof FieldsComponent
   */
  public structure: any;

  /**
   *
   *
   * @type {boolean}
   * @memberof FieldsComponent
   */
  public visibility = true;
  private dataTableKey: any;
  private vocabularySubscription: any;

  /**
   *
   * @param commService
   */
  constructor(
    public commService: CommunicationService,
    private route: ActivatedRoute,
    private tableService: DataTableService,
    public alertsCenterService: AlertsCenterService,
    public dialog: MatDialog,
    public http: HttpService
  ) {
    this.entityStates = ['Pubblicato', 'Inserito'];
    this.structure = { type: null, value: null };
    this.isNew = this.route.snapshot.params.newEntity !== undefined;

  }
  

  /**
   * Sets defaultValue for Morfeo bugged components
   *
   * @param {any[]} components
   * @param {*} f
   * @memberof FieldsComponent
   */
  public setMissingDefaultValues(components: any[], f: any): void {
    
    for (let i = 0; i < components.length; i++) {
      const comp = components[i];
      if (comp.type === 'select') {
        if (comp.defaultValue) {
          f.form.patchValue( { [comp.key]: comp.defaultValue.replace('_data', '') } );
        }
        if (comp.key === 'mystd_stato'){
          this.commService.registerEntityState(comp.defaultValue);
        }
      } else if (
        comp.type === 'checkbox' &&
        comp.defaultValue === 'true'
      ) {
        f.form.patchValue( { [comp.key]: comp.defaultValue } );
      } else if (
        comp.type === 'selectboxes' &&
        comp.defaultValue !== undefined && comp.defaultValue !== null
      ) {
        let defaultValue = comp.defaultValue.split(','); 
        let searchBoxDefaultValues = [];
        if (defaultValue.length>1) {
          if (comp.values.length !== defaultValue.length) {
            comp.values.forEach(compValue => {
              defaultValue.forEach(defaultValue => {
                if (compValue.value === defaultValue) {
                  searchBoxDefaultValues.push(defaultValue);
                }
              });
            });
          }
        } else {
          searchBoxDefaultValues = defaultValue;
        }
        let stringifiedDefaultValue =  JSON.stringify(searchBoxDefaultValues); 
        if (!!stringifiedDefaultValue) { f.form.patchValue( { [comp.key]: stringifiedDefaultValue } ); }
      } else if (!!comp.columns) {
        this.setMissingDefaultValues(comp.columns, f);
      } else if (!!comp.components) {
        this.setMissingDefaultValues(comp.components, f);
      }
    }

  }

  public correctDatatable(components: any[]): void {

    for (let i = 0; i < components.length; i++) {
      const comp = components[i];
      if (
        comp.components
        && comp.components[0]
        && comp.components[0].components
        && comp.components[0].components[0]
        && comp.components[0].components[0].type === 'dataTable'
      ) {
        this.dataTableKey = comp.components[0].components[0].key;
        const dataTableComponent = comp.components[0].components[0].data && comp.components[0].components[0];
        if (dataTableComponent.data.columns) {
          _.forEach(comp.components[0].components[0].data.columns, col => {
            if (col.buttons && col.buttons[0] && col.buttons[0].icon === null) {
              col.buttons[0].icon = 'add';
            }
          });
          delete dataTableComponent.readOnly;
          delete dataTableComponent.hidden;
        }
        comp.components = comp.components[0].components;
      } else if (!!comp.columns) {
        this.correctDatatable(comp.columns);
      } else if (!!comp.components) {
        this.correctDatatable(comp.components);
      }
    }

  }

  public sub:any;


  public ngOnDestroy(){
    if (this.sub)
      this.sub.unsubscribe();
    if (this.vocabularySubscription)
      this.vocabularySubscription.unsubscribe();
  }


  public openDialog(tableData: any): void {

    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.data = tableData;
    dialogConfig.maxHeight = '500px';
    dialogConfig.width = '60vw';
    dialogConfig.disableClose = false;

    const dialogRef = this.dialog.open(DynamicDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe( result => {

      if (!dialogConfig.data.selectedEntity) { return; }
      const selectedEntity = dialogConfig.data.selectedEntity;
      _.forEach(Object.keys(selectedEntity), entityKey => {
        this.jsonFormRef.form.patchValue( { [entityKey]: selectedEntity[entityKey] } )
      });

      this.onClose.emit({ dynamicDialogModifiedEntities: selectedEntity });

    });
  }

  /**
   *
   *
   * @memberof FieldsComponent
   */
  public ngAfterViewChecked() {
    if (!this.sub && this.formRef)
    this.sub = this.formRef.formReadyEvent.subscribe(f => {
      this.jsonFormRef = f;

      f.form.statusChanges.subscribe(val => {
        this.onValidationChangeState.emit({child: 'fields', value: val});
      });

      this.setMissingDefaultValues(this.jsonForm.components, f);
      this.commService.setEntityName(f.form.value.mystd_name);


      this.tableService.setCallback([this.dataTableKey, 'save', 'edit'], (row) => {
        const lastIdx = row.save.lastIndexOf('/') + 1;
        let vocabularyType = row.save.substring(lastIdx, row.save.length);
        if (vocabularyType.includes('#')) {
          const lastIdxHash = vocabularyType.lastIndexOf('#') + 1;
          vocabularyType = vocabularyType.substring(lastIdxHash, vocabularyType.length);
        }

        this.vocabularySubscription = this.http.getVocabulary(vocabularyType)
          .subscribe(
            data => {
              this.openDialog(data);
            },
            error => {
              const errorMessage = environment.showErrors ? error.error : '';
              this.alertsCenterService.showAlert({ message: 'Si è verificato un errore nel caricamento dei dati' + errorMessage,  type: 'danger', autoClosable: true });
            }
          );

      });
    
    });
  }

  /**
   * 
   * @param changes 
   */
  public ngOnChanges(changes: SimpleChanges): void {
    if (changes.masterFields && changes.masterFields.currentValue) {
      this.correctDatatable(changes.masterFields.currentValue.components);
      this.setCodeEditorType(changes.masterFields.currentValue.components);
      if (this.editState) {
        this.setWebsitetoTextfield(changes.masterFields.currentValue.components);
      }
      this.jsonForm = changes.masterFields.currentValue;
    }
  }

  public setWebsitetoTextfield(components: any): void {
    for (let i = 0; i < components.length; i++) {
      const comp = components[i];
      if (comp.type === 'htmlelement') {
        const el = $('<div></div>');
        el.html(comp.html);
        const value = $('a', el).attr('href');
        if (value) {
          comp.defaultValue = value;
          comp.type = 'textfield';
        }
      } else if (!!comp.columns) {
        this.setWebsitetoTextfield(comp.columns);
      } else if (!!comp.components) {
        this.setWebsitetoTextfield(comp.components);
      }
    }
  }

  public setCodeEditorType(components: any): void {
    for (let i = 0; i < components.length; i++) {
      const comp = components[i];
      if (comp.type === 'codeEditor') {
        let mode = null;
        if (i == 0 && !!components[i+1]){
          mode = !!components[i+1].defaultValue ? components[i+1].defaultValue.toLowerCase() : 'json';
        } else if (i === components.length - 1 && !!components[i-1]) {
          mode = !!components[i-1].defaultValue ? components[i-1].defaultValue.toLowerCase() : 'json';
        } else {
          if (!!components[i-1]) {
            mode = !!components[i-1].defaultValue ? components[i-1].defaultValue.toLowerCase() : 'json';
          }
          if (!mode) {
            mode = components[i+1].defaultValue ? components[i+1].defaultValue.toLowerCase() : 'json';
          }
        }
        comp.codeEditorOptions = {
          mode,
          indentDefaultValue: true,
          indentSize: 2,
          showLineNumbers: false,
        }
      } else if (!!comp.columns) {
        this.setCodeEditorType(comp.columns);
      } else if (!!comp.components) {
        this.setCodeEditorType(comp.components);
      }
    }
  }

}
