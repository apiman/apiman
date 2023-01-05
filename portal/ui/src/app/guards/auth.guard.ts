/*
 * Copyright 2022 Scheer PAS Schweiz AG
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
import { PermissionsService } from '../services/permissions/permissions.service';
import { ConfigService } from '../services/config/config.service';
import { SnackbarService } from '../services/snackbar/snackbar.service';
import { hasRequiredAuthRoles } from '../shared/utility';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard extends KeycloakAuthGuard {
  constructor(
    protected readonly router: Router,
    protected readonly keycloakAngular: KeycloakService,
    private keycloakHelper: KeycloakHelperService,
    private permissionsService: PermissionsService,
    private config: ConfigService,
    private snackbar: SnackbarService
  ) {
    super(router, keycloakAngular);
  }

  async isAccessAllowed(): Promise<boolean | UrlTree> {
    // Force the user to log in if currently unauthenticated
    if (!this.authenticated) {
      await this.keycloakAngular.login({
        redirectUri: window.location.href
      });
    }

    if (!hasRequiredAuthRoles(this.config.getBackendRoles(), this.roles)) {
      // TODO: change to error page
      this.snackbar.showErrorSnackBar('Not enough permission');
      return this.router.createUrlTree(['/home']);
    }

    // we are logged in and can set the tokens and fetch permissions
    this.keycloakHelper.initUpdateTokens();
    await this.permissionsService.updateUserPermissions();

    return this.authenticated;
  }
}
