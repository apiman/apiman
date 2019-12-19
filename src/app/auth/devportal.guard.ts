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
