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
import { Component, OnInit } from '@angular/core';
import { CommunicationService } from '../../services/communication.service';
import { SearchService } from '../../services/search.service';
import { AlertsCenterService } from 'src/app/services/alerts-center.service';

import * as _ from 'lodash';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})

export class SearchComponent implements OnInit {

  public query: any;
  public modifiedEntityTypes: any;
  public entityTypeSelected: string;
  public researchTypes: {value: string, name: string}[];
  public researchTypeSelected: string;
  public hasValues: boolean;
  public entitySelected: any;
  private entityTypesMap: any;
  private entityTypesMapReverse: any;
  public dataSource: any;
  public tableColumns: string[];
  public pageLength: number;

  public pathRoute: string;

  constructor(
    public searchService: SearchService,
    public commService: CommunicationService,
    private alertsCenterService: AlertsCenterService,
    private router: Router,
    private activatedRouter: ActivatedRoute
  ) {
    activatedRouter.params.subscribe( (params) => {
      if(params['queryType']) this.pathRoute=params['queryType']
    })
    this.pageLength = 0;
    this.entityTypesMap = {
      enti: { name: 'Ente', value: 'ente', index: 1 },
      aziendaIct: { name: 'Azienda ICT', value: 'aziendaICT', index: 2 },
      processi: { name: 'Processo', value: 'processo', index: 3 },
      api: { name: 'API', value: 'api', index: 4 },
    }

    this.tableColumns = ['code', 'version', 'name', 'entityDisplay', 'detail'];

    this.query = null;
    this.modifiedEntityTypes = [
      { value: 'all', name: 'Tutte le entità' },
      { value: 'enti', name: 'Enti' },
      { value: 'aziendaIct', name: 'Azienda ICT' },
      { value: 'processi', name: 'Processi' },
      { value: 'api', name: 'API' },
    ];
    this.hasValues = false;

    this.entityTypeSelected = 'all';

    this.researchTypeSelected = 'lastVersions';
    this.researchTypes = [
      {
        value: 'lastVersions',
        name: 'Cerca tra ultime versioni',
      },
      {
        value: 'allVersions',
        name: 'Cerca tra tutte le versioni',
      }
    ];

  }

  ngOnInit(): void {
  }

}
