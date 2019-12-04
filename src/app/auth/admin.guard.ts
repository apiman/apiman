import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import { Observable } from 'rxjs';
import {KeycloakAuthGuard, KeycloakService} from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard extends KeycloakAuthGuard implements CanActivate {

  constructor(protected router: Router, protected keycloakAngular: KeycloakService) {
    super(router, keycloakAngular);
  }

  isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    const keycloakInstance = this.keycloakAngular.getKeycloakInstance();
    const isAuthorized = keycloakInstance.tokenParsed.realm_access.roles.find((role) => role === 'apiadmin') !== undefined;
    if (!isAuthorized) {
      this.router.navigate(['/not-authorized'], { skipLocationChange: true });
    }
    return Promise.resolve(isAuthorized);
  }

}
