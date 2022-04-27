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
import { ActivatedRoute, Router } from '@angular/router';
import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { CommunicationService } from 'src/app/services/communication.service';
import { HttpService } from 'src/app/services/http.service';
import { environment } from 'src/environments/environment';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { EntitySelectorDialogComponent } from '../entity-selector-dialog/entity-selector-dialog.component';
import _ from 'lodash';
@Component({
  selector: 'app-search-execute',
  templateUrl: './search-execute.component.html',
  styleUrls: ['./search-execute.component.scss']
})
export class SearchExecuteComponent implements OnInit {
  public id: string;
  public query: {
    name?: string,
    description?: string,
    query?: string,
  };
  public originalQuery: {
    name?: string,
    description?: string,
    query?: string,
  };
  public parameters: any[];
  private dictionary = {
    type: {
      scalar: 'Scalare',
      entity: 'Tabellare'
    }
  };

  public resultColumns: string[];
  public resultValues: any;
  public showError = false;
  public errorMessage = '';
  public showNoResults = false;
  public totalRecords: number;
  public pageSize;
  public pageNum;

  constructor(
    public http: HttpService,
    public commService: CommunicationService,
    public alertsCenterService: AlertsCenterService,
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    this.query = {
      name: 'Nome query'
    };

    this.route.params.subscribe(params => {
      if (!params || !params.id) { return; }
      this.id = params.id;
      this.http.getQuery(this.id).subscribe(response => {
        this.query = response.result;
        this.originalQuery = JSON.parse(JSON.stringify(response.result));
        this.parameters = response.result.params.map(el => {
          el.type = this.dictionary.type[el.type];
          return el;
        });
      }, (_error) => {
        const errorMessage = environment.showErrors ? _error.error : '';
        this.alertsCenterService.showAlert({ message: 'Si è verificato un errore nel caricamento della query' + errorMessage,  type: 'danger', autoClosable: true });
      });
    });

  }

  public cancel(): void {
    this.router.navigate(['/search/semquery/']);
  }

  public execute(params?): void {
    if (this.query.query !== this.originalQuery.query) {
      this.query.query = this.originalQuery.query;
    }

    this.replaceParams();

    function u_btoa(buffer) {
      const binary = [];
      const bytes = new Uint8Array(buffer);
      for (let i = 0, il = bytes.byteLength; i < il; i++) {
          binary.push(String.fromCharCode(bytes[i]));
      }
      return btoa(binary.join(''));
    }

    const encodedString = new TextEncoder().encode(this.query.query);
    let query = u_btoa(encodedString);


    this.commService.setFetchingDataStatus(true);

    this.http.postExecuteQuery(query, params).subscribe(result => {
      if (result.values.length === 0) {
        this.showNoResults = true;
      } else {
        this.showNoResults = false;
      }
      this.totalRecords = result.totalRecords;
      this.resultValues = result.values;
      this.resultColumns = result.columns;
      this.commService.setFetchingDataStatus(false);
      this.showError = false;

    }, (_error) => {
      const errorMessage = environment.showErrors ? _error.error : '';
      this.showNoResults = false;
      this.commService.setFetchingDataStatus(false);
      this.showError = true;
      this.errorMessage = _error.error;
      this.alertsCenterService.showAlert({
        message: `Si è verificato un errore nella esecuzione della query ${errorMessage}`,
        type: 'danger',
        autoClosable: true
      });
    });
  }

  private replaceParams(): void {
    _.forEach(this.parameters, param => {
      if (param.type === 'Scalare') {
        this.query.query = this.query.query.replace(param.label, param.value);
      } else if (param.type === 'Tabellare') {

        this.query.query = this.query.query.replace(param.label, `${param._targetIndividualsIRI}`);
      }
    });
  }

  public onEntityParamClicked(selectedEntity: any): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    let selectedEntityCode;
    dialogConfig.data = {
      selectedEntity,
    };
    dialogConfig.maxHeight = '500px';
    dialogConfig.width = '60vw';
    dialogConfig.disableClose = false;

    const dialogRef = this.dialog.open(EntitySelectorDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe( result => {

      if (!dialogConfig.data.selectedEntity._targetIndividualsIRI) {
        return;
      }
      const selectedTargetIndividualsIRI = dialogConfig.data.selectedEntity._targetIndividualsIRI;

      _.forEach(this.parameters, param => {
        if (dialogConfig.data.selectedEntity.label === param.label) {
          param.value = selectedTargetIndividualsIRI.replace('https://mystandard.regione.veneto.it/onto/BPO_data#', '');
          param._targetIndividualsIRI = selectedTargetIndividualsIRI;
        }
      });

    });
  }

  public getPage($event): void{
    this.pageSize = $event.pageSize;
    this.pageNum = $event.pageIndex;
    const params = {
      pageNum: this.pageNum,
      pageSize: this.pageSize,
    };
    this.execute(params);
  }

}
