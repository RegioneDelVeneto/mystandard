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
import { ComponentPortal } from '@angular/cdk/portal';
import { Directive, ElementRef, OnInit, Input } from '@angular/core';
import { DynamicOverlay } from '../services/dynamic-overlay';
import { OverlayRef } from '@angular/cdk/overlay';

import { Observable } from 'rxjs';

import { LoaderComponent } from '../components/loader/loader.component';

/**
 *
 *
 * @export
 * @class OverlayLoadingDirective
 * @implements {OnInit}
 */
@Directive({
  selector: '[overlayLoading]'
})
export class OverlayLoadingDirective implements OnInit {
  @Input('overlayLoading') toggler: Observable<any>;

  private overlayRef: OverlayRef;

  constructor(
    private host: ElementRef,
    private dynamicOverlay: DynamicOverlay
  ) {}

  public ngOnInit(): void {
    if(!this.overlayRef){
      this.overlayRef = this.dynamicOverlay.createWithDefaultConfig(
        this.host.nativeElement
      );
    }
    

    this.toggler.subscribe(show => {
      if (show) {
        if(this.overlayRef.hasAttached())
          this.overlayRef.detach();
        
        this.overlayRef.attach(new ComponentPortal(LoaderComponent));
      } else {
        this.overlayRef.detach();
      }
    });
  }
}