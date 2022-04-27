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
import * as _ from 'lodash';

@Component({
  selector: 'app-dynamic-dialog',
  templateUrl: './dynamic-dialog.component.html',
  styleUrls: ['./dynamic-dialog.component.scss']
})

export class DynamicDialogComponent {

  public columns: any;
  public values: any;
  public dataReference: any;
  public columnNamesMap = {};

  constructor(
    @Inject(MAT_DIALOG_DATA) data,
    private dialogRef: MatDialogRef<DynamicDialogComponent >
  ) {

    this.dataReference = data;
    const columns = _.map(data.columns, column => {
      this.columnNamesMap[column.value] = column.name;
      return column.value;
    });
    columns.push('Aggiungi');
    this.columns = columns;
    this.values = data.value;
  }

  public add(element: any): any {
    this.dataReference.selectedEntity = element;
    this.dialogRef.close();
  }

}

