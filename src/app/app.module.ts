import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER  } from '@angular/core';

import { PasHeaderComponent } from './pas-header/pas-header.component';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { ApiListComponent } from './api-list/api-list.component';

import { MatTableModule } from '@angular/material/table';
import { HttpClientModule } from '@angular/common/http';

import { KeycloakService, KeycloakAngularModule } from 'keycloak-angular';
import { initializer } from './app-init';
import {environment} from '../environments/environment';


@NgModule({
  declarations: [
    AppComponent,
    PasHeaderComponent,
    ApiListComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatIconModule,
    MatTableModule,
    HttpClientModule,
    KeycloakAngularModule
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
export class AppModule { }
