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
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import {
  IForm, MrfFormComponent
} from '@eng/morfeo';

import { HttpService } from 'src/app/services/http.service';
@Component({
  selector: 'app-sparql-search',
  templateUrl: './sparql-search.component.html',
  styleUrls: ['./sparql-search.component.scss']
})
export class SparqlSearchComponent implements AfterViewInit {
  @ViewChild(MrfFormComponent) formRef: MrfFormComponent;

  public query: any = null;
  public values: any[] = null;
  public columns = ['code', 'version', 'name', 'entityDisplay', 'detail'];
  public sparqlForm: IForm;
  public jsonFormRef: any;
  public input: any = '';
  public showError = false;
  public errorMessage = '';

  constructor(
    private http: HttpService,
  ) {
    this.sparqlForm = this.createForm();
  }

  public ngAfterViewInit(): void {
    this.formRef.formReadyEvent.subscribe(f => {
      this.jsonFormRef = f;
      this.input = f.form.controls.sparqlFormKey.value;
      f.form.controls.sparqlFormKey.valueChanges.subscribe(val => {
        this.input = val;
      });
    });
  }

  public executeQuery(): void {
    this.http.postExecuteQuery(this.input, null)
      .subscribe(data => {
        this.showError = false;
        this.errorMessage = '';
        this.columns = data.columns;
        this.values = data.values;
      },
      error => {
        this.showError = true;
        this.errorMessage = error.error;
      });
  }

  public createForm(): IForm {

    return {
      components: [
        {
          key: 'sparqlFormKey',
          label: 'Query',
          type: 'codeEditor',
          defaultValue: '',
          readOnly: false,
          hidden: false,
          codeEditorOptions: {
            mode: 'sparql',
          }
        }
      ]
    };

  }


}
