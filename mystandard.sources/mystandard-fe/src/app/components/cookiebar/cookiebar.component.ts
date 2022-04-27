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
import { AfterViewInit, Component, Input, ViewEncapsulation } from '@angular/core';

declare var setupCookieBar: any;

@Component({
    selector: 'mys-cookiebar',
    templateUrl: 'cookiebar.component.html',
    encapsulation: ViewEncapsulation.None
})
export class CookiebarComponent implements AfterViewInit {

    private basePath: string = '/';
    
    @Input()
    private imgString: string;

    public cookieDivId = Math.floor(Math.random() * 1000);
    public ipa: string;

    constructor() { 
        if (!this.imgString) {
            this.imgString = `${this.basePath}${'assets/logo-mystandard.png'}`
        }
    }

    public ngAfterViewInit(): void {
        this.injectScript();
    }

    public injectScript(){
        let cookieDiv = <HTMLDivElement>document.getElementById(`cookie-bar-container-${this.cookieDivId}`);
        let script: HTMLScriptElement = document.createElement('script');
        let hidden: string = `?theme=minimal&customize=1&always=1&thirdparty=1&refreshPage=1&showNoConsent=1&remember=180&showPolicyLink=1&privacyPage=${encodeURIComponent('http://myportal-infopbl.regione.veneto.it/myportal/INFOPBL/informative/info_cookies')}&logoUrl=${encodeURIComponent(this.imgString)}`;
        script.innerHTML = '';
        if (!cookieDiv) {
            console.error(`div with id cookie-bar-container-${this.cookieDivId} not found`)
            return;
        }
        cookieDiv.appendChild(script);
        script.src = `${this.basePath}${'assets/libs/cookie-bar/cookiebar-latest.min.js'}${hidden}`;
        script.defer = true;
        script.onload = () => {
            if (!document.getElementById('cookie-bar-prompt')) {
                setupCookieBar();
            }
        }
    }

    public revokeCookieConsent() {
        document.cookie='cookiebar=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/';
        document.cookie='cookiebar-third-party=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/';
        document.cookie='cookiebar-tracking=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/';
        setupCookieBar();
        return false;
    }

}
