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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { CommunicationService } from 'src/app/services/communication.service';
import { HttpService } from 'src/app/services/http.service';
import { environment } from 'src/environments/environment';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { EntityEliminationDialogComponent } from './entity-elimination-dialog/entity-elimination-dialog.component';
@Component({
  selector: 'app-semantic-search',
  templateUrl: './semantic-search.component.html',
  styleUrls: ['./semantic-search.component.scss']
})
export class SemanticSearchComponent implements OnInit, OnDestroy {

  public dataSource: {name: string, description: string}[];
  public tableColumns: string[];
  public rowClicked: number;
  public isOpen: boolean;
  public triggerOrigin: any;
  public totalRecords: number;
  public isLogged = false;
  public isLoggedSub: any;

  public pageNum = 0;
  public pageSize = 10;

  public filter = {
    value: {
      name: '',
      description: '',
    },
    expanded: false,
  };

  constructor(
    public http: HttpService,
    public commService: CommunicationService,
    public alertsCenterService: AlertsCenterService,
    private router: Router,
    private dialog: MatDialog,
  ) {
    this.tableColumns = ['name', 'description', 'actions'];
    this.isLogged = this.commService.getLoginStatus();
    this.isLoggedSub = this.commService.getLoginStatusBySubscription().subscribe(result => {
      this.isLogged = result;
    });
   }

  ngOnInit(): void {
    this.getQueries();
  }

  ngOnDestroy(): void {
    this.isLoggedSub.unsubscribe();
  }

  public new(): void {
    this.router.navigate(['/', 'search', 'semquery', 'newQuery']);
  }

  public getPage($event): void{
    this.pageSize = $event.pageSize;
    this.pageNum = $event.pageIndex;
    this.getQueries();
  }

  public onActionClick(i: number, trigger: any): void {
    this.isOpen = !this.isOpen;
    this.triggerOrigin = trigger;
  }

  public executeQuery(id: string): void {
    this.router.navigate(['/search/semquery', id, 'execute']);
  }

  public modifyQuery(id: string): void {
    this.router.navigate(['/search/semquery/', id]);
  }

  public deleteQuery(id: string): void {
    this.http.deleteQuery(id).subscribe( () => {
      this.getQueries();
    }, (error) => {
      console.log(error);
    });
  }

  private getQueries(): void {
    this.http.getQueries(
        this.pageNum,
        this.pageSize,
        this.filter.value.name,
        this.filter.value.description)
      .subscribe(response => {
        this.dataSource = response.result.records;
        this.totalRecords = response.result.pagination.totalRecords;
        const messageObj = this.commService.getResultMessage();
        if (messageObj.mustShow && messageObj.message.origin === 'query') {
          const message = messageObj.message.message;
          this.alertsCenterService.showAlert({ message, type: 'success', autoClosable: true });
          this.commService.setResultMessage(false, null);
        }
      }, (_error: any) => {
        const errorMessage = environment.showErrors ? JSON.stringify(_error.error) : '';
        this.alertsCenterService.showAlert({
          message: 'Si è verificato un errore nel caricamento dellef query' + errorMessage, type: 'danger', autoClosable: true
        });
      });
  }

  public openConfirmationDialog(id: string): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.autoFocus = true;
    dialogConfig.height = '20vh';
    dialogConfig.minHeight = '200px';
    dialogConfig.width = '40vw';
    dialogConfig.minWidth = '500px';
    dialogConfig.disableClose = false;
    const dialogRef = this.dialog.open(EntityEliminationDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe( result => {
      if (!!result && result === 'confirm') {
        this.deleteQuery(id);
      }
    });
  }

  public onSearchClick(): void {
    this.pageNum = 0;
    this.pageSize = 10;
    this.getQueries();
  }

  public onCancelSearchClick(): void {
    this.pageNum = 0;
    this.pageSize = 10;
    this.filter.value.name = '';
    this.filter.value.description = '';
    this.getQueries();
  }

}
