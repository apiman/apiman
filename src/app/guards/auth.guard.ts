import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {KeycloakAuthGuard, KeycloakService} from "keycloak-angular";
import {KeycloakHelperService} from "../services/keycloak-helper/keycloak-helper.service";

@Injectable({
  providedIn: 'root'
})

export class AuthGuard extends KeycloakAuthGuard {

  constructor(protected readonly router: Router,
              protected readonly keycloakAngular: KeycloakService,
              private keycloakHelper: KeycloakHelperService) {
    super(router, keycloakAngular);
  }

  /**
   * If not authenticated this guard forces a login and starts the automatic token refresh mechanism
   * @param route the current route
   * @param state the current state
   */
  async isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean | UrlTree> {
    if (!this.authenticated) {
      console.log('Login required');
      await this.keycloakAngular.login({
        redirectUri: window.location.origin + state.url
      });
    } else {
      // we are logged in and can set the tokens
      this.keycloakHelper.setAndUpdateTokens();
    }
    return Promise.resolve(this.authenticated);
  }
}
