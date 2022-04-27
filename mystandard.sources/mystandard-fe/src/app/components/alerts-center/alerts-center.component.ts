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
import { Component, OnInit, OnDestroy, ViewEncapsulation } from '@angular/core';
import { AlertsCenterService } from '../../services/alerts-center.service';
import {
  animate,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';

import { Subscription } from 'rxjs';

import { AlertType } from 'src/app/model/Model.myStandard';

/**
 * 
 * @export
 * @class AlertsCenterComponent
 * @implements {OnInit}
 * @implements {OnDestroy}
 */
@Component({
  selector: 'app-alerts-center',
  templateUrl: './alerts-center.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./alerts-center.component.scss'],
  animations: [
    trigger('animation', [
        state('fancy', style([{ transform: 'translateX(0)' }, { transform: 'translateY(0)' }, { opacity: 1 }, { maxHeight: 300 }])),
        transition('void => fancy', [
            style({ opacity: 0, maxHeight: 0, transform: 'translateY(-100%)' }),
            animate('300ms ease-in-out')
        ]),
        transition('fancy => void', [
            animate('300ms ease-in-out', style({ transform: 'translateX(100%)', height: 0, opacity: 0 }))
        ])
    ])
]
})
export class AlertsCenterComponent implements OnInit, OnDestroy {

  /**
   *
   *
   * @memberof AlertsCenterComponent
   */
  public hasTopSpace = false;

  /**
   *
   *
   * @private
   * @type {Subscription}
   * @memberof AlertsCenterComponent
   */
  private alertsSubscription: Subscription;

  /**
   *
   *
   * @type {AlertType[]}
   * @memberof AlertsCenterComponent
   */
  public alerts: AlertType[] = [];
  alertType: string;

  /**
   * 
   * @param alertsCenterService 
   */
  constructor( private alertsCenterService: AlertsCenterService ) {
  }

  /**
   *
   *
   * @memberof AlertsCenterComponent
   */
  public ngOnInit(): void {
      this.alertsSubscription = this.alertsCenterService.subscribe((alert: AlertType) => {
          this.hasTopSpace = alert.topSpace;
          this.alerts.unshift(alert);
          this.alertType = alert.type;
          if (alert.autoClosable) {
              setTimeout(() => {
                  this.closeAlert(alert);
              }, alert.closeInMillis ? alert.closeInMillis : 5000);
          }
      });
  }

  /**
   *
   *
   * @memberof AlertsCenterComponent
   */
  public ngOnDestroy(): void {
      if (this.alertsSubscription) {
        this.alertsSubscription.unsubscribe();
      }
  }

  /**
   *
   *
   * @param {AlertType} alert
   * @memberof AlertsCenterComponent
   */
  public closeAlert(alert: AlertType): void {
      const index = this.alerts.indexOf(alert);
      if (index !== -1) {
        this.alerts.splice(index, 1);
      }
  }
}
