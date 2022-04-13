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
import { PermissionsService } from '../permissions/permissions.service';
import { BackendService } from '../backend/backend.service';
import { EMPTY, Observable, of } from 'rxjs';
import { IOrganization, IPermission } from '../../interfaces/ICommunication';
import { KeycloakHelperService } from '../keycloak-helper/keycloak-helper.service';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {
  constructor(
    private permissionsService: PermissionsService,
    private backendService: BackendService,
    private keycloakHelper: KeycloakHelperService
  ) {}

  /**
   * Creates a new organization if no one exists
   *
   * @returns The organization and an empty object if the organization already exists
   */
  public createHomeOrgIfNotExists(): Observable<IOrganization> {
    const usernameAsOrgId = this.keycloakHelper.getUsername();
    if (this.hasHomeOrg(usernameAsOrgId)) {
      return of({} as IOrganization);
    } else {
      console.log('Creating home organization for: ' + usernameAsOrgId);
      return this.backendService.createOrganization(usernameAsOrgId).pipe(
        catchError((err) => {
          console.error('Error while creating an user organization', err);
          return EMPTY;
        })
      );
    }
  }

  /**
   * Checks if the user has at least one organization with "clientEdit" permissions
   *
   * @returns True if there is at least one org
   */
  private hasHomeOrg(username: string): boolean {
    return this.permissionsService
      .getAllowedOrganizations({
        name: `clientEdit`
      } as IPermission)
      .some((orgId: string) => orgId.includes(username));
  }
}
