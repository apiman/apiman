import { KeycloakService } from 'keycloak-angular';
import {Inject} from '@angular/core';
import {environment} from '../environments/environment';

export function initializer(keycloak: KeycloakService): () => Promise<any> {
  return (): Promise<any> => keycloak.init({
    config: {
      url: environment.keycloakAuthUrl,
      realm: 'Apiman',
      clientId: 'devportal'
    },
    initOptions: {
      onLoad: 'login-required',
      checkLoginIframe: false,
      token: sessionStorage.getItem('apiman_keycloak_token'),
      refreshToken: sessionStorage.getItem('apiman_keycloak_refresh_token')
    },
    enableBearerInterceptor: true,
    bearerExcludedUrls: ['/assets', '/clients/public']
  }).then(success => {
    const keycloakInstance = keycloak.getKeycloakInstance();
    sessionStorage.setItem('apiman_keycloak_token', keycloakInstance.token);
    sessionStorage.setItem('apiman_keycloak_refresh_token', keycloakInstance.refreshToken);
  });
}
