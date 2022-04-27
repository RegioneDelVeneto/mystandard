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
import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  QueryList,
  Renderer2,
  SimpleChanges,
  ViewChild,
  ViewChildren,
} from '@angular/core';

import {
  DataTableService,
  MrfFormComponent,
  IForm,
  ButtonService,
} from '@eng/morfeo';

import { HttpService } from 'src/app/services/http.service';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';

import * as _ from 'lodash';
import * as $ from 'jquery';
import * as moment from 'moment';
import { ActivatedRoute } from '@angular/router';
import { UtilsService } from '../../../services/utils.service';
import { AttachmentsTableDialogComponent } from './attachments-table-dialog/attachments-table-dialog.component';

@Component({
  selector: 'app-attachments-table',
  templateUrl: './attachments-table.component.html',
  styleUrls: ['./attachments-table.component.scss']
})
export class AttachmentsTableComponent implements OnInit {
  @Input() attachments: any;
  @Input() cancel: boolean;
  @Input() editState: boolean;
  @Input() newUploadedAttachment: [];
  @Output() onAttachmentChange = new EventEmitter();
  @Output() onValidationChangeState = new EventEmitter();
  @ViewChildren(MrfFormComponent) formsRef: QueryList<MrfFormComponent>;
  @ViewChild('attachmentInputId') dummyInputRef: ElementRef;
  @ViewChild(MrfFormComponent) editableFormRef: MrfFormComponent;

  public attachmentPropsMap: any;
  public counter: number;
  public editableForm: any;
  public editableJsonFormRef: any;
  public fields: any[];
  public isLogged: boolean;
  public isNew: boolean;
  public readOnlyForm: IForm;
  public readOnlyJsonFormRef: any;
  public showReadOnlyForm: boolean;
  public showEditableForm: boolean;
  public testAttachmentsIds: {};
  public mappedAttachments: any;
  public uploadedAttachments_old: any[];
  public uploadedAttachments: any[];
  public updateAttachments: boolean;
  public attachmentsList: any;
  public unusedColumns: [];
  public attachmentsState: any;

  public uploadedAttachment: any;
  public values: any;
  repeatableList: any[];
  attachmentsNum: any;
  windowOpened: boolean;

  public tableOptions= {
    filterButtonsNoIcon: true,
  };
    constructor(
    private datatableService: DataTableService,
    private httpService: HttpService,
    public renderer: Renderer2,
    private route: ActivatedRoute,
    private buttonService: ButtonService,
    private utils: UtilsService,
    public dialog: MatDialog,
  ) {
    this.editState = false;
    this.showReadOnlyForm = true;
    this.showEditableForm = true;
    this.testAttachmentsIds = {};
    this.isNew = this.route.snapshot.params.newEntity !== undefined;
    this.attachmentPropsMap = {
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
    this.counter = 1;
    this.fields = [];
    this.mappedAttachments = {};
  }

  ngOnInit(): void {
    this.uploadedAttachments_old = [];
    this.uploadedAttachments = [];
    if (
      this.editableForm &&
      this.editableForm.components && this.editableForm.components[0] &&
      this.editableForm.components[0].data && this.editableForm.components[0].data.values
    ) {
      this.counter = this.editableForm.components[0].data.values.length + 1;
    }
    if (this.isNew || this.editState) {
      if (
        !this.attachments ||
        !this.attachments.components ||
        !this.attachments.components[1] ||
        !this.attachments.components[1].data
      ) return;

      this.values = this.attachments.components[1].data.values;

      this.attachmentsNum = this.values.length;

      this.counter = this.values.length + 1;

      this.repeatableList = [];
      this.uploadedAttachments_old = !!this.newUploadedAttachment ? [this.newUploadedAttachment] : [];

      this.correctJson(this.attachments.components);

      this.attachmentsState = JSON.parse(JSON.stringify(this.attachments));

      this.editableForm = this.attachments;

      setTimeout(() => {

        this.buttonService.registerCallback('newAttachmentButton', () => {
          this.clickOnDummy();
        });

        this.datatableService.setCallback(
          [ this.editableForm.components[1].key, 'action', 'delete', ],
          (el) => {

            const idKey = el.feId !== undefined && el.feId !== null ? 'feId' : this.attachmentPropsMap.id;
            const standardKey = 'https://mystandard.regione.veneto.it/onto/BPO#id_documento';

            // remove from rendered attachments (model)
            let hasAttachments = false;
            let deletedAttachment;
            if ( !this.utils.isUndefined(this.attachmentsState.components[1].data.values) ) {
              this.attachmentsState.components[1].data.values = _.filter(this.attachmentsState.components[1].data.values, att => {
                const isDeletable = att[idKey] === el[idKey];
                deletedAttachment = isDeletable ? att : undefined;
                return !isDeletable;
              });
              if (this.attachmentsState.components[1].data.values.length === 0) {
                hasAttachments = true;
              }
            }

            this.onAttachmentChange.emit({
              attachmentsState: this.attachmentsState,
              uploadedAttachment: undefined,
              deletedUploadedAttachment: idKey === 'feId' || idKey === standardKey ? deletedAttachment : undefined,
            });
          },
        );

        this.datatableService.setCallback(
          [ this.editableForm.components[1].key, 'action', 'edit', ],
          (el) => {
            this.openDialog(el);
          },
        );

        if (this.readOnlyForm && this.readOnlyForm.components) {
          this.datatableService.setCallback(
            [ this.readOnlyForm.components[0].key, 'action', 'download', ],
            (el) => {
              this.getAttachment(el.id);
            },
          );
        }
      });
    }
  }

  public correctJson(inputAttachments: any): void {
    let attachments;
    attachments = inputAttachments.length > 1 ? inputAttachments[1] : inputAttachments[0];
    delete attachments.label;
    const colsLen = attachments.data.columns.length;
    this.unusedColumns = attachments.data.columns.splice(5, colsLen - 5);
    attachments.data.columns[4].buttons[0].icon = this.editState || this.isNew ? 'edit' : 'download';
    attachments.data.columns[4].buttons[0].action = this.editState || this.isNew ? 'edit' : 'download';
    attachments.data.columns[4].buttons[0].label = this.editState || this.isNew ? 'Modifica' : 'Scarica';

    if (attachments.data.columns[4].buttons[1]) {
      attachments.data.columns[4].buttons[1].label = 'Elimina'
    }
    if (!this.editState && inputAttachments.length > 1) {
      inputAttachments = inputAttachments.splice(0, 1);
      attachments.data.columns[4].buttons.splice(1, 1);
      attachments.data.columns[4].buttons[0].icon = 'download'
      attachments.data.columns[4].buttons[0].action = 'download'
    }
  }

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes.attachments !== undefined && changes.attachments.currentValue !== undefined) {
      const form = changes.attachments.currentValue;
      
      if (this.isNew || this.editState) {
        form.components[0].emptyRepeatable = true;
        if (
          this.editableForm &&
          this.editableForm.components && this.editableForm.components[0] &&
          this.editableForm.components[0].data && this.editableForm.components[0].data.values
        ) {
          this.counter = this.editableForm.components[0].data.values.length + 1;
        }
      } else {
        let counter = 0;
        _.forEach(form.components, c => {
          if (c.columns) {
            const dataTable = c.columns[c.columns.length - 1].components[0];
            dataTable.key += counter.toString();  // unique key
            this.datatableService.setCallback([dataTable.key, 'save', 'edit'], el => {
              this.getAttachment(el.save);
            });
          }
        });

        this.correctJson(changes.attachments.currentValue.components);

        this.readOnlyForm = changes.attachments.currentValue;

        changes.attachments.currentValue.components[0].readOnly = false;
        changes.attachments.currentValue.components[0].data.columns[4].value = 'action';
        changes.attachments.currentValue.components[0].data.columns[4].buttons[0].icon = 'file_download';
        changes.attachments.currentValue.components[0].data.columns[4].buttons[0].action = 'download';
        this.readOnlyForm = changes.attachments.currentValue;
        

        this.datatableService.setCallback(
          [ this.readOnlyForm.components[0].key, 'action', 'download', ],
          (el) => {
            this.getAttachment(el[this.attachmentPropsMap.id]);
          },
        );

      }
    }
  }

  
  public sub: any;

  public ngOnDestroy() {
    if (this.sub)
      this.sub.unsubscribe();
  }

  public ngAfterViewInit(): void {
    if (!this.formsRef.find((el, idx) => idx === 0) || this.sub)
      return;
    if (!this.editState) {
      this.sub = this.formsRef.find((el, idx) => idx === 0).formReadyEvent.subscribe(f => {
        this.readOnlyJsonFormRef = f;
      });
    } else {

      this.sub = this.formsRef.find((el, idx) => idx === 0).formReadyEvent.subscribe(f => {
        this.editableJsonFormRef = f;
        this.setInitialFields();

        f.form.statusChanges.subscribe(val => {
          this.onValidationChangeState.emit({child: 'attachments', value: val});
        });

      });
    }
  }

  public ngAfterViewChecked() {
    if (!this.formsRef.find((el, idx) => idx === 0) || this.sub)
      return;
    if (!this.editState) {
      this.sub = this.formsRef.find((el, idx) => idx === 0).formReadyEvent.subscribe(f => {
        this.readOnlyJsonFormRef = f;
      });
    } else {
      this.sub = this.formsRef.find((el, idx) => idx === 0).formReadyEvent.subscribe(f => {
      });
    }
  }

  public setUui(): string {

    let uui = null;

    const exists = (input) => {
      let found = false;
      _.forEach(this.uploadedAttachments_old, a => {
        if (a.id === input) {
          found = true;
        }
      })
      return found;
    };

    do {
      uui = _.uniqueId('attachment_');
    } while (exists(uui))

    return uui;
  }

  public setInitialFields(): void {

    if (
      !this.editableForm ||
      !this.editableForm.components || !this.editableForm.components[0] ||
      !this.editableForm.components[0].data || !this.editableForm.components[0].data.values
    ) return;

    this.attachmentsList = [];

    const attachments = this.values;
    const processedAttachments = [];

    _.forEach(attachments, attachment => {
      const processedAttachment: any = {};
      _.forEach(Object.keys(attachment), key => {
        this.mappedAttachments[key] = attachment[key];
        processedAttachment[key] = attachment[key];
      });
      processedAttachments.push(processedAttachment);
    });

    this.updateAttachmentsNumber();
  }

  public updateAttachmentsNumber(): void {
    const attachmentsNum = $('mrf-repeatable-container div div button').length;
    this.attachmentsNum = attachmentsNum;
    $('mrf-repeatable-container div div button').off('click');
    $('mrf-repeatable-container div div button').on('click', () => {
      const num = $('mrf-repeatable-container div div button').length;
      if (attachmentsNum === 1 && num === 1) {
        this.onAttachmentChange.emit({ hasAttachments: false });
      }
    });
  }

  public clickOnDummy(): void {
    this.dummyInputRef.nativeElement.click();
  }

  public onFileUpload(event: any): void {

    if (event.target.files.length <= 0) { return; }

    let file = null;

    const newDate = moment().format('DD/MM/YYYY');

    file = event.target.files[0];
    file.idFile = this.setUui();
    this.uploadedAttachments_old.push({ lastModifiedDate: newDate, counter: this.counter, uui: file.idFile, file: event.target.files[0] });
    this.uploadedAttachments.push({ lastModifiedDate: newDate, counter: this.counter, uui: file.idFile, file: event.target.files[0] });
    this.uploadedAttachment = { lastModifiedDate: newDate, counter: this.counter, uui: file.idFile, file: event.target.files[0] };

    const name_old = this.attachmentPropsMap.name_old + (this.counter === 1 ? '' : ':' + this.counter);
    const date_old = this.attachmentPropsMap.date_old + (this.counter === 1 ? '' : ':' + this.counter);
    const type_old = this.attachmentPropsMap.type_old + (this.counter === 1 ? '' : ':' + this.counter);
    
    const newAttachment_old = {
      [name_old]: file.name,
      [date_old]: newDate.toString(),
      [type_old]: file.type,
    };


    this.updateAttachments = true;

    this.counter++;

    ///
    const tmp = this.attachmentsState;

    const custom_ind_name = this.attachmentPropsMap.name;
    const name = this.attachmentPropsMap.name;
    const date = this.attachmentPropsMap.lastModifiedDate;
    const type = this.attachmentPropsMap.type;

    const newAttachment = {
      [name]: file.name,
      [date]: newDate.toString(),
      [type]: !file.type ? 'Allegato generico' : file.type,
      feId: file.idFile
    };

    this.attachmentsState.components[1].data.values.splice(0, 0, newAttachment);

    this.onAttachmentChange.emit({
      hasAttachments: true,
      attachmentsState: this.attachmentsState,
      uploadedAttachment: this.uploadedAttachment,
      deletedUploadedAttachment: undefined,
    });

    ///
    if (this.uploadedAttachments_old && this.uploadedAttachments_old.length !== 0) {
      if (this.counter === 2) {
        
        this.onAttachmentChange.emit({
          old: true,
          hasAttachments: true, 
          newFirstAttachment: newAttachment_old, 
          uploadedAttachments: this.uploadedAttachments_old[0] ? this.uploadedAttachments_old[0] : null 
        });

      } else {

        this.onAttachmentChange.emit({ old: true, hasAttachments: true });
      }
    }
  }

  public getAttachment(attachmentId: string): void {
    const entityId = this.route.snapshot.params.code + '_' + this.route.snapshot.params.version;
    this.httpService.getAttachment(entityId, attachmentId)
      .subscribe(data => {
        const attachmentName = data.headers.get('attachmentName');
        const type = data.headers.get('attachmentType');
        const newBlobText = new Blob([data.body], { type });
        const blobData = window.URL.createObjectURL(newBlobText);
        var link = document.createElement('a');
        link.href = blobData;
        link.download = attachmentName;
        link.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
      });
  }

  public openDialog(attachmentClicked: any): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      name: attachmentClicked[this.attachmentPropsMap.name],
      description: attachmentClicked[this.attachmentPropsMap.description],
    };
    dialogConfig.maxHeight = '500px';
    dialogConfig.width = '40vw';
    dialogConfig.disableClose = true;

    const dialogRef = this.dialog.open(AttachmentsTableDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe( result => {

      const name = dialogConfig.data.name;
      const description = dialogConfig.data.description;
      let editedAttachmentClicked;
      _.forEach(this.attachmentsState.components[1].data.values, att => {
        if (
          (attachmentClicked.feId !== undefined && attachmentClicked.feId === att.feId) // uploaded attachment
          || (attachmentClicked[this.attachmentPropsMap.id] !== undefined && attachmentClicked[this.attachmentPropsMap.id] === att[this.attachmentPropsMap.id]) // existing attachment
        ) {
          att[this.attachmentPropsMap.name] = name;
          att[this.attachmentPropsMap.description] = description;
          editedAttachmentClicked = att;
        }
      });
      this.onAttachmentChange.emit({
        attachmentsState: this.attachmentsState,
        modifiedAttachment: editedAttachmentClicked,
        uploadedAttachment: undefined,
        deletedUploadedAttachment: undefined,
      });
    });
  }

}
