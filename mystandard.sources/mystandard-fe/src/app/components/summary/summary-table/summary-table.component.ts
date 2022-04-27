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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/services/authentication.service';

@Component({
  selector: 'app-summary-table',
  templateUrl: './summary-table.component.html',
  styleUrls: ['./summary-table.component.scss']
})
export class SummaryTableComponent implements OnInit {

  @Input() summaryTable: any;
  public userDomain: string;

  @Output()
  public sortForm: EventEmitter<any> = new EventEmitter<any>();


  public displayedColumns: string[] = ['DominioBusiness', 'LabelTipoEntita', 'name', 'Versione', 'Stato', 'DataUltimaModifica', 'action'];

  constructor( public router: Router, public authService: AuthenticationService ) { }

  ngOnInit(): void { }

  public navigateToEntityDetails (entity) {
    let urlDomain = 'Generale';
    const dominiEntita = entity.DominioBusiness.split(',');
    if(this.authService.profile.domains.length === 1 && this.authService.profile.domains.includes('Pagamenti') &&
       dominiEntita.includes('Pagamenti')) { 
        urlDomain = 'Pagamenti';
    }

    const url = `entities/${urlDomain}/${entity.TipoEntita}/${entity.CodiceEntita}/${entity.Versione}`
    this.router.navigate([url])
  }

  sortData(event){
    this.sortForm.emit(event)
  }
}
