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
import { CommunicationService } from '../../../services/communication.service';
import { SearchService } from '../../../services/search.service';
import { AlertsCenterService } from 'src/app/services/alerts-center.service';

import * as _ from 'lodash';
import {
  DataTableService,
  MrfFormComponent,
  IForm
} from '@eng/morfeo';
import { Router } from '@angular/router';

@Component({
  selector: 'app-free-search',
  templateUrl: './free-search.component.html',
  styleUrls: ['./free-search.component.scss']
})

export class FreeSearchComponent {

  public query: any;
  public modifiedEntityTypes: any;
  public entityTypeSelected: string;
  public researchTypes: {value: string, name: string}[];
  public researchTypeSelected: string;
  public hasValues: boolean;
  public formJson: IForm;
  public entitySelected: any;
  public dataSource: any;
  public tableColumns: string[];
  public pageLength: number;
  private uniqueAllKey = '';

  constructor(
    public searchService: SearchService,
    public commService: CommunicationService,
    private alertsCenterService: AlertsCenterService,
    private router: Router,
    public tableService: DataTableService,
  ) {
    this.entityTypeSelected = this.commService.getFreeSearchType();
    this.commService.setFreeSearchType('');
    this.pageLength = 0;
    this.tableColumns = ['code', 'version', 'name', 'entityDisplay', 'detail'];
    this.query = null;
    const paths = this.commService.getPaths();
    this.uniqueAllKey = this.generateUniqueAllKey(paths);
    this.modifiedEntityTypes = [{ value: this.uniqueAllKey, name: 'Tutte le entità' }, ...paths];
    this.modifiedEntityTypes = this.modifiedEntityTypes.sort((a, b) => (a.name > b.name) ? 1 : -1);
    this.hasValues = false;
    this.entityTypeSelected = this.entityTypeSelected  !== '' ? this.entityTypeSelected  : this.uniqueAllKey;
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

  private generateUniqueAllKey(paths: any[]): string {
    let uniqueKey = '';
    let filtered = [];
    do {
      uniqueKey = _.uniqueId('all_');
      filtered = _.filter(paths, p => p.value === uniqueKey);
    } while (filtered.length > 0);
    return uniqueKey;
  }

  public search(offset, pageSize): void {
    const entityTypeSelected = this.entityTypeSelected === this.uniqueAllKey ? null : this.entityTypeSelected;
    //this.commService.setFetchingDataStatus(true);
    this.searchService.searchEntities(
      this.query, offset, pageSize,
      this.researchTypeSelected, entityTypeSelected)
    .subscribe(data => {
      //this.commService.setFetchingDataStatus(false);
      this.pageLength = data.totaltems;
      this.dataSource = this.mapData(data);
      this.hasValues = true;
    });
  }

  public navigate(element): void {
    this.router.navigate(['/', this.commService.getBaseApiPath(), element.domain, element.entityType, element.code, element.version]);
  }

  public mapData(data): any {
    const values = [];
    _.forEach(data.results, v => {
      const value: any = {};
      value.code = v.dataProperties['dataProperty.https://mystandard.regione.veneto.it/onto/BPO#CodiceEntita'];
      value.version = v.dataProperties['dataProperty.https://mystandard.regione.veneto.it/onto/BPO#Versione'];
      value.name = v.dataProperties['dataProperty.https://w3id.org/italia/onto/l0/name'];
      value.entityType = v.entityType;
      value.entityDisplay = this.commService.getLabelsMap().subDomains[value.entityType];
      value.domain = v.dataProperties.__internal_domain__;
      values.push(value);
    });
    return values;
  }

  public getPage($event): void{
    const pageIndex = $event.pageIndex;
    const pageSize = $event.pageSize;

    this.search(pageIndex * pageSize, pageSize);
  }


}
