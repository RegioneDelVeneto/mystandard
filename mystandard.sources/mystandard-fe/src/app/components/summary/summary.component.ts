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
import { ChangeDetectorRef, Component, ComponentFactoryResolver, OnInit, ViewEncapsulation } from '@angular/core';

import { environment } from '../../../environments/environment';

import { AlertsCenterService } from 'src/app/services/alerts-center.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { CommunicationService } from 'src/app/services/communication.service';
import { HttpService } from 'src/app/services/http.service';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { AutologinService } from 'src/app/services/autologin.service';

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.scss']
})
export class SummaryComponent implements OnInit {

  public title: string;
  public role: string;
  public tabSelected: string;
  public isLogged: boolean = null;

  public summaryOperator: any;
  public summaryTransmit: any;
  public summaryPublish: any;
  public summaryApprove: any;
  public summaryStandard: any;

  public domainFilters = [];
  public typeFilters = [];

  public nameFilter: string = null;
  public domainSelected: string = null;
  public typeSelected: string = null;
  public fieldSorted: string = null;
  public sortDirection: string = "";
  public paginations = {
    APPROVA: {
      size: 10,
      page: 0
    },
    PUBBLICA: {
      size: 10,
      page: 0
    },
    TRASMETTI: {
      size: 10,
      page: 0
    },
    PUBBLICA_COME_STANDARD: {
      size: 10,
      page: 0
    },
    OPERATORE: {
      size: 10,
      page: 0
    }
  }

  public roleSubscription: any;
  public loginStatusSubscription: any;
  public entitiesSubscription: any;

  constructor(
    public alertsCenterService: AlertsCenterService,
    public authService: AuthenticationService,
    private commService: CommunicationService,
    private http: HttpService,
    private ref: ChangeDetectorRef,
    private router: Router,
    private autologinService: AutologinService
  ) { }


  ngOnInit(): void {
    this.loginStatusSubscription = this.authService.getProfileFromServer().subscribe(status => {
      this.isLogged = status;
      if (this.isLogged === false || !this.authService.getHasRoles()) { // la seconda condizione gestisce gli utenti loggati senza ruoli
        if(this.autologinService.getAutologinDone().autologinDone){   //se e' stato usato l'autologin, reindirizza all'url desiderato per l'utente senza ruoli
          this.autologinService.navigateTo(this.autologinService.getAutologinDone());
        }
        else{
          this.router.navigate(['entities/Generale/API']);
        }
        return;
      }
    });
    this.role=this.authService.getRole()
    if(this.role && !this.authService.hasMultiTenant) this.getAllEntities(false)
    this.roleSubscription = this.authService.getRolesBySubscription().subscribe(roles => {
      this.role = roles[0];
      this.getAllEntities(false)
    },(_error) => {
      const errorMessage = environment.showErrors ? _error.error : '';
      //this.commService.setFetchingDataStatus(false);
      this.alertsCenterService.showAlert({ message: 'Si è verificato un errore nel caricamento della query' + errorMessage,  type: 'danger', autoClosable: true });
    })
    if(this.autologinService.getAutologinDone().autologinDone){   //se e' stato usato l'autologin, reindirizza all'url desiderato per l'utente
      this.autologinService.navigateTo(this.autologinService.getAutologinDone());
  }
  }

  public getAllEntities(getFiltered: boolean) {
      if (this.role === 'ROLE_OPERATORE_EN' || this.role === 'ROLE_OPERATORE_EE_LL') {
        this.title = 'Bacheca Operatore'
        this.getEntities();
      } else {
        this.title = 'Bacheca Responsabile di Dominio'
        this.getEntities("APPROVA");
        this.getEntities("PUBBLICA");
        this.getEntities("TRASMETTI");
        if (this.role === 'ROLE_RESPONSABILE_STANDARD' || this.role === 'RESPONSABILE_STANDARD') {
          this.title = 'Bacheca Responsabile Standard'
          this.getEntities("PUBBLICA_COME_STANDARD");
        }
      }
      if(!getFiltered)this.getFilters();
  }

  public getEntities(status: string = undefined) {
    let httpParams = new HttpParams();
    if (this.nameFilter) httpParams = httpParams.set("name", this.nameFilter);
    if (this.domainSelected) httpParams = httpParams.set("domain", this.domainSelected);
    if (this.typeSelected) httpParams = httpParams.set("type", this.typeSelected);
    if (this.paginations[this.tabSelected]) httpParams = httpParams.set("pageNum", this.paginations[this.tabSelected].page + 1);
    else httpParams = httpParams.set("pageNum", '1');
    if (this.paginations[this.tabSelected]) httpParams = httpParams.set("pageSize", this.paginations[this.tabSelected].size);
    else httpParams = httpParams.set("pageSize", '10');
    if(this.sortDirection!=="") {
      httpParams = httpParams.set("sortField", this.fieldSorted);
      httpParams = httpParams.set("sortDirection", this.sortDirection);
    }

    this.entitiesSubscription = this.http.getFilteredSummary(status, httpParams).subscribe(data => {
      if(data.records){
        const domainToRemove="http://server/unset-base/";
        data.records.forEach(element => {
          if(element.DominioBusiness && element.DominioBusiness.includes(domainToRemove)) {
            let domains = element.DominioBusiness.split(",");
            const domainsLength = domains.length;
            element.DominioBusiness = "";
            
            domains.forEach( (dom, index) => {
              if (dom !== domainToRemove) {
                element.DominioBusiness += dom;
                if(index !== domainsLength-2) {  //perche' -2 : -1 perche' l'ultimo indice e' minore di 1 rispetto la length dell'array. Un altro -1 perche' va rimosso domainToRemove
                  element.DominioBusiness += ',';
                }
              }
              
            });
            
          } 
        });
      }

      const messageObj = this.commService.getResultMessage();
      if (messageObj.mustShow && messageObj.message.origin !== 'query') {
        const message = messageObj.message.message;
        this.alertsCenterService.showAlert({ message, type: 'success', autoClosable: true });
        this.commService.setResultMessage(false, null);
      }

      switch (status) {
        case 'APPROVA': {
          this.summaryApprove = data;
          break;
        }
        case 'PUBBLICA': {
          this.summaryPublish = data;
          break;
        }
        case 'TRASMETTI': {
          this.summaryTransmit = data;
          break;
        }
        case 'PUBBLICA_COME_STANDARD': {
          this.summaryStandard = data;
          break;
        }
        default: {
          this.summaryOperator = data
          break;
        }
      }
      //this.commService.setFetchingDataStatus(false);
    },
      error => {
        const errorMessage = environment.showErrors ? error.error : '';
        //this.commService.setFetchingDataStatus(false);
        this.alertsCenterService.showAlert({ message: 'Si è verificato un errore nel caricamento della risorsa' + errorMessage, type: 'danger', autoClosable: true });
      })
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
        if (this.typeFilters.map(item => item.label).includes(item.label) === false) this.typeFilters.push(item)
      }))
    this.typeFilters = this.typeFilters.sort((a,b) => (a.label > b.label) ? 1 : ((b.label > a.label) ? -1 : 0))
  }

  public setDomainTypeFilters() {
    this.typeSelected = null;
    Object.keys(this.domainFilters).forEach(key => {
      if (this.domainFilters[key].domain === this.domainSelected) {
        this.typeFilters = this.domainFilters[key].items
      }
    });
    this.typeFilters = this.typeFilters.sort((a,b) => (a.label > b.label) ? 1 : ((b.label > a.label) ? -1 : 0))
  }

  public getFilteredEntities() {
    //this.commService.setFetchingDataStatus(true);
    this.getAllEntities(true)
  }

  public resetFields() {
    this.nameFilter = null;
    this.domainSelected = null;
    this.typeSelected = null;
    this.typeFilters = [];
    this.populateTypeFilters();
  }

  public tabChanged(tabChangeEvent: MatTabChangeEvent): void {
    switch (tabChangeEvent.index) {
      case 0: {
        this.tabSelected = 'APPROVA'
        break;
      }
      case 1: {
        this.tabSelected = 'PUBBLICA'
        break;
      }
      case 2: {
        this.tabSelected = 'TRASMETTI'
        break;
      }
      case 3: {
        this.tabSelected = 'PUBBLICA_COME_STANDARD'
        break;
      }
      default: {
        this.tabSelected = ''
        break;
      }
    }
  }

  public handlePageEvent(event) {
    this.paginations[this.tabSelected].size = event.pageSize
    this.paginations[this.tabSelected].page = event.pageIndex

    this.getEntities(this.tabSelected)
  }

  public handleSort(event) {
    this.fieldSorted = event.active;
    this.sortDirection = event.direction;
    this.getEntities(this.tabSelected)
  }

  public ngOnDestroy(): void {
    
    if (this.roleSubscription) { this.roleSubscription.unsubscribe(); }
    if (this.loginStatusSubscription) { this.loginStatusSubscription.unsubscribe(); }
    if (this.entitiesSubscription) { this.entitiesSubscription.unsubscribe(); }
  }
}