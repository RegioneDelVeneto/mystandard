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
import { AfterViewChecked, Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import * as _ from 'lodash';
import { IForm, MrfFormComponent } from '@eng/morfeo';

@Component({
  selector: 'app-attachments-table-dialog',
  templateUrl: './attachments-table-dialog.component.html',
  styleUrls: ['./attachments-table-dialog.component.scss']
})
export class AttachmentsTableDialogComponent implements OnInit, AfterViewChecked {
  private name = '';
  private description = '';
  public dataReference: any;

  public form: IForm;
  private jsonFormRef: any;
  @ViewChild(MrfFormComponent) formRef: MrfFormComponent;

  constructor(
    @Inject(MAT_DIALOG_DATA) data,
    private dialogRef: MatDialogRef<AttachmentsTableDialogComponent>,
  ) {
    this.name = data.name;
    this.description = data.description;
    this.dataReference = data;
  }

  ngOnInit(): void {
    this.createForm();
  }

  public ngAfterViewChecked(): void {
    this.formRef.formReadyEvent.subscribe(f => {
      this.jsonFormRef = f;
    });
  }

  public onCloseClick(): void {
    this.dialogRef.close();
  }

  public onSaveClick(): void {
    this.dataReference.name = this.jsonFormRef.form.controls.name.value;
    this.dataReference.description = this.jsonFormRef.form.controls.description.value;
    this.dialogRef.close();
  }

  public createForm(): void {
    this.form = {
      components: [
        {
      key: 'columns',
      type: 'columns',
      columns: [
            {
          components: [
                {
              key: 'name',
              type: 'textfield',
              label: 'Nome',
              hidden: false,
              defaultValue: this.name,
                }
              ]
            },
            {
          components: [
                {
              key: 'description',
              type: 'textfield',
              label: 'Descrizione',
              defaultValue: this.description,
                }
              ]
            }
          ],
        }
      ]
    }
  }

}
