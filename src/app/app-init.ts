import { KeycloakService } from 'keycloak-angular';
import {Inject} from '@angular/core';
import {environment} from '../environments/environment';

export function initializer(keycloak: KeycloakService): () => Promise<any> {
  return (): Promise<any> => keycloak.init({
    config: {
      url: environment.keycloakAuthUrl,
      realm: 'Apiman',
      clientId: 'apiman-devportal'
    },
    initOptions: {
      onLoad: 'login-required',
      checkLoginIframe: false,
      token: localStorage.getItem('apiman_keycloak_token'),
      refreshToken: localStorage.getItem('apiman_keycloak_refresh_token')
    },
    enableBearerInterceptor: true,
    bearerExcludedUrls: ['/assets', '/clients/public']
  }).then(success => {
    const keycloakInstance = keycloak.getKeycloakInstance();
    localStorage.setItem('apiman_keycloak_token', keycloakInstance.token);
    localStorage.setItem('apiman_keycloak_refresh_token', keycloakInstance.refreshToken);
  });
}
