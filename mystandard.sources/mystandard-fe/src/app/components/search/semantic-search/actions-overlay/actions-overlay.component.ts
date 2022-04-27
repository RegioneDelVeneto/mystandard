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
import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-actions-overlay',
  templateUrl: './actions-overlay.component.html',
  styleUrls: ['./actions-overlay.component.scss']
})
export class ActionsOverlayComponent implements OnInit {

  @Input() isOpen: boolean;
  @Input() idRow: number;

  constructor() { }

  ngOnInit(): void {
    console.log(this.idRow);
  }


}