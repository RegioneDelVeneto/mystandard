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
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';

import { Injectable } from '@angular/core';
import { throwError } from 'rxjs';
import { Observable } from 'rxjs/internal/Observable';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable()
export class AuthenticationErrorInterceptor implements HttpInterceptor {

  constructor(
  ){ }

  public intercept(request: HttpRequest<any>, next: HttpHandler): Observable <HttpEvent<any>> {

    return next.handle(request)
      .pipe(
        catchError(error => {
          if (error.status === 401 || error.status === 403) {
            window.open(`${environment.baseApiUrl}saml/login?callbackUrl=${location}`, "_self");
          }
          return throwError(error);
        })
      );
  }
}
