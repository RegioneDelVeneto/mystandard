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

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.scss']
})

export class ConfirmDialogComponent implements OnInit {

  public dataReference: any;
  public buttonText: string;
  private buttonTextMap: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) data,
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
  ) {
    this.dataReference = data;
    this.buttonTextMap = {
      delete: 'ELIMINA',
      newVersion: 'CREA VERSIONE',
      restoreVersion: 'RIPRISTINA'
    };
    this.buttonText = this.buttonTextMap[data.action];
  }

  ngOnInit(): void {
  }

  onConfirmClick(): void {
    this.dialogRef.close('confirm');
  }

  onCloseCLick(): void {
    this.dialogRef.close();
  }
}
