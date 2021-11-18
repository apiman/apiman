import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree
} from '@angular/router';
import { KeycloakAuthGuard, KeycloakService } from 'keycloak-angular';
import { KeycloakHelperService } from '../services/keycloak-helper/keycloak-helper.service';
import { BackendService } from '../services/backend/backend.service';
import { ICurrentUser } from '../interfaces/ICommunication';
import { catchError } from 'rxjs/operators';
import { EMPTY } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard extends KeycloakAuthGuard {
  constructor(
    protected readonly router: Router,
    protected readonly keycloakAngular: KeycloakService,
    private keycloakHelper: KeycloakHelperService,
    private backend: BackendService
  ) {
    super(router, keycloakAngular);
  }

  /**
   * If not authenticated this guard forces a login and starts the automatic token refresh mechanism
   * @param route the current route
   * @param state the current state
   */
  async isAccessAllowed(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean | UrlTree> {
    if (!this.authenticated) {
      const url =
        window.location.href.substring(
          0,
          window.location.href.lastIndexOf('/')
        ) + state.url;
      await this.keycloakAngular.login({
        redirectUri: url
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
        });
    }
    return Promise.resolve(this.authenticated);
  }
}
