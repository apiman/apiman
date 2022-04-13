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

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { ConfigService } from '../../services/config/config.service';
import SwaggerUI from 'swagger-ui';
import { KeycloakHelperService } from '../../services/keycloak-helper/keycloak-helper.service';
import { KeycloakService } from 'keycloak-angular';
import { IUrlPath } from '../../interfaces/IUrlPath';

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.scss']
})
export class SwaggerComponent implements OnInit {
  private readonly apiKeyHeader = 'X-API-Key';
  endpoint = '';
  isPublicApi = false;
  isTryEnabled = false;

  constructor(
    private route: ActivatedRoute,
    private heroService: HeroService,
    private translator: TranslateService,
    private config: ConfigService,
    private keycloakHelperService: KeycloakHelperService,
    private keycloakService: KeycloakService
  ) {
    this.endpoint = config.getEndpoint();
  }

  /**
   * Load the swagger definition and display it with the swagger ui bundle library on component initialization
   */
  async ngOnInit(): Promise<void> {
    const { organizationId, apiId, apiVersion, apiKey } = this.getRouteParams();
    const urls: IUrlPath = {
      urlPath: `${this.endpoint}/devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/definition`,
      loggedInUrlPath: `${this.endpoint}/devportal/protected/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/definition`
    };

    this.heroService.setUpHero({
      title: this.translator.instant('COMMON.API_DOCS') as string,
      subtitle: `${apiId} - ${apiVersion}`
    });

    let definitionUrl = urls.urlPath;
    if (await this.keycloakService.isLoggedIn()) {
      definitionUrl = urls.loggedInUrlPath;
    }

    const swaggerOptions: SwaggerUI.SwaggerUIOptions = {
      dom_id: '#swagger-editor',
      layout: 'BaseLayout',
      url: definitionUrl,
      docExpansion: 'list',
      operationsSorter: 'alpha',
      tryItOutEnabled: true,
      supportedSubmitMethods: this.getSupportedMethods(),
      requestInterceptor: (request: SwaggerUI.Request) => {
        this.setAuthorizationHeader(request);
        this.setApiKeyHeader(apiKey, request);
        return request;
      },
      responseInterceptor: (response: SwaggerUI.Response) => {
        return response;
      },
      onComplete: () => {
        swaggerUI.preauthorizeApiKey(this.apiKeyHeader, apiKey);
      }
    };

    this.setDisableAuth(swaggerOptions);

    // Loads the swagger ui with its options
    const swaggerUI = SwaggerUI(swaggerOptions);
  }

  private setApiKeyHeader(apiKey: string | null, request: SwaggerUI.Request) {
    if (!this.isPublicApi && apiKey != null) {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      request.headers[this.apiKeyHeader] = apiKey;
    }
  }

  private setAuthorizationHeader(request: SwaggerUI.Request) {
    const token = this.keycloakHelperService.getTokenFromLocalStorage();
    if (token.length > 0) {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      request.headers.Authorization = 'Bearer ' + token;
    }
  }

  private setDisableAuth(swaggerOptions: SwaggerUI.SwaggerUIOptions) {
    if (!this.isTryEnabled) {
      // See https://github.com/swagger-api/swagger-ui/issues/3725
      const DisableAuthorizePlugin = () => ({
        wrapComponents: { authorizeBtn: () => () => null }
      });
      swaggerOptions.plugins = [];
      swaggerOptions.plugins.push(DisableAuthorizePlugin);
    }
  }

  private getRouteParams() {
    const organizationId = this.route.snapshot.paramMap.get('orgId') ?? '';
    const apiId: string = this.route.snapshot.paramMap.get('apiId') ?? '';
    const apiVersion = this.route.snapshot.paramMap.get('apiVersion') ?? '';
    const clientId = this.route.snapshot.queryParamMap.get('clientId') ?? '';
    const apiKey = localStorage.getItem(
      `APIMAN_DEVPORTAL-${organizationId}-${apiId}-${apiVersion}-${clientId}`
    );

    this.isPublicApi = JSON.parse(
      this.route.snapshot.queryParamMap.get('publicApi') ?? 'false'
    ) as boolean;

    this.isTryEnabled = JSON.parse(
      this.route.snapshot.queryParamMap.get('try') ?? 'false'
    ) as boolean;

    return { organizationId, apiId, apiVersion, apiKey };
  }

  private getSupportedMethods(): SwaggerUI.SupportedHTTPMethods[] {
    const submitMethods: SwaggerUI.SupportedHTTPMethods[] = [
      'get',
      'put',
      'post',
      'delete',
      'options',
      'head',
      'patch',
      'trace'
    ];
    if (this.isTryEnabled) {
      return submitMethods;
    } else {
      return [];
    }
  }
}
