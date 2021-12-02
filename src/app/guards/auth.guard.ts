/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { KeycloakAuthGuard, KeycloakService } from 'keycloak-angular';
import { KeycloakHelperService } from '../services/keycloak-helper/keycloak-helper.service';
import { BackendService } from '../services/backend/backend.service';
import { ICurrentUser } from '../interfaces/ICommunication';
import { catchError } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { PermissionsService } from '../services/permissions/permissions.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard extends KeycloakAuthGuard {
  constructor(
    protected readonly router: Router,
    protected readonly keycloakAngular: KeycloakService,
    private keycloakHelper: KeycloakHelperService,
    private backend: BackendService,
    private permissionsService: PermissionsService
  ) {
    super(router, keycloakAngular);
  }

  async isAccessAllowed(): Promise<boolean | UrlTree> {
    if (!this.authenticated) {
      await this.keycloakAngular.login({
        redirectUri: window.location.href
      });
    } else {
      // we are logged in and can set the tokens
      this.keycloakHelper.setAndUpdateTokens();

      this.backend
        .getCurrentUser()
        .pipe(
          catchError((err) => {
            console.warn(err);
            this.authenticated = false;
            return EMPTY;
          })
        )
        .subscribe((user: ICurrentUser) => {
          console.log('Logged in with user: ' + user.username, user);
          this.permissionsService.setPermissions(user.permissions);
        });
    }
    return Promise.resolve(this.authenticated);
  }
}
