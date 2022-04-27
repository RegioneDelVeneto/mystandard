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
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DataTableService, IForm } from '@eng/morfeo';
import * as _ from 'lodash';

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.scss']
})

export class DialogComponent implements OnInit {

  public form: IForm;
  public dataReference: any;
  public tableOptions= {
    filterButtonsNoIcon: true,
  };

  constructor(
    @Inject(MAT_DIALOG_DATA) data,
    private tableService: DataTableService,
    private dialogRef: MatDialogRef<DialogComponent >
  ) {
    this.form = data.form;
    this.dataReference = data;
  }

  ngOnInit(): void {
    this.tableService.setCallback(['newRelationshipTableKey', 'tools', 'edit'], (row) => {
      this.dataReference.selectedEntity = row;
      this.dialogRef.close();
    });
  }
}

