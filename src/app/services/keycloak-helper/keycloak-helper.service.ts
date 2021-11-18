import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakInstance, KeycloakProfile } from 'keycloak-js';
import { ConfigService } from '../config/config.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
  deps: [KeycloakService]
})
export class KeycloakHelperService {
  private isLoggedIn = false;
  private executed = false;
  private username?: string;

  private readonly TOKEN = 'KEYCLOAK_SESSION_STORAGE_TOKEN';
  private readonly REFRESH_TOKEN = 'KEYCLOAK_SESSION_STORAGE_REFRESH_TOKEN';

  constructor(
    private readonly keycloak: KeycloakService,
    private configService: ConfigService,
    private router: Router
  ) {}

  /**
   * Init keycloak setting, this is called via APP Initializer
   */
  public initKeycloak(): Promise<boolean> {
    return this.keycloak.init({
      config: {
        url: this.configService.getAuth().url,
        realm: this.configService.getAuth().realm,
        clientId: this.configService.getAuth().clientId
      },
      initOptions: {
        onLoad: 'check-sso',
        checkLoginIframe: false,
        token: window.sessionStorage.getItem(this.TOKEN) ?? '',
        refreshToken: window.sessionStorage.getItem(this.REFRESH_TOKEN) ?? ''
      },
      loadUserProfileAtStartUp: true, // because of https://github.com/mauriciovigolo/keycloak-angular/pull/269
      enableBearerInterceptor: true,
      bearerExcludedUrls: ['/assets', '/clients/public'] //TODO
    });
  }

  public async getUserProfile(): Promise<KeycloakProfile | null> {
    this.isLoggedIn = await this.keycloak.isLoggedIn();
    if (this.isLoggedIn) {
      return await this.keycloak.loadUserProfile();
    }
    return null;
  }

  public login(): void {
    void this.keycloak.login();
  }

  public logout(): void {
    this.clearTokensFromSessionStorage();
    // remove current angular route so that the base path is still used
    const url = window.location.href.replace(this.router.url, '') + '/home';
    void this.keycloak.logout(url);
  }

  /**
   * Will set the current tokens after login, and start an automatic refresh of tokens.
   * Could be triggered multiple times but should only execute once
   */
  public setAndUpdateTokens(): void {
    if (!this.executed) {
      this.setTokensToSessionStorage(this.keycloak.getKeycloakInstance());
      setInterval(() => {
        this.keycloak
          .updateToken()
          .then(() => {
            // console.log('Token successfully refreshed', new Date());
            this.setTokensToSessionStorage(this.keycloak.getKeycloakInstance());
          })
          .catch(() => {
            // console.log('Error refreshing token', new Date());
            this.clearTokensFromSessionStorage();
          });
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      }, Math.min((this.keycloak.getKeycloakInstance().tokenParsed!.exp! - 60) * 1000, 4 * 60 * 1000)); // refresh token minimum every 4 minutes)
    }
    this.executed = true;
  }

  private setTokensToSessionStorage(keycloakInstance: KeycloakInstance) {
    window.sessionStorage.setItem(this.TOKEN, keycloakInstance.token ?? '');
    window.sessionStorage.setItem(
      this.REFRESH_TOKEN,
      keycloakInstance.refreshToken ?? ''
    );
  }

  private clearTokensFromSessionStorage() {
    window.sessionStorage.removeItem(this.TOKEN);
    window.sessionStorage.removeItem(this.REFRESH_TOKEN);
  }

  public getUsername(): string {
    if (!this.username) {
      this.username = this.keycloak.getUsername();
    }
    return this.username;
  }
}
