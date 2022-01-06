/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

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

  private readonly TOKEN = 'APIMAN_DEVPORTAL_KEYCLOAK_SESSION_STORAGE_TOKEN';
  private readonly REFRESH_TOKEN =
    'APIMAN_DEVPORTAL_KEYCLOAK_SESSION_STORAGE_REFRESH_TOKEN';

  constructor(
    private readonly keycloak: KeycloakService,
    private configService: ConfigService,
    private router: Router
  ) {}

  /**
   * Init keycloak setting, this is called via APP Initializer
   */
  public initKeycloak(): Promise<boolean> {
    return this.keycloak
      .init({
        config: {
          url: this.configService.getAuth().url,
          realm: this.configService.getAuth().realm,
          clientId: this.configService.getAuth().clientId
        },
        initOptions: {
          onLoad: 'check-sso',
          checkLoginIframe: false,
          token: localStorage.getItem(this.TOKEN) ?? '',
          refreshToken: localStorage.getItem(this.REFRESH_TOKEN) ?? ''
        },
        loadUserProfileAtStartUp: true, // because of https://github.com/mauriciovigolo/keycloak-angular/pull/269
        enableBearerInterceptor: true,
        bearerExcludedUrls: ['/assets']
      })
      .catch((error) => {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
        if (error.error && error.error === 'access_denied') {
          // if we got an access_denied we still can init the keycloak library
          return true;
        } else {
          console.error(error);
          throw error;
        }
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
    if (!this.executed && this.keycloak.getKeycloakInstance().tokenParsed) {
      this.setTokensToLocalStorage(this.keycloak.getKeycloakInstance());
      setInterval(() => {
        this.keycloak
          .updateToken()
          .then(() => {
            this.setTokensToLocalStorage(this.keycloak.getKeycloakInstance());
          })
          .catch(() => {
            console.error('Error refreshing token', new Date());
            this.clearTokensFromSessionStorage();
          });
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      }, Math.min((this.keycloak.getKeycloakInstance().tokenParsed!.exp! - 60) * 1000, 4 * 60 * 1000)); // refresh token minimum every 4 minutes)
    }
    this.executed = true;
  }

  private setTokensToLocalStorage(keycloakInstance: KeycloakInstance) {
    localStorage.setItem(this.TOKEN, keycloakInstance.token ?? '');
    localStorage.setItem(
      this.REFRESH_TOKEN,
      keycloakInstance.refreshToken ?? ''
    );
  }

  public getTokenFromLocalStorage(): string {
    // we have to trigger the method here to make sure the tokens are set after a login
    this.setAndUpdateTokens();
    return localStorage.getItem(this.TOKEN) ?? '';
  }

  private clearTokensFromSessionStorage() {
    localStorage.removeItem(this.TOKEN);
    localStorage.removeItem(this.REFRESH_TOKEN);
  }

  public getUsername(): string {
    if (!this.username) {
      this.username = this.keycloak.getUsername();
    }
    return this.username;
  }
}
