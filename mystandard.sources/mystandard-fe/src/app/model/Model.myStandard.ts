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
export interface GenericEntityModel {
  entityId: number;
  code: number;
  version: number;
  name: string;
  description?: string;
  state: 'inserito' | 'pubblicato';
  attachments: any[];
  relationships:
    { 'Def./Spec.': RelationshipModel[] , 'Usa': RelationshipModel[] } |
    { 'Def./Spec.': RelationshipModel[] } |
    { 'Usa': RelationshipModel[] };
}

export interface RelationshipModel {
  relationshipId: number;
  entity: 'API' | 'Processi';
  code: number;
  version: number;
  name: string;
  state: 'inserito' | 'pubblicato';
}

export interface OrganizationModel extends GenericEntityModel {
  logo: object;
  legalForm: string;
  address: string;
  email: string;
  telephoneNumber: string;
  website: string;
}

export interface StructuredEntityModel extends GenericEntityModel {
  format: string;
  traceStructure: string;
}

export type EntityType =
  { name: 'Entità Generica', value: 'entitaGenerica', index: 0 } |
  { name: 'Enti', value: 'ente', index: 1 } |
  { name: 'Azienda ICT', value: 'aziendaICT', index: 2 } |
  { name: 'Processi', value: 'processo', index: 3 } |
  { name: 'API', value: 'api', index: 4 };

export interface SearchRequest {
  entityType: EntityType;
}

export interface CodeSearchRequest extends SearchRequest {
  code: number;
}

export interface VersionSearchRequest extends CodeSearchRequest {
  version: number;
}

export interface APISearchRequest extends SearchRequest {
  technologyType?: string;
  expositionType?: string;
  exchangeMode?: string;
}

export enum SpecialPropertiesNumber {
  entityId = 1,
  code = 2,
  version = 3,
}

export interface AlertType {
  topSpace?: boolean;
  message: string;
  type: 'danger' | 'success' | 'info' | 'warning';
  autoClosable: boolean;
  closeInMillis?: number;
}

export enum OperationType {
  ADD = 'ADD',
  REMOVE = 'REMOVE',
  MODIFY = 'MODIFY',
  NOTHING = 'NOTHING'
}
