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
import { Component } from '@angular/core';

/**
 *
 *
 * @export
 * @class LoaderComponent
 */
@Component({
  selector: 'app-loader',
  template:
    `<mat-card>
        <mat-card-header>
            <mat-card-title>
                Caricamento dei dati...
            </mat-card-title>
        </mat-card-header>
        <mat-card-content>
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
        </mat-card-content>
    </mat-card>`
})
export class LoaderComponent {

  constructor() { }

}
