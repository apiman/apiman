import { Injectable } from '@angular/core';
import {KeycloakAuthGuard, KeycloakService} from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class KeycloakUserService {

  constructor(protected keycloakAngular: KeycloakService) { }

  /**
   * Check if the user is a API-Mgmt Admin
   */
  public isAdmin(): boolean {
    const keycloakInstance = this.keycloakAngular.getKeycloakInstance();
    return keycloakInstance.tokenParsed.realm_access.roles.find((role) => role === 'apiadmin') !== undefined;
  }

  /**
   * Check if the user is a Devportal User
   */
  public isUser(): boolean {
    const keycloakInstance = this.keycloakAngular.getKeycloakInstance();
    return keycloakInstance.tokenParsed.realm_access.roles.find((role) => role === 'devportaluser') !== undefined;
  }
}
