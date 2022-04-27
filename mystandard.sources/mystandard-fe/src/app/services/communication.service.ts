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
import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { EntityType } from '../model/Model.myStandard';
import { UtilsService } from './utils.service';
import * as _ from 'lodash';

/**
 *
 *
 * @export
 * @class CommunicationService
 */
@Injectable({providedIn: 'root'})
export class CommunicationService {
  private entityTypes: EntityType[];
  private isDetailSelected = new Subject();
  private isDetailSelectedStatus: boolean = null;
  private isLogged = new Subject();
  private isLoggedStatus: boolean = null;
  private prevEntityDetailsStatus: { type: EntityType, code: number, version: number, name: string, state: string } = null;
  private isModifyEnabled = new Subject();
  private isModifyEnabledStatus: boolean = null;
  private isFetchingData = new BehaviorSubject(false);
  private resultMessage: {mustShow, message} = null;
  private actualRoute: {domain: string, subDomain: string, code: string, version: string, idx: string};
  private staticPaths: any;
  private labelsMap: any;
  private labelsMapSubj = new Subject();
  private baseApiPath = '';
  private baseApiPathSubj = new Subject();
  private freeSearchType = '';
  private entityState = '';
  private entityStateSubj = new Subject();
  private entityName = '';
  private entityNameSubj = new Subject();
  private irisMap: {};
  private validationStatus = { fields: true, attachments: true };
  private validationStatusSubj = new Subject();
  private ipasFilterMap = {};
  private ipasFilterMapSubj = new Subject();
  private ipaSubj = new Subject();
  private entitiesCreatingIpasSubj = new Subject();
  private entitiesCreatingIpas = '';

  /**
   * Creates an instance of CommunicationService.
   * @memberof CommunicationService
   */
  public constructor(
    private utils: UtilsService,
  ) {

    this.resultMessage = {
      mustShow: false,
      message: null,
    };
    this.actualRoute = {domain: '', subDomain: '', code: '', version: '', idx: ''};
    this.staticPaths = [];
    this.labelsMap = {
      staticPaths: {},
      domains: {},
      subDomains: {},
    };
    this.irisMap = {};
    this.ipasFilterMap = {};
  }

  public getIpasFilterMap(): any {
    return this.ipasFilterMap;
  }

  public isStaticDomain(): boolean {
    const actualRoute = '/' + this.actualRoute.domain;
    let isStaticDomain = false;
    _.forEach(this.staticPaths, path => {
      if (path.url === actualRoute) { isStaticDomain = true; }
    });
    return isStaticDomain;
  }

  public getActualDomainLabel(): string {
    if (this.isStaticDomain()) { return this.getPathLabel('staticPaths', this.actualRoute.domain) };
    return this.getPathLabel('domains', this.actualRoute.domain);
  }

  public getActualSubDomainLabel(): string {
    return this.getPathLabel('subDomains', this.actualRoute.subDomain);
  }

  public getPathLabel(type: string, path: string): string {
    return this.labelsMap[type][path];
  }

  public setBaseApiPath(data: any[]): void {
    if (data.length > 0) {
      const path = this.utils.getSplittedPath(data[0].url);
      this.baseApiPath = path[0];
      this.baseApiPathSubj.next(this.baseApiPath);
    }
  }

  public getBaseApiPath(): string {
    return this.baseApiPath;
  }

  public getBaseApiPathBySubscription(): Subject<any> {
    return this.baseApiPathSubj;
  }

  public getLabelsMap(): any {
    return this.labelsMap;
  }

  public getLabelsMapBySubscription(): Subject<any> {
    return this.labelsMapSubj;
  }

  public setLabelsMapBySubscription(state: any): void {
    this.labelsMapSubj.next(state);
  }

  public getPaths(): any {
    const paths = [];
    _.forEach(Object.keys(this.labelsMap.subDomains), k => {
      paths.push({
        value: k,
        name: this.labelsMap.subDomains[k],
      });
    });
    return paths;
  }

  public getIriFromSubDomain(subDomain: string): string {
    return this.irisMap[subDomain];
  }

  public createMaps(paths: any[]): any {

    _.forEach(paths, path => {

      if (!path.items) {

        this.labelsMap.staticPaths[path.url.replace('/', '')] = path.label;

      } else {

        _.forEach(path.items, item => {

          // Domains mapping
          const urlAsArray = item.url.split('/');
          urlAsArray.splice(0, 1);
          const domainPath = urlAsArray[1] ? urlAsArray[1] : null;
          const domainLabel = domainPath ? path.label : null;
          if (domainPath !== undefined) {
            this.labelsMap.domains[domainPath] = domainLabel;
            if (!this.ipasFilterMap[domainPath]) { this.ipasFilterMap[domainPath] = []; }
          }

          const subDomainPath = urlAsArray[2] ? urlAsArray[2] : null;
          const subDomainLabel = subDomainPath ? item.label : null;

          if (subDomainPath !== undefined) {
            this.labelsMap.subDomains[subDomainPath] = subDomainLabel;
            this.irisMap[subDomainPath] = item.entityIRI;
            if (item.ipaFilter) { this.ipasFilterMap[domainPath].push(subDomainPath); }
          }
        });
      }
    });

    this.setLabelsMapBySubscription(this.getLabelsMap);

  }

  public setFreeSearchType(input: string): void {
    this.freeSearchType = input;
  }

  public getFreeSearchType(): string {
    return this.freeSearchType;
  }

  public getActualRoute(): any {
    return this.actualRoute;
  }

  public setStaticPaths(staticPaths: any): void {
    this.staticPaths = staticPaths;
  }

  public getStaticPaths(): any {
    return this.staticPaths;
  }

  public setActualRouteDomain(domain): void {
    this.actualRoute.domain = domain;
  }

  public setActualRouteSubDomain(subDomain): void {
    this.actualRoute.subDomain = subDomain;
  }

  /**
   *
   *
   * @returns {Subject<any>}
   * @memberof CommunicationService
   */
  public getDetailStatusBySubscription(): Subject<any> {
    return this.isDetailSelected;
  }

  /**
   *
   *
   * @returns {boolean}
   * @memberof CommunicationService
   */
  public getDetailStatus(): boolean {
    return this.isDetailSelectedStatus;
  }

  /**
   *
   *
   * @param {boolean} status
   * @memberof CommunicationService
   */
  public setDetailStatus(status: boolean): void {
      this.isDetailSelected.next(status);
    }

  /**
   *
   *
   * @returns {Subject<any>}
   * @memberof CommunicationService
   */
  public getLoginStatusSubscription(): Subject<any> {
    return this.isLogged;
  }

  /**
   *
   *
   * @returns {boolean}
   * @memberof CommunicationService
   */
  public getLoginStatus(): boolean {
    return this.isLoggedStatus;
  }


  public getValidationStatusSubscription(): Subject<any> {
    return this.validationStatusSubj;
  }

  public setValidationStatus(property: string, value: boolean): void {
    const actualValidationStatus = this.validationStatus;
    actualValidationStatus[property] = value;
    this.validationStatusSubj.next(actualValidationStatus);
  }

  /**
   *
   *
   * @param {boolean} status
   * @memberof CommunicationService
   */
  public setLoginStatus(status: boolean): void {
    this.isLoggedStatus = status;
    this.isLogged.next(status);
  }

  /**
   *
   *
   * @returns {Subject<any>}
   * @memberof CommunicationService
   */
  public getLoginStatusBySubscription(): Subject<any> {
    return this.isLogged;
  }

  public setEntityName(name: string): void {
    this.entityNameSubj.next(name);
    this.entityName = name;
  }

  public getEntityNameBySubscription(): Subject<any> {
    return this.entityNameSubj;
  }

  public registerEntityState(state: string): void {
    this.entityState = state;
    this.entityStateSubj.next(state);
  }

  public getEntityState(): string {
    return this.entityState;
  }

  public getEntityStateBySubscription(): Subject<any> {
    return this.entityStateSubj;
  }

  /**
   *
   *
   * @returns {Subject<any>}
   * @memberof CommunicationService
   */
  public getModifyEnabledStatusBySubscription(): Subject<any> {
    return this.isModifyEnabled;
  }

  /**
   *
   *
   * @returns {boolean}
   * @memberof CommunicationService
   */
  public getModifyEnabledStatus(): boolean {
    return this.isModifyEnabledStatus;
  }

  /**
   *
   *
   * @param {boolean} status
   * @memberof CommunicationService
   */
  public setModifyEnabledStatus(status: boolean): void{
    this.isModifyEnabled.next(status);
    this.isModifyEnabledStatus = status;
  }

  /**
   *
   *
   * @returns {Subject<any>}
   * @memberof CommunicationService
   */
  public getFetchingDataStatus(): Subject<any> {
    return this.isFetchingData;
  }

  /**
   *
   *
   * @param {boolean} status
   * @memberof CommunicationService
   */
  public setFetchingDataStatus(status: boolean): void {
    this.isFetchingData.next(status);
  }

  public setResultMessage(mustShow: boolean, message: any): void {
    this.resultMessage = {
      mustShow,
      message
    }
  }

  public getResultMessage(): any {
    return this.resultMessage;
  }

  public setIpa(ipa: string): void {
    this.ipaSubj.next(ipa);
  }

  public getIpaBySubscription(): Subject<any> {
    return this.ipaSubj;
  }

  public setEntitiesCreatingIpas(entitiesMap: any): void {
    this.entitiesCreatingIpasSubj.next(entitiesMap);
    this.entitiesCreatingIpas = entitiesMap;
  }

  public getEntitiesCreatingIpasBySubscription(): Subject<any> {
    return this.entitiesCreatingIpasSubj;
  }

  public getEntitiesCreatingIpas(): any {
    return this.entitiesCreatingIpas;
  }

  public setWarningMessageAlertForUserWithNoRoles(): void {
    this.setResultMessage(true, { message: 'Non hai ruoli per MyStandard, navigherai in modalità visualizzazione', type: 'warning', autoClosable: true, closeInMillis: 2000 });
  }

}
