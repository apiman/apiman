import {Component, OnInit} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import {TokenService} from '../../services/token.service';

@Component({
  selector: 'app-developer',
  templateUrl: './developer.component.html',
  styleUrls: ['./developer.component.scss']
})

export class DeveloperComponent {

  public developers: Array<string> = [];

  /**
   * load the keycloak roles from keycloak service
   * @param keycloak the keycloak service
   */
  constructor(private keycloak: KeycloakService, private tokenService: TokenService) {
    // to enforce that the token is updated we use Number.MAX_SAFE_INTEGER here as min validity (ensure the roles are up to date)
    this.keycloak.updateToken(Number.MAX_SAFE_INTEGER).then(() => {
      console.log('token refreshed');
      const keycloakInstance = keycloak.getKeycloakInstance();
      // set token to token service
      tokenService.setTokens(keycloakInstance.token, keycloakInstance.refreshToken);
      // get keycloak client roles of devportal
      // set ts-ignore here because our own property 'devportal' is not part of the type definition of the keycloak library
      // @ts-ignore
      const resourceAccess = keycloakInstance.tokenParsed.resource_access.devportal;
      console.log('read keycloak resourceAccess', resourceAccess);
      if (resourceAccess && resourceAccess.roles) {
        this.developers = resourceAccess.roles;
      } else {
        this.developers = [];
      }
    }).catch(() => {
      console.error('Failed to refresh token');
    });
  }
}
