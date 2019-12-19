import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {KeycloakAuthGuard, KeycloakService} from 'keycloak-angular';
import {KeycloakUserService} from '../services/keycloak-user.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard extends KeycloakAuthGuard implements CanActivate {

  constructor(protected router: Router,
              protected keycloakAngular: KeycloakService,
              public keycloakUser: KeycloakUserService) {
    super(router, keycloakAngular);
  }

  isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    const isAuthorized = this.keycloakUser.isAdmin();
    if (!isAuthorized) {
      this.router.navigate(['/not-authorized'], {skipLocationChange: true});
    }
    return Promise.resolve(isAuthorized);
  }

}
