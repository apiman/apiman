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

import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router} from '@angular/router';
import {KeycloakAuthGuard, KeycloakService} from 'keycloak-angular';
import {KeycloakUserService} from '../services/keycloak-user.service';

@Injectable({
  providedIn: 'root'
})
export class DevportalGuard extends KeycloakAuthGuard implements CanActivate {
  constructor(protected router: Router,
              protected keycloakAngular: KeycloakService,
              public keycloakUser: KeycloakUserService) {
    super(router, keycloakAngular);
  }

  isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    const isAuthorized = this.keycloakUser.isUser();
    if (!isAuthorized) {
      this.router.navigate(['/not-authorized'], {skipLocationChange: true});
    }
    return Promise.resolve(isAuthorized);
  }
}
