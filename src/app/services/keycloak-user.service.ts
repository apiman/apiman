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

import { Injectable } from '@angular/core';
import {KeycloakAuthGuard, KeycloakService} from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class KeycloakUserService {

  constructor(protected keycloakAngular: KeycloakService) { }

  /**
   * Check if the user is a API-Mgmt Admin
   */
  public isAdmin(): boolean {
    const keycloakInstance = this.keycloakAngular.getKeycloakInstance();
    return keycloakInstance.tokenParsed.realm_access
      && keycloakInstance.tokenParsed.realm_access.roles.find((role) => role === 'apiadmin') !== undefined;
  }

  /**
   * Check if the user is a Devportal User
   */
  public isUser(): boolean {
    const keycloakInstance = this.keycloakAngular.getKeycloakInstance();
    return keycloakInstance.tokenParsed.realm_access
      && keycloakInstance.tokenParsed.realm_access.roles.find((role) => role === 'devportaluser') !== undefined;
  }
}
