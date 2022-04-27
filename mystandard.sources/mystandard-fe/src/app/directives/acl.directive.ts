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
/**
 * 
 * @author Regione del Veneto
 * @version 1.0.0
 * @since 22nd September 2017
 * @export
 * @class ACLDirective
 */
import { Directive, Input, ViewContainerRef, TemplateRef, OnChanges } from '@angular/core';
import { AuthenticationService } from '../services/authentication.service';

export interface ACLDirectiveConfiguration {
    roles: Array<string>;
}

@Directive({ selector: '[mysAcl]' })
export class ACLDirective implements OnChanges {

    /**
     *
     *
     * @private
     * @type {Array<string>}
     * @memberof ACLDirective
     */
    private roles: Array<string> = [];

    /**
     * 
     * 
     * @memberof ACLDirective
     */
    @Input()
    public set mysAcl(obj: ACLDirectiveConfiguration) {
        this.roles = obj.roles;
        this.verifyAcl();
    }

    private profile: any;

    /**
     *
     *
     * @memberof ACLDirective
     */
    public ngOnChanges(): void {
        this.verifyAcl();
    }

    /**
     * 
     * 
     * @author Regione del Veneto
     * @private
     * @memberof ACLDirective
     */
    private verifyAcl() {

        let isAuthorized = false;

        if (!this.profile) {
            this.viewContainer.clear();
            return;
        }

        isAuthorized = !!this.profile.roles.find(role => this.roles.includes(role.toUpperCase()));

        if (isAuthorized) {
            this.viewContainer.clear();
            this.viewContainer.createEmbeddedView(this.templateRef);
        }
        else {
            this.viewContainer.clear();
        }
    }

    /**
     * Creates an instance of ACLDirective.
     * @author Regione del Veneto
     * @param {TemplateRef<any>} templateRef
     * @param {ViewContainerRef} viewContainer
     * @param {UserService} userService
     * @memberof ACLDirective
     */
    constructor(
        private templateRef: TemplateRef<any>,
        private viewContainer: ViewContainerRef,
        private authService: AuthenticationService,
    ) {
        this.authService.getProfile().then(profile => {
            this.profile = profile;
            this.verifyAcl();
        });
    }
}
