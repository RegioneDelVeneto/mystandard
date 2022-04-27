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
  OnChanges,
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
} from '@eng/morfeo';

import { HttpService } from 'src/app/services/http.service';

import * as _ from 'lodash';
import * as $ from 'jquery';
import * as moment from 'moment';
import { ActivatedRoute } from '@angular/router';

/**
 *
 *
 * @export
 * @class AttachmentsComponent
 * @implements {OnInit}
 * @implements {OnChanges}
 */
@Component({
  selector: 'app-attachments',
  templateUrl: './attachments.component.html',
  styleUrls: ['./attachments.component.scss']
})
export class AttachmentsComponent implements OnInit, OnChanges {
  @Input() attachments: any;
  @Input() cancel: boolean;
  @Input() editState: boolean;
  @Input() newUploadedAttachment: any;
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
  public uploadedAttachments: any[];
  public updateAttachments: boolean;
  public attachmentsList: any;

  public uploadedAttachment: { [x: string]: any; };
  public values: any;
  repeatableList: any[];
  attachmentsNum: any;
  windowOpened: boolean;

  constructor(
    private datatableService: DataTableService,
    private httpService: HttpService,
    public renderer: Renderer2,
    private route: ActivatedRoute,
  ) {
    this.editState = false;
    this.showReadOnlyForm = true;
    this.showEditableForm = true;
    this.testAttachmentsIds = {};
    this.isNew = this.route.snapshot.params.newEntity !== undefined;
    this.attachmentPropsMap = {
      name: 'mystd_nome_allegato',
      type: 'mystd_tipoFile',
      lastModifiedDate: 'mystd_date',
      lastModified: 'lastmodified',
      description: 'mystd_descrizione_allegato',
    }
    this.counter = 1;
    this.fields = [];
    this.mappedAttachments = {};
    const a = this.attachments;
  }

  ngOnInit(): void {
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
        !this.attachments.components[0] ||
        !this.attachments.components[0].data
      ) return;

      this.values = this.attachments.components[0].data.values;

      this.attachmentsNum = this.values.length;

      this.counter = this.values.length + 1;

      this.repeatableList = [];
      this.uploadedAttachments = !!this.newUploadedAttachment ? [this.newUploadedAttachment] : [];

      this.removeCustomIndNameColumns(this.attachments);
      
      this.editableForm = this.attachments;
    }
  }


  public ngDoCheck() {
    if (this.updateAttachments) {
      this.setFields();
      this.updateAttachments = false;
    }
  }

  public removeCustomIndNameColumns(attachments: any): void {
    if (
      !attachments ||
      !attachments.components
    ) return;
    _.forEach(attachments.components, component => {
      if (!component.components) return;
      _.forEach(component.components, compComponent => {
        compComponent.columns = _.filter(compComponent.columns, column => {
          if ( column.components && column.components[0] && column.components[0].key !== 'custom_ind_name') return true;
          return false;          
        });
      });
    });
  }

  public setDateFormatOnMorfeoColumns(attachments: any): void {
    if (
      !attachments ||
      !attachments.components
    ) return;
    _.forEach(attachments.components, component => {
      if (!component.columns) return;
      _.forEach(component.columns, column => {
        if (!column.components) return;
        _.forEach(column.components, columnComponent => {
          if (!columnComponent.defaultValue) return;
          if (columnComponent.defaultValue.includes('GMT')) {
            const newMoment = moment(columnComponent.defaultValue).format('DD/MM/YYYY');
            if (!!newMoment) {
              columnComponent.defaultValue = newMoment.toString();
            }
          }
        });
      });
    });
  }

  public setDateFormatOnMorfeoRepeatable(attachments: any): void {
    if (
      !attachments ||
      !attachments.components
    ) return;
    _.forEach(attachments.components, component => {
      if (!component.data || !component.data.values) return;
      _.forEach(component.data.values, value => {
        _.forEach(Object.keys(value), key => {
          if (value[key].includes('GMT')) {
            const newMoment = moment(value[key]).format('DD/MM/YYYY');
            if (!!newMoment) {
              value[key] = newMoment.toString();
            }
          }
        });
      });
    });
  }

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes.attachments !== undefined && changes.attachments.currentValue !== undefined) {
      const form = changes.attachments.currentValue;
      
      if (this.isNew || this.editState) {
        form.components[0].emptyRepeatable = true;
        this.setDateFormatOnMorfeoRepeatable(changes.attachments.currentValue);
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

        this.setDateFormatOnMorfeoColumns(changes.attachments.currentValue);

        this.readOnlyForm = changes.attachments.currentValue;
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
    this.uploadedAttachments.push({ lastModifiedDate: newDate, counter: this.counter, uui: file.idFile, file: event.target.files[0] });

    const name = this.attachmentPropsMap.name + (this.counter === 1 ? '' : ':' + this.counter);
    const date = this.attachmentPropsMap.lastModifiedDate + (this.counter === 1 ? '' : ':' + this.counter);
    const type = this.attachmentPropsMap.type + (this.counter === 1 ? '' : ':' + this.counter);
    
    const newAttachment = {
      [name]: file.name,
      [date]: newDate.toString(),
      [type]: file.type,
    };

    this.uploadedAttachment = newAttachment;

    this.updateAttachments = true;

    this.counter++;

    if (this.uploadedAttachments && this.uploadedAttachments.length !== 0) {
      if (this.counter === 2) {
        
        this.onAttachmentChange.emit({ 
          hasAttachments: true, 
          newFirstAttachment: newAttachment, 
          uploadedAttachments: this.uploadedAttachments[0] ? this.uploadedAttachments[0] : null 
        });

      } else {

        this.onAttachmentChange.emit({ hasAttachments: true });
      }
    }
  }

  public setUui(): string {

    let uui = null;

    const exists = (input) => {
      let found = false;
      _.forEach(this.uploadedAttachments, a => {
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

  public setFields(): void {

    const controlAttachments = this.editableJsonFormRef.form.controls;
    const attachmentsToRender = {};

    _.forEach(Object.keys(controlAttachments), k => {
      attachmentsToRender[k] = controlAttachments[k].value;
    });

    _.forEach(Object.keys(this.uploadedAttachment), k => {
      if (
        k.length >= 3 &&
        k.substring(0, 36) === 'https://w3id.org/italia/onto/TI/date'
      ) {
        const idx = k.substring(37, k.length);
        const formattedDate = moment(this.uploadedAttachment[k]).format("DD/MM/YYYY");
        attachmentsToRender[k] = formattedDate;
      } else {
        attachmentsToRender[k] = this.uploadedAttachment[k];
      }
    });


    this.editableJsonFormRef.setValue(attachmentsToRender);
    const num = $('mrf-repeatable-container div div button');

    $('mrf-repeatable-container div div button').on('click', () => {
      const num = $('mrf-repeatable-container div div button');
    });

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

}
