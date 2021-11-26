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
