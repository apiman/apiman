import {Inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';

export interface ApiSearchData {
  beans: Array<ApiDetails>;
  totalSize: number;
}

export interface ApiDetails {
  name: string;
  id: string;
  description: string;
  createdOn: string;
  organizationName: string;
  organizationId: string;
}

export interface ApiEndpointDetails {
  managedEndpoint: string;
}

export interface ApiKeyDetails {
  apiKey: string;
}

export interface ClientDetails {
  name: string;
  id: string;
  description: string;
  organizationName: string;
  organizationId: string;
  numContracts: number;
}

export interface ApiVersionsDetails {
  name: string;
  id: string;
  description: string;
  version: string;
  status: 'Created' | 'Ready' | 'Published' | 'Retired';
  organizationName: string;
  organizationId: string;
  publicAPI: boolean;
}

export interface ClientVersionDetails {
  name: string;
  id: string;
  description: string;
  version: string;
  status: 'Created' | 'Ready' | 'Registered' | 'Retired';
  apiKey: string;
  organizationName: string;
  organizationId: string;
}

export interface ContractDetails {
  createdOn: string;
  clientVersion: string;
  apiVersion: string;
  planVersion: string;
  clientOrganizationId: string;
  clientOrganizationName: string;
  apiOrganizationId: string;
  apiOrganizationName: string;
  apiDescription: string;
  clientId: string;
  planName: string;
  contractId: number;
  apiName: string;
  clientName: string;
  planId: string;
  apiId: string;
}

export interface ApiVersionDetails {
  id: number;
  version: string;
  status: 'Created' | 'Ready' | 'Published' | 'Retired';
  modifiedOn: string;
  createdOn: string;
  modifiedBy: string;
  createdBy: string;
  retiredOn: string;
  publishedOn: string;
  api: Api;
  definitionType: 'None' | 'SwaggerJSON' | 'SwaggerYAML' | 'WSDL' | 'WADL' | 'RAML' | 'External';
  endpointContentType: 'json' | 'xml';
  endpointProperties: object;
  endpointType: 'rest' | 'soap';
  plans: Array<Plan>;
  endpoint: string;
  publicAPI: boolean;
  parsePayload: boolean;
  gateways: Array<Gateway>;
}

export interface Plan {
  version: string;
  planId: string;
}

export interface Gateway {
  gatewayId: string;
}

export interface Api {
  name: string;
  id: string;
  description: string;
  organization: Organization;
  createdOn: string;
  createdBy: string;
  numPublished: number;
}

export interface Organization {
  name: string;
  id: string;
  description: string;
  modifiedOn: string;
  createdOn: string;
  modifiedBy: string;
  createdBy: string;
}

export interface GatewayDetails {
  name: string;
  id: string;
  type: 'REST';
  description: string;
}

export interface GatewayEndpoint {
  endpoint: string;
}

@Injectable({
  providedIn: 'root'
})

/**
 * A service which executes the REST calls to Apiman UI REST Interface
 */
export class ApiDataService {
  constructor(private http: HttpClient, @Inject('APIMAN_UI_REST_URL') private apimanUiRestUrl: string) { }

  public getAllApis(): Observable<ApiSearchData> {
    const url = this.apimanUiRestUrl + '/search/apis/';
    const body = { filters: [{name: 'name', value: '***', operator: 'like'}], page: 1, pageSize: 10000};
    return this.http.post(url, body) as Observable<ApiSearchData>;
  }

  public getApiEndpoint(organizationId, apiId, apiVersion) {
    const url = this.apimanUiRestUrl + '/organizations/' + organizationId + '/apis/' + apiId + '/versions/' + apiVersion + '/endpoint/';
    return this.http.get(url) as Observable<ApiEndpointDetails>;
  }

  public getApiKey(organizationId, clientId, clientVersion) {
    const url = this.apimanUiRestUrl + '/organizations/' + organizationId + '/clients/' + clientId + '/versions/' + clientVersion + '/apikey/';
    return this.http.get(url) as Observable<ApiKeyDetails>;
  }

  public getUserClients() {
    const url = this.apimanUiRestUrl + '/currentuser/clients/';
    return this.http.get(url) as Observable<Array<ClientDetails>>;
  }

  public getUserApis() {
    const url = this.apimanUiRestUrl + '/currentuser/apis/';
    return this.http.get(url) as Observable<Array<ApiDetails>>;
  }

  public getApiVersions(organizationId, apiId) {
    const url = this.apimanUiRestUrl + '/organizations/' + organizationId + '/apis/' + apiId + '/versions/';
    return this.http.get(url) as Observable<Array<ApiVersionsDetails>>;
  }

  public getApiVersionDetails(organizationId, apiId, apiVersion) {
    const url = this.apimanUiRestUrl  + '/organizations/' + organizationId + '/apis/' + apiId + '/versions/' + apiVersion;
    return this.http.get(url) as Observable<ApiVersionDetails>;
  }

  public getClientVersions(organizationId, clientId) {
    const url = this.apimanUiRestUrl + '/organizations/' + organizationId + '/clients/' + clientId + '/versions/';
    return this.http.get(url) as Observable<Array<ClientVersionDetails>>;
  }

  public getContracts(organizationId, clientId, clientVersion) {
    const url = this.apimanUiRestUrl + '/organizations/' + organizationId + '/clients/' + clientId + '/versions/' + clientVersion + '/contracts';
    return this.http.get(url) as Observable<Array<ContractDetails>>;
  }

  public getGateways() {
    const url = this.apimanUiRestUrl + '/gateways';
    return this.http.get(url) as Observable<Array<GatewayDetails>>;
  }

  public getGatewayEndpoint(gatewayId) {
    const url = this.apimanUiRestUrl + '/gateways/' + gatewayId + '/endpoint';
    return this.http.get(url) as Observable<GatewayEndpoint>;
  }

}
