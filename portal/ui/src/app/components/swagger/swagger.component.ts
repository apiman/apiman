/*
 * Copyright 2023 Scheer PAS Schweiz AG
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

import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { ConfigService } from '../../services/config/config.service';
import SwaggerUI from 'swagger-ui';
import { KeycloakHelperService } from '../../services/keycloak-helper/keycloak-helper.service';
import { IUrlPath } from '../../interfaces/IUrlPath';
import { IApiVersion, IContract } from '../../interfaces/ICommunication';
import { ApiService } from '../../services/api/api.service';
import { forkJoin, Observable, of } from 'rxjs';
import { BackendService } from '../../services/backend/backend.service';
import { HttpErrorResponse } from '@angular/common/http';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.scss']
})
export class SwaggerComponent implements OnInit {
  private readonly apiKeyHeader = 'X-API-Key';
  endpoint = '';
  isTryEnabled = false;
  contract?: IContract;
  imgSize = '40';
  apiVersion: IApiVersion = {} as IApiVersion;

  apiVersion$: Observable<IApiVersion> = of({} as IApiVersion);
  contract$: Observable<IContract> = of({} as IContract);

  @ViewChild('swagger-editor') swaggerEditorEl: HTMLElement = {} as HTMLElement;

  constructor(
    private route: ActivatedRoute,
    private snackbar: SnackbarService,
    private router: Router,
    private heroService: HeroService,
    private translator: TranslateService,
    private config: ConfigService,
    private keycloak: KeycloakService,
    private keycloakHelperService: KeycloakHelperService,
    private apiService: ApiService,
    private backendService: BackendService,
    private cdr: ChangeDetectorRef
  ) {
    this.endpoint = config.getEndpoint();
  }

  /**
   * Load the swagger definition and display it with the swagger ui bundle library on component initialization
   */
  ngOnInit() {
    this.heroService.setUpHero({
      title: this.translator.instant('COMMON.API_DOCS') as string,
      subtitle: ``
    });
    this.handleRouteParams();
    forkJoin([
      this.apiVersion$,
      this.contract$,
      this.keycloak.isLoggedIn()
    ]).subscribe({
      next: ([apiVersion, contract, isLoggedIn]) => {
        this.apiVersion = apiVersion;
        this.contract = contract?.id ? contract : undefined;
        this.isTryEnabled = this.apiVersion.publicAPI || !!this.contract;
        this.imgSize = this.contract ? '70' : '40';
        this.initSwaggerUi(isLoggedIn);
      },
      error: (err: HttpErrorResponse) => {
        console.error(err);
        this.snackbar.showErrorSnackBar(
          this.translator.instant('SNACKBAR_ERROR_NO_PERMISSIONS') as string
        );
        void this.router.navigate(['/home']);
      }
    });
  }

  private handleRouteParams() {
    const apiOrgId = this.route.snapshot.paramMap.get('orgId') ?? '';
    const apiId = this.route.snapshot.paramMap.get('apiId') ?? '';
    const apiVersion = this.route.snapshot.paramMap.get('apiVersion') ?? '';

    this.apiVersion$ = this.apiService.getApiVersion(
      apiOrgId,
      apiId,
      apiVersion
    );

    const clientOrgId = this.route.snapshot.queryParamMap.get('clientOrgId');
    const clientId = this.route.snapshot.queryParamMap.get('clientId');
    const clientVersion =
      this.route.snapshot.queryParamMap.get('clientVersion');
    const contractId = this.route.snapshot.queryParamMap.get('contractId');

    if (clientOrgId && clientId && clientVersion && contractId) {
      this.contract$ = this.backendService.getContract(
        clientOrgId,
        clientId,
        clientVersion,
        contractId
      );
    }
  }

  private initSwaggerUi(isLoggedIn: boolean) {
    const apiOrgId = this.apiVersion.api.organization.id;
    const apiId = this.apiVersion.api.id;
    const apiVersion = this.apiVersion.version;
    const urls: IUrlPath = {
      urlPath: `${this.endpoint}/devportal/organizations/${apiOrgId}/apis/${apiId}/versions/${apiVersion}/definition`,
      loggedInUrlPath: `${this.endpoint}/devportal/protected/organizations/${apiOrgId}/apis/${apiId}/versions/${apiVersion}/definition`
    };
    const definitionUrl = isLoggedIn ? urls.loggedInUrlPath : urls.urlPath;

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
        this.setApiKeyHeader(request);
        return request;
      },
      responseInterceptor: (response: SwaggerUI.Response) => {
        return response;
      },
      onComplete: () => {
        swaggerUI.preauthorizeApiKey(
          this.apiKeyHeader,
          this.contract?.client.apikey
        );
      }
    };

    this.setDisableAuth(swaggerOptions);
    this.cdr.detectChanges();
    const swaggerUI: SwaggerUI = SwaggerUI(swaggerOptions);
  }

  private setApiKeyHeader(request: SwaggerUI.Request) {
    if (this.contract) {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      request.headers[this.apiKeyHeader] = this.contract.client.apikey;
    }
  }

  private setAuthorizationHeader(request: SwaggerUI.Request) {
    const token = this.keycloakHelperService.getToken();
    if (token && token.length > 0) {
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
