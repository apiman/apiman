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

import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { ICurrentUser } from 'src/app/interfaces/ICommunication';

@Component({
  selector: 'app-account-organization-permissions',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './account-organization-permissions.component.html',
  styleUrls: ['./account-organization-permissions.component.scss']
})
export class AccountOrganizationPermissionsComponent {
  public apiManagementPermissions: Map<string, Array<string>>;

  constructor() {
    this.apiManagementPermissions = new Map();
  }

  @Input()
  set apimanAccount(user: ICurrentUser | null) {
    if (!user) return;

    for (const entity of user.permissions) {
      if (!this.apiManagementPermissions.has(entity.organizationId)) {
        this.apiManagementPermissions.set(entity.organizationId, [entity.name]);
      } else {
        this.apiManagementPermissions
          .get(entity.organizationId)
          ?.push(entity.name);
      }
    }
  }
}
