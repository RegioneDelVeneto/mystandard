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

export interface AutologinObject {
    autologinDone: boolean,
    callBackUrl: string
}

@Injectable({ providedIn: 'root' })
export class AutologinService {

    private static autoLoginInfoKey = 'autoLoginInfo';
    private static usedLoginInfoKey = 'usedLoginInfo';
    private static showFirstAlertAfterUse = false;
    private static hasMultipleRole = false;
    private static firstTripForMultiTenant = true;

    public counter = 0;
    
    constructor(){

    }

    public setAutologinDone(done: AutologinObject): void {
        localStorage.setItem(AutologinService.autoLoginInfoKey, JSON.stringify(done));
        AutologinService.showFirstAlertAfterUse = true;
    }

    public getAutologinDone(): AutologinObject {
        const autoLoginObject = JSON.parse(localStorage.getItem(AutologinService.autoLoginInfoKey));
        if(autoLoginObject)
            return autoLoginObject;
        else
            return {
                autologinDone: false,
                callBackUrl: null
            } 
    }

    public navigateTo(done: AutologinObject): void {
        const url = done.callBackUrl;
        localStorage.setItem(AutologinService.usedLoginInfoKey, localStorage.getItem(AutologinService.autoLoginInfoKey));
        localStorage.removeItem(AutologinService.autoLoginInfoKey);
        window.location.replace(url);
    }

    public cleanLocalStorage() {
        localStorage.removeItem(AutologinService.autoLoginInfoKey);
        localStorage.removeItem(AutologinService.usedLoginInfoKey);
    }

    public hasDoneAutologinForFirstAlert() {
        if(AutologinService.showFirstAlertAfterUse) {
            AutologinService.showFirstAlertAfterUse = false;
            return true;
        }
        else {
            return false;
        }
    }

    public hasMultipleRole(): boolean {
        return AutologinService.hasMultipleRole;
    }

    public setHasMultipleRole(toSet: boolean): void {
        AutologinService.hasMultipleRole = toSet;
    }

    public isFirstTripForMultinenant(): boolean {
        return AutologinService.firstTripForMultiTenant;
    }

    public setFirstTripForMultinenant(toSet: boolean): void {
        AutologinService.firstTripForMultiTenant = toSet;
    }

    public hasUsedAlreadyAutologin(): AutologinObject{
        const autoLoginObject = JSON.parse(localStorage.getItem(AutologinService.usedLoginInfoKey));
        if(autoLoginObject)
            return autoLoginObject;
        else
            return {
                autologinDone: false,
                callBackUrl: null
            } 
    }
}