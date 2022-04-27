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
import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { CommunicationService } from 'src/app/services/communication.service';
import { HttpService } from 'src/app/services/http.service';
import { environment } from 'src/environments/environment';
import {
  DataTableService,
  MrfFormComponent,
  IForm
} from '@eng/morfeo';

@Component({
  selector: 'app-entity-selector-dialog',
  templateUrl: './entity-selector-dialog.component.html',
  styleUrls: ['./entity-selector-dialog.component.scss']
})
export class EntitySelectorDialogComponent implements OnInit {
  public dataReference: any;
  public tableOptions= {
    filterButtonsNoIcon: true,
  };
  public form: IForm;
  public result: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) data,
    private dialogRef: MatDialogRef<EntitySelectorDialogComponent>,
    public http: HttpService,
    public alertsCenterService: AlertsCenterService,
    public commService: CommunicationService,
    public tableService: DataTableService,
  ) {
    this.dataReference = data;
  }

  public ngOnInit(): void {
    this.commService.setFetchingDataStatus(false);

    this.http.getRelationshipsNewEntity('Generale', this.dataReference.selectedEntity.key).subscribe(result => {
      this.result = result;
      const form = this.createForm(this.dataReference.selectedEntity.key, false);
      form.components[0].data.values = result;
      this.tableService.setCallback(['semanticQueryDatatable', 'tools', 'edit'], (row) => {
        this.dataReference.selectedEntity._targetIndividualsIRI = row._targetIndividualsIRI;
        this.dialogRef.close();
      });
      this.form = form;
    }, (_error) => {
      const errorMessage = environment.showErrors ? _error.error : '';
      this.alertsCenterService.showAlert({
        message: 'Si è verificato un errore nel caricamento della risorsa' + errorMessage,
        type: 'danger',
        autoClosable: true
      });
      this.commService.setFetchingDataStatus(false);
    });


  }

  private createForm(entity: string, hasPagination): IForm {
    const url = `${environment.baseApiUrl}relazioni/range/Generale/${entity}` +
      '?pageNum=$pageNum&pageSize=$pageSize&sortField=$sortField&sortDirection=$sortDirection&$filter';

    let newForm: IForm;
    newForm = {
      components: [
        {
          type: 'dataTable',
          key: 'semanticQueryDatatable',
          dataSrc: 'url',
          data: {
            url,
            columns: [
                  { value: 'CodiceEntita', label: 'Codice', sortable: true },
                  { value: 'Versione', label: 'Versione', sortable: true },
                  { value: 'name', label: 'Nome', sortable: true },
                  { value: 'Stato', label: 'Stato', sortable: true },
                  { value: 'tools', label: 'Azioni',
                    buttons: [
                      { label: '', icon: 'add', action: 'edit', color: 'primary', style: 'icon' }
                    ]
                  },
            ],
            pagination: { sizeOptions: [ 10, 20, 50 ] },
            filter: { components: this.generateFilters() },
          },
          input: true,
        }
      ]
    }
    return newForm;
  }

  private generateFilters(): any[] {

    const components = [
      {
      type: 'columns',
      columns: [
          {
          components: [{ key: 'CodiceEntita', type: 'textfield', label: 'Codice' }],
          width: 6
          },
          {
          components: [{ key: 'Versione', type: 'textfield', label: 'Versione' }],
          width: 2
          }
        ],
      },
      {
      type: 'columns',
      columns: [
          {
          components: [{ key: 'name', type: 'textfield', label: 'Nome' }],
          width: 6
          },
          {
          components: [
              {
                label: 'Stato', key: 'Stato', type: 'select',
                data: { values: [
                  { label: 'Inserito', value: 'Inserito' },
                  { label: 'Pubblicato', value: 'Pubblicato' },
                  { label: '', value: '' }
                ]},
              },
            ],
          width: 2
          }
        ]
      }
    ];
    return components;

  }

  onCloseCLick(): void {
    this.dialogRef.close();
  }

}
