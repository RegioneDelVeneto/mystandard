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

import { Observable } from 'rxjs';
import { Subject } from 'rxjs';
import { Subscription } from 'rxjs';

import { AlertType } from '../model/Model.myStandard';

/**
 *
 *
 * @export
 * @class AlertsCenterService
 */
@Injectable({providedIn: 'root'})
export class AlertsCenterService {

    
    private emitter: Subject<AlertType> = new Subject<AlertType>();

    private alerts: Observable<AlertType> = this.emitter.asObservable();

    /**
     *
     *
     * @param {(message: AlertType) => void} callback
     * @returns {Subscription}
     * @memberof AlertsCenterService
     */
    public subscribe(callback: (message: AlertType) => void): Subscription {
        return this.alerts.subscribe(callback);
    }

    /**
     *
     *
     * @param {AlertType} alert
     * @memberof AlertsCenterService
     */
    public showAlert(alert: AlertType): void{
        this.emitter.next(alert);
    }

    public errorsHandler (error): string[] {
        if(error.status===412 && error.error.errors) {
            let array = []
            error.error.errors.forEach(element => {
                let cod = Object.keys(element)[0];
                let fieldName = this.mapperError(cod);
                array.push(fieldName===""? "" : fieldName+": " + " " +element[cod]);
            });
            return array;
        }
        else return []
    }

    public mapperError (key) {
        switch(key) {
            case "mystd_hasAddress_organizationPostalCode":
                return "CAP";
            case "mystd_hasLogo_URL_image":
                return "URL Logo";
            case "mystd_hasEmail_organization_email_address":
                return "Email";
            case "mystd_hasImage_URL_image":
                return "URL Immagine";
            case "mystd_hasWebSite_URL_website":
                return "URL Sito Web";
            case "mystd_taxCode":
                return "Codice Fiscale";
            default:
                return ""
        }
    }

}