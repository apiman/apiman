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
import {
  KeycloakEvent,
  KeycloakEventType,
  KeycloakService
} from 'keycloak-angular';
import { KeycloakProfile, KeycloakTokenParsed } from 'keycloak-js';
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
          checkLoginIframe: false
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

  public getKeycloakAccountUrl(): string {
    if (this.configService.getAuth().accountUrl) {
      return <string>this.configService.getAuth().accountUrl;
    } else {
      return `${this.configService.getAuth().url}/realms/${
        this.configService.getAuth().realm
      }/account`;
    }
  }

  public login(): void {
    void this.keycloak.login();
  }

  public logout(): void {
    // this.clearTokensFromSessionStorage();
    // remove current angular route so that the base path is still used
    const url = window.location.href.replace(this.router.url, '') + '/home';
    void this.keycloak.logout(url);
  }

  /**
   * Will set the current tokens after login, and start an automatic refresh of tokens.
   * Could be triggered multiple times but should only execute once
   */
  public initUpdateTokens(): void {
    if (!this.executed && this.keycloak.getKeycloakInstance().tokenParsed) {
      this.keycloak.keycloakEvents$.subscribe({
        next: (event: KeycloakEvent) => {
          if (event.type == KeycloakEventType.OnTokenExpired) {
            console.info('Try to refresh token');
            void this.keycloak.updateToken();
          }
        }
      });
    }
    this.executed = true;
  }

  public getToken(): string {
    return this.keycloak.getKeycloakInstance().token as string;
  }

  public getUsername(): string {
    if (!this.username) {
      this.username = this.keycloak.getUsername();
    }
    return this.username;
  }

  public decodeCurrentKeycloakToken(): KeycloakTokenParsed {
    try {
      return <KeycloakTokenParsed>(
        JSON.parse(window.atob(this.getToken().split('.')[1]))
      );
    } catch (error) {
      console.error('Error while decoding keycloak token', error);
      throw error;
    }
  }
}
