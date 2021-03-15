/*
 * Copyright 2020 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {Injectable} from '@angular/core';

/**
 * Api Version
 */
export interface ApiVersion {
  id: number;
  api: Api;
  status: 'Created' | 'Ready' | 'Published' | 'Retired';
  endpoint: string;
  endpointType: 'rest' | 'soap';
  endpointContentType: 'json' | 'xml';
  endpointProperties: object;
  gateways: Array<Gateway>;
  publicAPI: boolean;
  plans: Array<Plan>;
  version: string;
  createdBy: string;
  createdOn: number;
  modifiedBy: string;
  modifiedOn: number;
  publishedOn: number;
  retiredOn: number;
  definitionType: 'None' | 'SwaggerJSON' | 'SwaggerYAML' | 'WSDL' | 'WADL' | 'RAML' | 'External';
  parsePayload: boolean;
  definitionUrl: string;
}

/**
 * Plan
 */
export interface Plan {
  version: string;
  planId: string;
}

/**
 * Gateway
 */
export interface Gateway {
  gatewayId: string;
}

/**
 * Api definition
 */
export interface Api {
  organization: Organization;
  id: string;
  name: string;
  description: string;
  createdBy: string;
  createdOn: number;
  numPublished: number;
}

/**
 * Organization
 */
export interface Organization {
  id: string;
  name: string;
  description: string;
  createdBy: string;
  createdOn: number;
  modifiedBy: string;
  modifiedOn: number;
}

/**
 * Client
 */
export interface Client {
  organizationId: string;
  organizationName: string;
  id: string;
  name: string;
  description: string;
  status: 'Created' | 'Ready' | 'Registered' | 'Retired';
  version: string;
  apiKey: string;
}

/**
 * Contract
 */
export interface Contract {
  contractId: number;
  clientOrganizationId: string;
  clientOrganizationName: string;
  clientId: string;
  clientName: string;
  clientVersion: string;
  apiOrganizationId: string;
  apiOrganizationName: string;
  apiId: string;
  apiName: string;
  apiVersion: string;
  apiDescription: string;
  planName: string;
  planId: string;
  planVersion: string;
  createdOn: string;
}

/**
 * Client Mapping for developer
 */
export interface ClientMapping {
  clientId: string;
  organizationId: string;
}

/**
 * Developer
 */
export interface Developer {
  id: string;
  clients: Array<ClientMapping>;
}

/**
 * Client Bean
 */
export interface ClientBean {
  name: string;
  id: string;
  description: string;
  createdOn: string;
  organizationName: string;
  organizationId: string;
}

/**
 * Client Search Result
 */
export interface ClientSearchResult {
  beans: Array<ClientBean>;
  totalSize: number;
}

/**
 * Gateway Details
 */
export interface GatewayDetails {
  name: string;
  id: string;
  type: 'REST';
  description: string;
}

/**
 * Gateway Endpoint
 */
export interface GatewayEndpoint {
  endpoint: string;
}

@Injectable({
  providedIn: 'root'
})

/**
 * A service which executes the REST calls to Api Mgmt UI REST Interface
 */
export class ApiDataService {

  private apiMgmtUiRestUrl: string = environment.apiMgmtUiRestUrl;

  /**
   * Constructor
   * @param http The http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Get all public apis
   */
  public getPublicApis() {
    const url = this.apiMgmtUiRestUrl + '/developers/apis';
    return this.http.get(url) as Observable<Array<ApiVersion>>;
  }

  /**
   * Get developer clients by developer id
   * @param developerId The developer Id
   */
  public getDeveloperClients(developerId: string) {
    const url = this.apiMgmtUiRestUrl + '/developers/' + developerId + '/clients';
    return this.http.get(url) as Observable<Array<Client>>;
  }

  /**
   * Get developer contracts by developer id
   * @param developerId The developer Id
   */
  public getDeveloperContracts(developerId: string) {
    const url = this.apiMgmtUiRestUrl + '/developers/' + developerId + '/contracts';
    return this.http.get(url) as Observable<Array<Contract>>;
  }

  /**
   * Get developer apis by developer id
   * @param developerId The developer Id
   */
  public getDeveloperApis(developerId: string) {
    const url = this.apiMgmtUiRestUrl + '/developers/' + developerId + '/apis';
    return this.http.get(url) as Observable<Array<ApiVersion>>;
  }

  /**
   * Get available api gateways
   */
  public getGateways() {
    const url = this.apiMgmtUiRestUrl + '/gateways';
    return this.http.get(url) as Observable<Array<GatewayDetails>>;
  }

  /**
   * Get gateway endpoint by gateway id
   * @param gatewayId The gateway id
   */
  public getGatewayEndpoint(gatewayId: string) {
    const url = this.apiMgmtUiRestUrl + '/gateways/' + gatewayId + '/endpoint';
    return this.http.get(url) as Observable<GatewayEndpoint>;
  }

  /**
   * Get the API Definition as blob
   * @param url the URL to the apiman definition
   */
  public getApiDefinition(url: string) {
    return this.http.get(url, {responseType: 'blob'});
  }
}
