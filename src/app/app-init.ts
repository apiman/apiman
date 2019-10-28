import { KeycloakService } from 'keycloak-angular';

export function initializer(keycloak: KeycloakService): () => Promise<any> {
  return (): Promise<any> => keycloak.init({
    config: {
      url: 'https://pc0854.scheer.systems:8445/auth',
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
