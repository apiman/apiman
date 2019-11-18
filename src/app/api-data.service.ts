import {Inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {from, Observable} from 'rxjs';
import {mergeMap} from 'rxjs/operators';

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
  retiredOn: string;
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
 * A service which executes the REST calls to Apiman UI REST Interface
 */
export class ApiDataService {

  /**
   * Contructor
   * @param http The http client
   * @param apimanUiRestUrl The apiman UI REST url
   */
  constructor(private http: HttpClient, @Inject('APIMAN_UI_REST_URL') private apimanUiRestUrl: string) { }

  /**
   * Get developer clients by developer id
   * @param developerId The developer Id
   */
  public getDeveloperClients(developerId: string) {
    const url = this.apimanUiRestUrl + '/developers/' + developerId + '/clients';
    return this.http.get(url) as Observable<Array<Client>>;
  }

  /**
   * Get developer contracts by developer id
   * @param developerId The developer Id
   */
  public getDeveloperContracts(developerId: string) {
    const url = this.apimanUiRestUrl + '/developers/' + developerId + '/contracts';
    return this.http.get(url) as Observable<Array<Contract>>;
  }

  /**
   * Get developer apis by developer id
   * @param developerId The developer Id
   */
  public getDeveloperApis(developerId: string) {
    const url = this.apimanUiRestUrl + '/developers/' + developerId + '/apis';
    return this.http.get(url) as Observable<Array<ApiVersion>>;
  }

  /**
   * Get available api gateways
   */
  public getGateways() {
    const url = this.apimanUiRestUrl + '/gateways';
    return this.http.get(url) as Observable<Array<GatewayDetails>>;
  }

  /**
   * Get gateway endpoint by gateway id
   * @param gatewayId The gateway id
   */
  public getGatewayEndpoint(gatewayId) {
    const url = this.apimanUiRestUrl + '/gateways/' + gatewayId + '/endpoint';
    return this.http.get(url) as Observable<GatewayEndpoint>;
  }

}
