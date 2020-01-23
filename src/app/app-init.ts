import {KeycloakService} from 'keycloak-angular';
import {environment} from '../environments/environment';
import {TokenService} from './services/token.service';

/**
 * Initialize the keycloak session
 * @param keycloak the keycloak service
 */
export function initializer(keycloak: KeycloakService, tokenService: TokenService): () => Promise<any> {
  // Store tokens in session storage for page reload
  tokenService.getTokens().subscribe((tokens) => {
    console.log('set token to session storage');
    sessionStorage.setItem('api_mgmt_keycloak_token', tokens.token);
    sessionStorage.setItem('api_mgmt_keycloak_refresh_token', tokens.refreshToken);
  });

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
    tokenService.setTokens(keycloakInstance.token, keycloakInstance.refreshToken);

    // refresh token via interval
    setInterval(() => {
        keycloak.updateToken().then(() => console.log('token refreshed finished')).catch(() => console.error('error refreshing token'));
        tokenService.setTokens(keycloakInstance.token, keycloakInstance.refreshToken);
      },
      Math.min((keycloakInstance.tokenParsed.exp - 60) * 1000, 4 * 60 * 1000)); // refresh token minimum every 4 minutes
  });
}
