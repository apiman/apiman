import {KeycloakService} from 'keycloak-angular';
import {environment} from '../environments/environment';

/**
 * Initialize the keycloak session
 * @param keycloak the keycloak service
 */
export function initializer(keycloak: KeycloakService): () => Promise<any> {
  return (): Promise<any> => keycloak.init({
    config: {
      url: environment.keycloakAuthUrl,
      realm: environment.apiMgmtRealm,
      clientId: 'devportal'
    },
    initOptions: {
      onLoad: 'login-required',
      checkLoginIframe: false,
      token: sessionStorage.getItem('api_mgmt_keycloak_token'),
      refreshToken: sessionStorage.getItem('api_mgmt_keycloak_refresh_token')
    },
    enableBearerInterceptor: true,
    bearerExcludedUrls: ['/assets', '/clients/public']
  }).then(success => {
    const keycloakInstance = keycloak.getKeycloakInstance();
    storeTokensInSessionStorage(keycloakInstance.token, keycloakInstance.refreshToken);
    // refresh token via interval
    setInterval(() => {
        keycloak.updateToken().then(() => console.log('token refreshed')).catch(() => console.error('error refreshing token'));
        // set fresh token to session storage
        storeTokensInSessionStorage(keycloakInstance.token, keycloakInstance.refreshToken);
      },
      Math.min((keycloakInstance.tokenParsed.exp - 60) * 1000, 4 * 60 * 1000)); // refresh token minimum every 4 minutes
  });
}

/**
 * Store tokens in session storage
 * @param token the token
 * @param refreshToken the refresh token
 */
function storeTokensInSessionStorage(token, refreshToken) {
  sessionStorage.setItem('api_mgmt_keycloak_token', token);
  sessionStorage.setItem('api_mgmt_keycloak_refresh_token', refreshToken);
}


