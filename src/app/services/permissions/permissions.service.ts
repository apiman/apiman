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
import { IPermission } from '../../interfaces/ICommunication';

@Injectable({
  providedIn: 'root'
})
export class PermissionsService {
  private permissions: IPermission[] = [];

  constructor() {}

  public setPermissions(permissions: IPermission[]): void {
    this.permissions = permissions;
  }

  public getPermissions(): IPermission[] {
    return this.permissions;
  }

  /**
   * This will return an array of organizationIds where the permission applies
   * @param requestedPermission the permission to be checked
   */
  public getAllowedOrganizations(requestedPermission: IPermission): string[] {
    const organizations: string[] = [];
    this.permissions.forEach((permission: IPermission) => {
      if (permission.name === requestedPermission.name) {
        organizations.push(permission.organizationId);
      }
    });
    return organizations;
  }
}
