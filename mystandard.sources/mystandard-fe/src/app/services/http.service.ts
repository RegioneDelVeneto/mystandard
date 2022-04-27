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
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  CodeSearchRequest,
  EntityType,
  SearchRequest,
  SpecialPropertiesNumber,
  VersionSearchRequest,
} from '../model/Model.myStandard';
import { PipeService } from './pipe.service.';
import { CommunicationService } from './communication.service';
import { environment } from './../../environments/environment';

import * as _ from 'lodash';

/**
 *
 *
 * @export
 * @class HttpService
 */
@Injectable({providedIn: 'root'})
export class HttpService {

  private baseApiUrl: string;

  /**
   * Creates an instance of HttpService.
   * @param {HttpClient} http
   * @param {PipeService} pipeService
   * @memberof HttpService
   */
  constructor(
    private http: HttpClient,
    private commService: CommunicationService,
    private pipeService: PipeService) {
     this.baseApiUrl = environment.baseApiUrl;
  }

  public getPaths(): Promise<any> {

    return this.http
      .get(
        this.baseApiUrl + 'menu' + '/',
        { }
      ).pipe(
        map(responseData => {
          return responseData;
        })
      ).toPromise();

  }

  public postEntity(domain: string, subDomain: string, entity: any): Observable<any> {
    const apiUrl = this.baseApiUrl + this.commService.getBaseApiPath() + '/' + domain + '/' + subDomain + '/';
    const headers = new HttpHeaders();
    headers.append("Content-Type", "application/form-data");
    return this.http
      .post(
        apiUrl,
        entity,
        { headers }
      ).pipe(
        map(responseData => {
          return this.pipeService.stringifyNumbers(responseData);
        })
      );
  }

  public putEntity(domain: string, subDomain: any, entity: any): Observable<any> {
    const apiUrl = this.baseApiUrl + this.commService.getBaseApiPath() + '/' + domain + '/' + subDomain + '/';
    return this.http
      .put(
        apiUrl,
        entity,
      ).pipe(
        map(responseData => {
          return this.pipeService.stringifyNumbers(responseData);
        })
      );
  }

  public getEntities(domain: string, subDomain: string): Observable<any> {
    const apiUrl = this.baseApiUrl + this.commService.getBaseApiPath() + '/' + domain + '/' + subDomain + '/';
    return this.http
      .get(apiUrl, {})
      .pipe(
        map(responseData => {
          const tmp = responseData;
          return this.pipeService.stringifyNumbers(responseData);
        })
      );
  }

  /**
   *
   *
   * @param {SearchRequest} searchRequest
   * @returns {Observable<any>}
   * @memberof HttpService
   */
  public getRelationshipsNewEntity(domain: string, entity: any): Observable<any> {
    return this.http
      .get(
        this.baseApiUrl + 'relazioni/range/' + domain + '/' + entity + '/',
      )
      .pipe(
        map(responseData => {
          return this.pipeService.stringifyNumbers(responseData);
        })
      );
  }

  /**
   *
   *
   * @param {SearchRequest} searchRequest
   * @returns {Observable<any>}
   * @memberof HttpService
   */
  public getNewEntity(domain: string, subDomain: string): Observable<any> {
    const apiUrl = this.baseApiUrl + this.commService.getBaseApiPath() +
      '/' + domain + '/' + subDomain + '/' + 'nuovo/';
    return this.http
      .get(
        apiUrl,
        { })
      .pipe(
        map(responseData => {
          return this.pipeService.stringifyNumbers(responseData);
        })
      );
  }

  public postEntityState(obj: any): Observable<any> {

    const apiUrl = this.baseApiUrl + this.commService.getBaseApiPath() + '/' +
      obj.domain + '/' + obj.subDomain + '/' +
      obj.code + '/' + obj.version + '/' +
      'publish/';

    return this.http
      .post(
        apiUrl,
        {}
      ).pipe(
        map(responseData => {
          return this.pipeService.stringifyNumbers(responseData);
        })
      );
  }

  /**
   *
   *
   * @param {VersionSearchRequest} versionSearchRequest
   * @returns {Observable<object>}
   * @memberof HttpService
   */
  public getEntityByVersion(obj: any, clone?: boolean ): Observable<object> {

    const readOnlyString = obj.readOnly ? 'readonly/' : '';
    let apiUrl = this.baseApiUrl + this.commService.getBaseApiPath() + '/' +
      obj.domain + '/' + obj.subDomain + '/' +
      obj.code + '/' + obj.version + '/';
    if(obj.readOnly)
      apiUrl += readOnlyString;
    
    if(clone)
      apiUrl += 'clone';

    return this.http
      .get(apiUrl, {})
      .pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public deleteEntity(obj: any): Observable<object> {

    const apiUrl = this.baseApiUrl + this.commService.getBaseApiPath() + '/' +
      obj.domain + '/' + obj.subDomain + '/' +
      obj.code + '/' + obj.version + '/' +
      'delete';

    return this.http
      .delete(
        apiUrl,
        {}
      )
      .pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public postExecuteQuery(query: string, params: any): Observable<any> {

    const apiUrl = this.baseApiUrl + 'utility' + '/' + 'query' + '/' + 'execute' + '/';

    return this.http
      .post(
        apiUrl,
        {
          query,
          pageSize: params && params.pageSize ? params.pageSize : 10,
          pageNum: params && params.pageNum ? params.pageNum : 0,
        },
      )
      .pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  /**
   *
   *
   * @param {string} attachmentType
   * @param {string} attachmentName
   * @returns {Observable<any>}
   * @memberof HttpService
   */
  public postAttachment(attachmentType: string, attachmentName: string): Observable<any> {
    const params = { attachmentType, attachmentName };
    return this.http
      .post(
        this.baseApiUrl + 'allegati/',
        params,
        {responseType: 'text'}, )
      .pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  /**
   *
   *
   * @param {string} attachmentId
   * @returns {Observable<any>}
   * @memberof HttpService
   */
  public getAttachment(entityId: string, attachmentId: string): Observable<any> {
    return this.http
      .get(
        this.baseApiUrl + 'allegati/' + entityId + '/' + attachmentId + '/',
        {
          observe: 'response',
          responseType: 'blob'
        },)
      .pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  /**
   *
   *
   * @param {string} attachmentId
   * @returns {Observable<any>}
   * @memberof HttpService
   */
  public deleteAttachment(attachmentId: string): Observable<any> {
    const params = { attachmentId };
    return this.http
      .delete(
        this.baseApiUrl + 'allegati/1/' + attachmentId + '/',
        {
          observe: 'response',
          responseType: 'text'
        },)
      .pipe(
        map(responseData => {
          console.log(responseData);
        })
      );
  }

  public getEntitiesFromSearch(
      query: string, offset: number, pageSize: number,
      searchType: string, entityType?: string
    ): Observable<any> {

    const params: any = {
      query,
      offset: offset.toString(),
      size: pageSize.toString(),
      searchType,
    };

    if (entityType !== null) {
      params.entityType = entityType;
    }

    return this.http
      .get(
        this.baseApiUrl + 'search' + '/',
        { params })
      .pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public getVocabulary(type: string): Observable<object> {

    const apiUrl = this.baseApiUrl + 'vocabulary' + '/' + type + '/';

    return this.http
      .get(apiUrl, {})
      .pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public getOwl(): Observable<any> {
    const apiUrl = this.baseApiUrl + 'utility' + '/' + 'export' + '/' + 'rdf' + '/';
    return this.http
    .get(apiUrl, {
      responseType: 'blob'
    })
    .pipe(
      map(responseData => {
        return responseData;
      })
    );
  }

  public getMaxVersion(domain: string, subDomain: string, code: string): Observable<any> {
    const apiUrl = this.baseApiUrl + this.commService.getBaseApiPath() + '/'
      + domain + '/' + subDomain + '/' + code + '/' + 'max';

    return this.http
    .get(apiUrl, {})
    .pipe(
      map(responseData => {
        return responseData;
      })
    );
  }

  public postIpa(ipa: string): Observable<any> {
    const apiUrl = this.baseApiUrl + 'postIPA';
    return this.http
      .post(
        apiUrl,
        ipa
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public getOperations(domain: string, subDomain: string, code: string, version: string): Observable<any> {
    const apiUrl = `${this.baseApiUrl}${this.commService.getBaseApiPath()}/${domain}/${subDomain}/${code}/${version}/operazioni`;
    return this.http
      .get(
        apiUrl
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public postOperation(params: any, note: string): Observable<any> {
    const apiUrl = `${this.baseApiUrl}${this.commService.getBaseApiPath()}/${params.domain}/${params.subDomain}/${params.code}/${params.version}/${params.operation}`;
    return this.http
      .post(
        apiUrl, { note }
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public getFilteredSummary( status: string = "", filters: HttpParams ): Observable<any> {
    const apiUrl = `${this.baseApiUrl}entities/bacheca/${status}`
    return this.http
      .get(
        apiUrl, 
        {params: filters}
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public getQueries(pageNum: number, pageSize: number, name?: string, description?: string, sortField?, sortDirection? ): Observable<any> {
    const apiUrl = `${this.baseApiUrl}query/management`;
    const params: any = {
        pageNum,
        pageSize,
        sortField,
        sortDirection,
    };
    if (name && name !== '') {
      params.name = name;
    }
    if (description && description !== '') {
      params.description = description;
    }
    return this.http
      .get(
        apiUrl, { params }
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public getQuery(id: string): Observable<any> {
    const apiUrl = `${this.baseApiUrl}query/management/${id}`;
    return this.http
      .get(
        apiUrl
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public deleteQuery(id): Observable<any> {
    const apiUrl = `${this.baseApiUrl}query/management/${id}`;
    return this.http
      .delete(
        apiUrl
        ).pipe(
          map(responseData => {
            return responseData;
          })
        );
    }

  public postQuery(params: any): Observable<any> {
    const apiUrl = `${this.baseApiUrl}query/management`;
    return this.http
      .post(
        apiUrl, params
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public putQuery(params: any): Observable<any> {
    const apiUrl = `${this.baseApiUrl}query/management`;
    return this.http
      .put(
        apiUrl, params
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

  public getHistorical(domain: string, entity: string, code: string, version: string): Observable<any> {
    const apiUrl = `${this.baseApiUrl}${this.commService.getBaseApiPath()}/${domain}/${entity}/${code}/${version}/storico`;

    return this.http
      .get(
        apiUrl
      ).pipe(
        map(responseData => {
          return responseData;
        })
      );
  }

}
