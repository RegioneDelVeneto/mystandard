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
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HashLocationStrategy, LocationStrategy, CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatListModule } from '@angular/material/list';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSortModule } from '@angular/material/sort';
import { MatToolbarModule, } from '@angular/material/toolbar';
import { MatTabsModule } from '@angular/material/tabs';

import { NgModule } from '@angular/core';
import { OverlayModule } from '@angular/cdk/overlay';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import { MrfFormModule } from '@eng/morfeo';

import { AlertsCenterComponent } from './components/alerts-center/alerts-center.component';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { AttachmentsComponent } from './components/detail/attachments/attachments.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { DetailComponent } from './components/detail/detail.component';
import { FieldsComponent } from './components/detail/fields/fields.component';
import { LoaderComponent } from './components/loader/loader.component';
import { OverlayLoadingDirective } from './directives/overlay-loading.directive';
import { ACLDirective } from './directives//acl.directive';
import { TabsComponent } from './components/detail/tabs/tabs.component';
import { DialogComponent } from './components/detail/tabs/dialog-component/dialog.component';
import { SearchComponent } from './components/search/search.component';
import { ConfirmDialogComponent } from './components/detail/confirm-dialog/confirm-dialog.component';
import { FreeSearchComponent } from './components/search/free-search/free-search.component';
import { SparqlSearchComponent } from './components/search/sparql-search/sparql-search.component';
import { SemanticSearchComponent } from './components/search/semantic-search/semantic-search.component';
import { DynamicDialogComponent } from './components/detail/fields/dynamic-dialog-component/dynamic-dialog.component';
import { AuthenticationErrorInterceptor } from './services/authentication-error-interceptor.service';
import { PaginatorInterceptor } from './services/paginator-interceptor.service';
import { SessionComponent } from './components/session/session.component';
import { SessionDialogComponent } from './components/session/session-dialog/session-dialog.component';
import { AttachmentsTableComponent } from './components/detail/attachments-table/attachments-table.component';
import { AttachmentsTableDialogComponent } from './components/detail/attachments-table/attachments-table-dialog/attachments-table-dialog.component';

import { from } from 'rxjs';
import { SummaryComponent } from './components/summary/summary.component';
import { SummaryTableComponent } from './components/summary/summary-table/summary-table.component';
import { ConfirmOperationDialogComponent } from './components/detail/confirm-operation-dialog/confirm-operation-dialog.component';
import { ActionsOverlayComponent } from './components/search/semantic-search/actions-overlay/actions-overlay.component';
import { SearchDetailComponent } from './components/search/semantic-search/search-detail/search-detail.component';
import { SearchExecuteComponent } from './components/search/semantic-search/search-execute/search-execute.component';
import { HttpSpinnerInterceptor } from './services/http-spinner.interceptor';
import { EntitySelectorDialogComponent } from './components/search/semantic-search/entity-selector-dialog/entity-selector-dialog.component';

import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { EntityEliminationDialogComponent } from './components/search/semantic-search/entity-elimination-dialog/entity-elimination-dialog.component';
import { AutoLoginComponent } from './components/autologin/autologin.component';
import { CookiebarComponent } from './components/cookiebar/cookiebar.component';

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

/**
 *
 *
 * @export
 * @class AppModule
 */
@NgModule({
  declarations: [
    AlertsCenterComponent,
    AppComponent,
    AttachmentsComponent,
    DashboardComponent,
    DetailComponent,
    DynamicDialogComponent,
    FieldsComponent,
    LoaderComponent,
    OverlayLoadingDirective,
    ACLDirective,
    TabsComponent,
    DialogComponent,
    SearchComponent,
    ConfirmDialogComponent,
    FreeSearchComponent,
    SparqlSearchComponent,
    SemanticSearchComponent,
    SessionComponent,
    SessionDialogComponent,
    AttachmentsTableComponent,
    AttachmentsTableDialogComponent,
    SummaryComponent,
    SummaryTableComponent,
    ConfirmOperationDialogComponent,
    ActionsOverlayComponent,
    SearchDetailComponent,
    SearchExecuteComponent,
    EntitySelectorDialogComponent,
    EntityEliminationDialogComponent,
    AutoLoginComponent,
    CookiebarComponent,
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    CommonModule,
    CodemirrorModule,
    MrfFormModule,
    FormsModule,
    HttpClientModule,
    MatBadgeModule,
    MatButtonModule,
    MatCardModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSortModule,
    MatSidenavModule,
    MatTableModule,
    MatPaginatorModule,
    MatToolbarModule,
    MatTabsModule,
    OverlayModule,
    ReactiveFormsModule,
    ReactiveFormsModule,
    TranslateModule.forRoot({
        loader: {
            provide: TranslateLoader,
            useFactory: HttpLoaderFactory,
            deps: [HttpClient]
        }
    }),
  ],
  exports: [
    ACLDirective,
  ],
  providers: [
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    { provide: HTTP_INTERCEPTORS,
      useClass: AuthenticationErrorInterceptor,
      multi: true
    },
    { provide: HTTP_INTERCEPTORS,
      useClass: PaginatorInterceptor,
      multi: true
    },
    { provide: HTTP_INTERCEPTORS,
      useClass: HttpSpinnerInterceptor,
      multi: true
    },
  ],
  bootstrap: [AppComponent],
  entryComponents: [LoaderComponent],
})
export class AppModule { }
