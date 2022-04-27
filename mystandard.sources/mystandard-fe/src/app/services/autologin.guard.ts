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
import { Injectable } from "@angular/core";
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, CanActivate, ActivatedRoute, Router } from "@angular/router";
import { Observable, of } from "rxjs";
import { mergeMap } from "rxjs/operators";
import { environment } from "src/environments/environment";
import { AutologinService } from "./autologin.service";


@Injectable({
    providedIn: 'root'
})
export class AutoLoginGuard implements CanActivate {

    constructor(private activatedRoute: ActivatedRoute,
                private autologinService: AutologinService) {

    }

    canActivate(): Promise<boolean> {
        return new Promise<boolean>(resolve => {
            this.activatedRoute.queryParams.subscribe(params => {
                if(params.autologin) {
                    const location = window.location.href;
                    const redirectUrl = location.split('?');
                    this.autologinService.setAutologinDone({autologinDone: true, callBackUrl: redirectUrl[0]});
                    window.open(`${environment.baseApiUrl}saml/login?callbackUrl=${redirectUrl[0]}`, "_self");

                }
                resolve(true);
            })
            
        })
        
    }


}