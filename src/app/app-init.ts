import { environment } from '../environments/environment';
import { KeycloakService } from 'keycloak-angular';

export function initializer(keycloak: KeycloakService): () => Promise<any> {
  return (): Promise<any> => keycloak.init({
    config: {
      url: environment.keycloakAuthEndpoint,
      realm: 'Apiman',
      clientId: 'apimandevportal'
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
