import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { KeycloakAuthGuard, KeycloakService } from 'keycloak-angular';
import { KeycloakHelperService } from '../services/keycloak-helper/keycloak-helper.service';
import { BackendService } from '../services/backend/backend.service';
import { ICurrentUser } from '../interfaces/ICommunication';
import { catchError } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { PermissionsService } from '../services/permissions/permissions.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard extends KeycloakAuthGuard {
  constructor(
    protected readonly router: Router,
    protected readonly keycloakAngular: KeycloakService,
    private keycloakHelper: KeycloakHelperService,
    private backend: BackendService,
    private permissionsService: PermissionsService
  ) {
    super(router, keycloakAngular);
  }

  async isAccessAllowed(): Promise<boolean | UrlTree> {
    if (!this.authenticated) {
      await this.keycloakAngular.login({
        redirectUri: window.location.href
      });
    } else {
      // we are logged in and can set the tokens
      this.keycloakHelper.setAndUpdateTokens();

      this.backend
        .getCurrentUser()
        .pipe(
          catchError((err) => {
            console.warn(err);
            this.authenticated = false;
            return EMPTY;
          })
        )
        .subscribe((user: ICurrentUser) => {
          console.log('Logged in with user: ', user);
          this.permissionsService.setPermissions(user.permissions);
        });
    }
    return Promise.resolve(this.authenticated);
  }
}
