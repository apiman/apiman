import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakInstance } from 'keycloak-js';
import { ConfigService } from '../config/config.service';

@Injectable({
  providedIn: 'root',
  deps: [KeycloakService],
})
export class KeycloakHelperService {
  private isLoggedIn = false;
  private executed = false;
  private username?: string;

  constructor(
    private readonly keycloak: KeycloakService,
    private configService: ConfigService
  ) {}

  /**
   * Init keycloak setting, this is called via APP Initializer
   */
  public initKeycloak(): Promise<any> {
    return this.keycloak.init({
        config: {
          url: this.configService.getAuth().url,
          realm: this.configService.getAuth().realm,
          clientId: this.configService.getAuth().clientId,
        },
        initOptions: {
          onLoad: 'check-sso',
          checkLoginIframe: false,
          token: window.sessionStorage.getItem(
            'KEYCLOAK_SESSION_STORAGE_TOKEN'
          )!,
          refreshToken: window.sessionStorage.getItem(
            'KEYCLOAK_SESSION_STORAGE_REFRESH_TOKEN'
          )!,
        },
        loadUserProfileAtStartUp: true, // because of https://github.com/mauriciovigolo/keycloak-angular/pull/269
        enableBearerInterceptor: true,
        bearerExcludedUrls: ['/assets', '/clients/public'] //TODO
      })
  }

  public async getUserProfile() {
    this.isLoggedIn = await this.keycloak.isLoggedIn();
    if (this.isLoggedIn) {
      return await this.keycloak.loadUserProfile();
    }
    return null;
  }

  public login(): void {
    this.keycloak.login();
  }

  public logout(): void {
    KeycloakHelperService.clearTokensFromSessionStorage();
    const url = window.location.href.substring(0, window.location.href.lastIndexOf('/')) + '/home';
    this.keycloak.logout(url);
  }

  /**
   * Will set the current tokens after login, and start an automatic refresh of tokens.
   * Could be triggered multiple times but should only execute once
   */
  public setAndUpdateTokens() {
    if (!this.executed) {
      KeycloakHelperService.setTokensToSessionStorage(
        this.keycloak.getKeycloakInstance()
      );
      setInterval(() => {
        this.keycloak
          .updateToken()
          .then(() => {
            // console.log('Token successfully refreshed', new Date());
            KeycloakHelperService.setTokensToSessionStorage(
              this.keycloak.getKeycloakInstance()
            );
          })
          .catch(() => {
            // console.log('Error refreshing token', new Date());
            KeycloakHelperService.clearTokensFromSessionStorage();
          });
      }, Math.min((this.keycloak.getKeycloakInstance().tokenParsed!.exp! - 60) * 1000, 4 * 60 * 1000)); // refresh token minimum every 4 minutes)
    }
    this.executed = true;
  }

  private static setTokensToSessionStorage(keycloakInstance: KeycloakInstance) {
    window.sessionStorage.setItem(
      'KEYCLOAK_SESSION_STORAGE_TOKEN',
      keycloakInstance.token!
    );
    window.sessionStorage.setItem(
      'KEYCLOAK_SESSION_STORAGE_REFRESH_TOKEN',
      keycloakInstance.refreshToken!
    );
  }

  private static clearTokensFromSessionStorage() {
    window.sessionStorage.removeItem('KEYCLOAK_SESSION_STORAGE_TOKEN');
    window.sessionStorage.removeItem('KEYCLOAK_SESSION_STORAGE_REFRESH_TOKEN');
  }

  public getUsername() {
    if (!this.username) {
      this.username = this.keycloak.getUsername();
    }
    return this.username;
  }
}
