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
0// tslint:disable
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { CommunicationService } from 'src/app/services/communication.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { HttpService } from 'src/app/services/http.service';

import * as _ from 'lodash';
import { environment } from 'src/environments/environment';
import { UtilsService } from 'src/app/services/utils.service';
import { FormArray, FormControl, FormGroup, FormGroupDirective, NgForm, Validators } from '@angular/forms';
import * as CodeMirror from 'codemirror';

@Component({
  selector: 'app-search-detail',
  templateUrl: './search-detail.component.html',
  styleUrls: ['./search-detail.component.scss']
})
export class SearchDetailComponent implements OnInit {
  public isNew = false;
  public query: {
    name: string,
    description: string,
    query: string,
  }
  public parameters: {
    name: string,
    type: string,
    class: string,
  } [];

  public queryFormControl: FormGroup;
  public paramsFormControl: FormGroup;
  public types: any[];
  public id: string;
  public queryText: any;
  public entityNamesSub: any;

  private domainFilters = [];
  public typeFilters = [];

  constructor(
    private route: ActivatedRoute,
    private http: HttpService,
    private alertsCenterService: AlertsCenterService,
    private router: Router,
    public commService: CommunicationService,
    public authService: AuthenticationService,
    private utils: UtilsService,
  ) {
    
    this.isNew = this.route.snapshot.params.id !== undefined && this.route.snapshot.params.id === 'newQuery';
    
    
    this.types = [ 
      { name: 'Tabellare', value: 'entity'},
      { name: 'Scalare', value: 'scalar'},
    ];

  }

  ngOnInit(): void {
    this.getEntityNames();
    this.getFilters();

    this.queryFormControl = new FormGroup( {
      name: new FormControl(null, [ Validators.required, ]),
      description: new FormControl(null, [ Validators.required, ]),
      query: new FormControl(null, [ Validators.required, ]),
      params: new FormArray([]),
    });


    if (!this.isNew) {
      this.route.params.subscribe(params => {
        if (!params || !params.id) { return; }
        this.id = params.id;
        this.http.getQuery(this.id).subscribe(response => {
          this.query = response.result;
          this.parameters = response.result.params;
          this.setInitialValues();
        }, (_error) => {
          const errorMessage = environment.showErrors ? _error.error : '';
          this.alertsCenterService.showAlert({ message: 'Si è verificato un errore nel caricamento della query' + errorMessage,  type: 'danger', autoClosable: true });
        });
      });
    }

  }

  public getEntityNames(): void {
    const mapEntities = (raw) => {
    };
    let rawEntities = this.commService.getLabelsMap();
    let entities;
    if (rawEntities && rawEntities.domains === {}) {
      this.entityNamesSub = this.commService.getLabelsMapBySubscription().subscribe(value => {
        rawEntities = value;
      });
    } else {
    }
  }

  public setInitialValues(): void {

    _.forEach(Object.keys(this.query), prop => {
      const paramControl = this.queryFormControl.get(prop);
      if (!!paramControl && prop !== 'params') {
        paramControl.setValue(this.query[prop]);
      }
    });

    if (this.parameters && this.parameters.length > 0) {
      _.forEach(this.parameters, par => this.addParam(par));
    }

  }

  public deleteParam(i: number): void {
    (<FormArray> this.queryFormControl.get('params')).removeAt(i);
  }

  public addParam(param?: any): void {

    const paramControl = new FormGroup({
      name: new FormControl(null, [ Validators.required, ]),
      type: new FormControl(null, [ Validators.required, ]),
      class: new FormControl(null, [ Validators.required, ]),
    });
    
    if (!!param) {
      paramControl.get('name').setValue(param.label);
      paramControl.get('type').setValue(param.type);
      paramControl.get('class').setValue(param.key);
    }

    (<FormArray> this.queryFormControl.get('params')).push(paramControl);

  }

  public onSaveButtonClick(): void {
    const paramsControl = this.queryFormControl.get('params').value;
    const names = [];
    let isError = false;
    _.forEach(paramsControl, param => {
      if (names.indexOf(param.name) !== -1) {
        isError = true;
      }
      names.push(param.name);
    });

    if (isError) {
      this.alertsCenterService.showAlert({
        message: `Controllare i nomi dei campi. Esistono dei duplicati`, type: 'danger', autoClosable: true
      });
      return;
    }

    const query = {
      id: this.id,
      name: this.queryFormControl.value.name,
      description: this.queryFormControl.value.description,
      query: this.queryFormControl.value.query,
      params: paramsControl.map(param => {
        return {
          label: param.name,
          'key': param.class,
          type: param.type
        }
      }),
    };


    const operation = this.isNew ? this.http.postQuery(query) : this.http.putQuery(query);
    const successMessage = this.isNew ? 'aggiornata' : 'salvata';
    const errorMessage = this.isNew ? 'nel salvataggio' : 'nella modifica';
    operation.subscribe(result => {
      this.commService.setResultMessage(true, { message: `Query ${successMessage} correttamente`, type: 'success', autoClosable: true, origin: 'query' });
      this.router.navigate(['/search/semquery']);
    },(_error: any) => {
      const errorMessage = environment.showErrors ? JSON.stringify(_error.error) : '';
      this.alertsCenterService.showAlert({
        message: `Si è verificato un errore ${errorMessage} della query` + errorMessage, type: 'danger', autoClosable: true
      });
    });

  }

  public onCancelButtonClick(): void {
    this.router.navigate(['/search/semquery']);
  }

  public onOperationSelected(): void {

  }

  public print(): void {
    const ctrls = this.queryFormControl.get('params')['controls'];
    _.forEach(ctrls, ctrl => {
      console.log(ctrl);
    })
  }

  public fill(): void {
    this.queryText = 'hello';
  }


  public getFilters() {
    this.http.getPaths().then(data => {
      this.domainFilters = data.items.filter(data => data.domain);
      this.populateTypeFilters();
    })
  }

  private populateTypeFilters() {
    this.domainFilters.forEach(domain =>
      domain.items.forEach(item => {
        if (this.typeFilters.includes(item.label) === false) this.typeFilters.push(item.label)
      }))
  }
}
