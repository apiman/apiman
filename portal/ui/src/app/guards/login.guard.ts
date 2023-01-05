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

@Injectable({
  providedIn: 'root'
})
export class LoginGuard extends KeycloakAuthGuard {
  constructor(
    router: Router,
    keycloakAngular: KeycloakService,
    private keycloakHelper: KeycloakHelperService
  ) {
    super(router, keycloakAngular);
  }

  /**
   * Checks only if the user is logged in else redirects to login page
   */
  async isAccessAllowed(): Promise<boolean | UrlTree> {
    // Force the user to log in if currently unauthenticated
    if (!this.authenticated) {
      await this.keycloakAngular.login({
        redirectUri: window.location.href
      });
    }
    // we are logged in and can set the tokens and fetch permissions
    this.keycloakHelper.initUpdateTokens();
    return this.authenticated;
  }
}
