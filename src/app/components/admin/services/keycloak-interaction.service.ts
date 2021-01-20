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

import {Inject, Injectable} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import KcAdminClient from 'keycloak-admin';
import {TokenService} from '../../../services/token.service';
import UserRepresentation from 'keycloak-admin/lib/defs/userRepresentation';
import GroupRepresentation from 'keycloak-admin/lib/defs/groupRepresentation';

@Injectable({
  providedIn: 'root'
})
export class KeycloakInteractionService {

  private kcAdminClient: KcAdminClient;


  /**
   * Constructor of Keycloak Interaction Service
   * @param keycloak the keycloak service
   * @param keycloakRestUrl the keycloak rest url
   */
  constructor(private keycloak: KeycloakService,
              private tokenService: TokenService,
              @Inject('KEYCLOAK_AUTH_URL') private keycloakRestUrl: string,
              @Inject('API_MGTM_REALM') private apiMgmtRealm: string) {
    this.kcAdminClient = new KcAdminClient({
      baseUrl: this.keycloakRestUrl,
      realmName: this.apiMgmtRealm
    });
    // set initial token
    const token = keycloak.getKeycloakInstance().token;
    this.kcAdminClient.setAccessToken(token);
    console.log('set token to admin library');

    this.tokenService.getTokens()
      .subscribe(tokens => {
        this.kcAdminClient.setAccessToken(tokens.token);
        console.log('set token to admin library');
      });
  }

  /**
   * Get all keycloak users
   * id, max value: https://www.keycloak.org/docs-api/12.0/rest-api/index.html#_groups_resource
   */
  public async getDevPortalUsers(): Promise<UserRepresentation[]> {
    const keycloakGroups: GroupRepresentation[] = [];
    const devPortalGroups: GroupRepresentation[] = [];
    const devPortalUsers: UserRepresentation[] = [];
    keycloakGroups.push(...(await this.kcAdminClient.groups.find()));
    devPortalGroups.push(...(keycloakGroups.filter(group => {
      return group.name === 'API-Management-Developer-Portal-Users' || group.name === 'API-Management-Administrators';
    })));
    for (const group of devPortalGroups){
      devPortalUsers.push(...(await this.kcAdminClient.groups.listMembers({id: group.id, max: (Math.pow(2, 31) - 1)})));
    }
    return devPortalUsers;
  }
}
