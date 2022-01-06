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
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { EMPTY, Observable } from 'rxjs';
import { ConfigService } from '../config/config.service';
import {
  IAction,
  IApi,
  IApiPlanSummary,
  IApiSummary,
  IApiVersion,
  IApiVersionEndpointSummary,
  IApiVersionSummary,
  IClient,
  IClientSummary,
  IClientVersionSummary,
  IContract,
  IContractSummary,
  ICurrentUser,
  INewContract,
  INewOrganization,
  IOrganization,
  IOrganizationSummary,
  IPolicy,
  IPolicySummary,
  ISearchCriteria,
  ISearchResultsApiSummary,
  ISearchResultsNotifications
} from '../../interfaces/ICommunication';
import { IPolicySummaryExt } from '../../interfaces/IPolicySummaryExt';
import { KeycloakHelperService } from '../keycloak-helper/keycloak-helper.service';
import { IPolicyProbe } from '../../interfaces/IPolicy';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class BackendService {
  private readonly endpoint: string;

  constructor(
    private http: HttpClient,
    private configService: ConfigService,
    private keycloakHelper: KeycloakHelperService
  ) {
    this.endpoint = configService.getEndpoint();
  }

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  /**
   * Searches apis
   */
  public searchApis(
    searchCriteria: ISearchCriteria
  ): Observable<ISearchResultsApiSummary> {
    const path = 'devportal/apis/search';
    return this.http.post(
      this.generateUrl(path),
      searchCriteria,
      this.httpOptions
    ) as Observable<ISearchResultsApiSummary>;
  }

  public getFeaturedApis(): Observable<IApiSummary[]> {
    const path = 'devportal/apis/featured';
    return this.http.get(
      this.generateUrl(path),
      this.httpOptions
    ) as Observable<IApiSummary[]>;
  }

  /**
   * Get Api
   */
  public getApi(orgId: string, apiId: string): Observable<IApi> {
    const path = `organizations/${orgId}/apis/${apiId}`;
    return this.http.get(
      this.generateUrl(path),
      this.httpOptions
    ) as Observable<IApi>;
  }

  /**
   * Get Api Versions
   */
  public getApiVersionSummaries(
    orgId: string,
    apiId: string
  ): Observable<IApiVersionSummary[]> {
    const path = `devportal/organizations/${orgId}/apis/${apiId}/versions`;
    return this.http.get<IApiVersionSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  /**
   * Get Api Version
   */
  public getApiVersion(
    orgId: string,
    apiId: string,
    version: string
  ): Observable<IApiVersion> {
    const path = `devportal/organizations/${orgId}/apis/${apiId}/versions/${version}`;
    return this.http.get<IApiVersion>(this.generateUrl(path), this.httpOptions);
  }

  public getClientOrgs(): Observable<Array<IOrganizationSummary>> {
    const username = this.keycloakHelper.getUsername();
    const path = `users/${username}/clientorgs`;
    return this.http.get(
      this.generateUrl(path),
      this.httpOptions
    ) as Observable<Array<IOrganizationSummary>>;
  }

  public createClient(orgId: string, clientName: string): Observable<IClient> {
    const path = `organizations/${orgId}/clients`;
    return this.http.post(
      this.generateUrl(path),
      {
        name: clientName,
        initialVersion: '1.0',
        description: ''
      },
      this.httpOptions
    ) as Observable<IClient>;
  }

  public createContract(
    organizationId: string,
    clientId: string,
    versionName: string,
    contract: INewContract
  ): Observable<IContract> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts`;
    return this.http.post(
      this.generateUrl(path),
      contract,
      this.httpOptions
    ) as Observable<IContract>;
  }

  public createOrganization(): Observable<IOrganization> {
    const org: INewOrganization = {
      name: this.keycloakHelper.getUsername(),
      description: ''
    };
    const path = 'devportal/organizations';
    return this.http.post<IOrganization>(
      this.generateUrl(path),
      org,
      this.httpOptions
    );
  }

  public getEditableClients(): Observable<IClientSummary[]> {
    const username = this.keycloakHelper.getUsername();
    const path = `users/${username}/editable-clients`;
    return this.http.get<IClientSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getViewableClients(): Observable<IClientSummary[]> {
    const username = this.keycloakHelper.getUsername();
    const path = `users/${username}/viewable-clients`;
    return this.http.get<IClientSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getClientVersions(
    organizationId: string,
    clientId: string
  ): Observable<IClientVersionSummary[]> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions`;
    return this.http.get<IClientVersionSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getContracts(
    organizationId: string,
    clientId: string,
    versionName: string
  ): Observable<IContractSummary[]> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts`;
    return this.http.get<IContractSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getContract(
    organizationId: string,
    clientId: string,
    versionName: string,
    contractId: number
  ): Observable<IContract> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts/${contractId}`;
    return this.http.get<IContract>(this.generateUrl(path), this.httpOptions);
  }

  public getManagedApiEndpoint(
    organizationId: string,
    apiId: string,
    versionName: string
  ): Observable<IApiVersionEndpointSummary> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${versionName}/endpoint`;
    return this.http.get<IApiVersionEndpointSummary>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getApiVersionPlans(
    organizationId: string,
    apiId: string,
    apiVersion: string
  ): Observable<IApiPlanSummary[]> {
    const path = `devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/plans`;
    return this.http.get<IApiPlanSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getPlanPolicySummaries(
    organizationId: string,
    planId: string,
    planVersion: string
  ): Observable<IPolicySummaryExt[]> {
    const path = `devportal/organizations/${organizationId}/plans/${planId}/versions/${planVersion}/policies`;
    return this.http.get<IPolicySummaryExt[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getPlanPolicy(
    organizationId: string,
    planId: string,
    planVersion: string,
    policyId: string
  ): Observable<IPolicy> {
    const path = `devportal/organizations/${organizationId}/plans/${planId}/versions/${planVersion}/policies/${policyId}`;
    return this.http.get<IPolicy>(this.generateUrl(path), this.httpOptions);
  }

  public getApiPolicySummaries(
    organizationId: string,
    apiId: string,
    apiVersion: string
  ): Observable<IPolicySummaryExt[]> {
    const path = `devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/policies`;
    return this.http.get<IPolicySummary[]>(
      this.generateUrl(path),
      this.httpOptions
    ) as Observable<IPolicySummaryExt[]>;
  }

  public getApiPolicy(
    organizationId: string,
    apiId: string,
    apiVersion: string,
    policyId: string
  ): Observable<IPolicy> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/policies/${policyId}`;
    return this.http.get<IPolicy>(this.generateUrl(path), this.httpOptions);
  }

  public getApiDefinition(
    organizationId: string,
    apiId: string,
    apiVersion: string
  ): Observable<Blob> {
    const path = `devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/definition`;
    return this.http.get(this.generateUrl(path), {
      responseType: 'blob'
    });
  }

  public sendAction(action: IAction): Observable<void> {
    console.log('sendAction');

    const path = `actions`;
    return this.http.post<void>(
      this.generateUrl(path),
      action,
      this.httpOptions
    );
  }

  public putNotifications(
    userName: string,
    notificationId: number
  ): Observable<HttpResponse<string>> {
    const path = `users/${userName}/notifications`;
    const body = {
      notificationIds: [notificationId],
      status: 'USER_DISMISSED'
    };
    return this.http.put(this.generateUrl(path), body, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      observe: 'response'
    }) as Observable<HttpResponse<string>>;
  }

  public postNotifications(
    userName: string,
    searchCriteria: ISearchCriteria
  ): Observable<ISearchResultsNotifications> {
    const path = `users/${userName}/notifications`;
    return this.http.post(
      this.generateUrl(path),
      searchCriteria,
      this.httpOptions
    ) as Observable<ISearchResultsNotifications>;
  }

  public headNotifications(userName: string): Observable<HttpResponse<string>> {
    const path = `users/${userName}/notifications`;
    return this.http.head(this.generateUrl(path), {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      observe: 'response'
    }) as Observable<HttpResponse<string>>;
  }

  public breakAllContracts(
    organizationId: string,
    clientId: string,
    clientVersion: string
  ): Observable<void> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${clientVersion}/contracts`;
    return this.http.delete<void>(this.generateUrl(path), this.httpOptions);
  }

  public deleteClient(
    organizationId: string,
    clientId: string
  ): Observable<void> {
    const path = `organizations/${organizationId}/clients/${clientId}`;
    return this.http.delete<void>(this.generateUrl(path), this.httpOptions);
  }

  public getPolicyProbe(
    contract: IContract,
    policy: IPolicy
  ): Observable<IPolicyProbe[]> {
    const path = `organizations/${contract.client.client.organization.id}/clients/${contract.client.id}/versions/${contract.client.version}/contracts/${contract.id}/policies/${policy.id}`;
    return this.http.post(
      this.generateUrl(path),
      { apiKey: contract.client.apikey },
      this.httpOptions
    ) as Observable<IPolicyProbe[]>;
  }

  public getCurrentUser(): Observable<ICurrentUser> {
    const path = 'users/currentuser/info';
    return this.http
      .get<ICurrentUser>(this.generateUrl(path), this.httpOptions)
      .pipe(
        catchError((err) => {
          console.error(err);
          return EMPTY;
        })
      );
  }

  /********* Helper **********/
  private generateUrl(path: string) {
    return `${this.endpoint}/${path}`;
  }
}
