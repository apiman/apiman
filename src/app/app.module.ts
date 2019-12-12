import {BrowserModule} from '@angular/platform-browser';
import {NgModule, APP_INITIALIZER} from '@angular/core';

import {PasHeaderComponent} from './components/pas-header/pas-header.component';

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
import {
  MatButtonModule,
  MatInputModule,
  MatOptionModule,
  MatAutocompleteModule,
  MatProgressSpinnerModule,
  MatTooltipModule
} from '@angular/material';
import {EditDeveloperComponent} from './components/admin/edit-developer/edit-developer.component';
import {NotAuthorizedComponent} from './components/not-authorized/not-authorized.component';
import {ToasterModule} from 'angular2-toaster';
import {SwaggerComponent} from './components/swagger/swagger.component';


@NgModule({
  declarations: [
    AppComponent,
    PasHeaderComponent,
    ApiListComponent,
    AdminComponent,
    DeveloperComponent,
    DeveloperListComponent,
    CreateDeveloperComponent,
    ClientMappingComponent,
    EditDeveloperComponent,
    SwaggerComponent,
    NotAuthorizedComponent
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
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializer,
      multi: true,
      deps: [KeycloakService]
    },
    {
      provide: 'APIMAN_UI_REST_URL',
      useValue: environment.apimanUiRestUrl
    },
    {
      provide: 'KEYCLOAK_AUTH_URL',
      useValue: environment.keycloakAuthUrl
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
