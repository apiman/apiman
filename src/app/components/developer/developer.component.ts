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

import {Component} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import {TokenService} from '../../services/token.service';

@Component({
  selector: 'app-developer',
  templateUrl: './developer.component.html',
  styleUrls: ['./developer.component.scss']
})

export class DeveloperComponent {

  /**
   * load the keycloak roles from keycloak service
   * @param keycloak the keycloak service
   */
  constructor(private keycloak: KeycloakService, private tokenService: TokenService) {
    // to enforce that the token is updated we use Number.MAX_SAFE_INTEGER here as min validity (ensure the roles are up to date)
    this.keycloak.updateToken(Number.MAX_SAFE_INTEGER).then(() => {
      console.log('token refreshed');
      const keycloakInstance = keycloak.getKeycloakInstance();
      // set token to token service
      tokenService.setTokens(keycloakInstance.token, keycloakInstance.refreshToken);
    }).catch(() => {
      console.error('Failed to refresh token');
    });
  }
}
