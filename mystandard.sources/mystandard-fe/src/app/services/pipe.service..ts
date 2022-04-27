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
import * as _ from 'lodash';

/**
 *
 *
 * @export
 * @class PipeService
 */
@Injectable({providedIn: 'root'})
export class PipeService {

  /**
   *
   *
   * @param {*} input
   * @returns {*}
   * @memberof PipeService
   */
  public stringifyNumbers(input: any): any {
    _.forEach(input, i => {
      _.forEach(Object.keys(i), k => {
        i[k] = typeof i[k] === 'number' ? i[k].toString() : i[k];
      });
    });
    return input;
  }

  /**
   *
   *
   * @param {any[]} input
   * @returns {*}
   * @memberof PipeService
   */
  public testCompleteMissingFields(input: any[]): any {
    _.forEach(input, i => {
      if (i.attachments === undefined) {
        i.attachments = [];
      }
      if (i.relationships === undefined) {
        i.relationships = { 'Def./Spec.': [], Usa: []};
      }
      if (i.type === undefined) {
        i.type = 'Enti';
      }
      if (i.ipaCode === undefined) {
        i.ipaCode = 'ipaCode';
      }
    });
    return input;
  }

}
