/*
 * Copyright 2020 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {BrowserModule} from '@angular/platform-browser';
import {NgModule, APP_INITIALIZER} from '@angular/core';

import {HeaderComponent} from './components/app-header/header.component';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';

import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {ReactiveFormsModule} from '@angular/forms';

import {ApiListComponent} from './components/developer/api-list/api-list.component';
import {MatTableModule} from '@angular/material/table';

import {HttpClientModule} from '@angular/common/http';
import {KeycloakService, KeycloakAngularModule} from 'keycloak-angular';
import {initializer} from './app-init';
import {environment} from '../environments/environment';
import {AdminComponent} from './components/admin/admin.component';
import {DeveloperComponent} from './components/developer/developer.component';
import {DeveloperListComponent} from './components/admin/developer-list.component';
import {CreateDeveloperComponent} from './components/admin/create-developer/create-developer.component';
import {ClientMappingComponent} from './components/admin/create-developer/client-mapping.component';
import {DragDropModule} from '@angular/cdk/drag-drop';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatOptionModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { _MatMenuDirectivesModule, MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSortModule } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import {EditDeveloperComponent} from './components/admin/edit-developer/edit-developer.component';
import {NotAuthorizedComponent} from './components/not-authorized/not-authorized.component';
import {ToasterModule} from 'angular2-toaster';
import {SwaggerComponent} from './components/swagger/swagger.component';
import {MenuComponent} from './components/menu/menu.component';
import {AboutComponent} from './about/about.component';
import {TokenService} from './services/token.service';


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    ApiListComponent,
    AdminComponent,
    DeveloperComponent,
    DeveloperListComponent,
    CreateDeveloperComponent,
    ClientMappingComponent,
    EditDeveloperComponent,
    SwaggerComponent,
    NotAuthorizedComponent,
    MenuComponent,
    AboutComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatIconModule,
    MatTableModule,
    HttpClientModule,
    KeycloakAngularModule,
    MatListModule,
    ReactiveFormsModule,
    DragDropModule,
    MatButtonModule,
    MatInputModule,
    ToasterModule.forRoot(),
    MatOptionModule,
    MatAutocompleteModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSortModule,
    _MatMenuDirectivesModule,
    MatMenuModule,
    MatCardModule,
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializer,
      multi: true,
      deps: [KeycloakService, TokenService]
    },
    {
      provide: 'API_MGMT_UI_REST_URL',
      useValue: environment.apiMgmtUiRestUrl
    },
    {
      provide: 'KEYCLOAK_AUTH_URL',
      useValue: environment.keycloakAuthUrl
    },
    {
      provide: 'API_MGTM_REALM',
      useValue: environment.apiMgmtRealm
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
