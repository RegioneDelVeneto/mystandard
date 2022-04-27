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
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { DashboardComponent } from './components/dashboard/dashboard.component';
import { DetailComponent } from './components/detail/detail.component';
import { SessionComponent } from './components/session/session.component';
import { SearchComponent } from './components/search/search.component';
import { SummaryComponent } from './components/summary/summary.component';
import { SearchDetailComponent } from './components/search/semantic-search/search-detail/search-detail.component';
import { SearchExecuteComponent } from './components/search/semantic-search/search-execute/search-execute.component';
import { AutoLoginComponent } from './components/autologin/autologin.component';
import { AutoLoginGuard } from './services/autologin.guard';

const routes: Routes = [
  { path: '', component: AutoLoginComponent, pathMatch: 'full', canActivate: [AutoLoginGuard]},

  { path: 'session', component: SessionComponent, canActivate: [AutoLoginGuard] },

  { path: 'summary', component: SummaryComponent, canActivate: [AutoLoginGuard]},

  { path: 'search/:queryType', component: SearchComponent, canActivate: [AutoLoginGuard] },
  { path: 'search/:queryType/:id', component: SearchDetailComponent,canActivate: [AutoLoginGuard]  },
  { path: 'search/:queryType/:id/execute', component: SearchExecuteComponent,canActivate: [AutoLoginGuard]  },
  { path: 'search/:queryType/:newQuery', component: SearchDetailComponent,canActivate: [AutoLoginGuard]  },

  { path: 'entities/:domain/:subDomain', component: DashboardComponent,canActivate: [AutoLoginGuard]  },
  { path: 'entities/:domain/:subDomain/:code/:version', component: DetailComponent,canActivate: [AutoLoginGuard]  },
  { path: 'entities/:domain/:subDomain/:newEntity', component: DetailComponent,canActivate: [AutoLoginGuard]  }
];
/**
 *
 *
 * @export
 * @class AppRoutingModule
 */
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
