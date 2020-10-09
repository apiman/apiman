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

import {Component, Inject, OnInit} from '@angular/core';
import {forkJoin, from, Observable, Subject} from 'rxjs';
import {concatMap, map, mergeAll, mergeMap, switchMap, toArray} from 'rxjs/operators';
import {ApiDataService, ApiVersion, Client, Contract} from '../../../services/api-data.service';
import {SpinnerService} from '../../../services/spinner.service';
import {ToasterService} from 'angular2-toaster';
import {Router} from '@angular/router';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {Sort} from '@angular/material/sort';
import {KeycloakService} from 'keycloak-angular';
import {formatDate} from '@angular/common';

export interface ApiListElement {
  id: string;
  apiName: string;
  apiOrganization: string;
  apiVersion: string;
  status: string;
  clientOrganization: string;
  clientName: string;
  clientVersion: string;
  endpoint: string;
  apikey: string;
  definitionType: string;
  apimanDefinitionUrl: string;
  swaggerURL: string;
  publicAPI: boolean;
  createdOn: number;
  modifiedOn: number;
  publishedOn: number;
  retiredOn: number;
}

class GatewayEndpoint {
  id: string;
  endpoint: string;

  constructor(id: string, endpoint: string) {
    this.id = id;
    this.endpoint = endpoint;
  }
}

@Component({
  selector: 'app-api-list',
  templateUrl: './api-list.component.html',
  styleUrls: ['./api-list.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})

export class ApiListComponent implements OnInit {

  columnHeaders: string[] = ['api', 'publicAPI', 'apiVersion', 'tryApi', 'options'];
  expandedElements: Array<ApiListElement> = [];

  apiData: Array<ApiListElement> = [];

  developerId: string = this.keycloak.getKeycloakInstance().profile.username;

  viewStatus = class ViewStatus {
    static readonly RETIRED = 'Retired';
    static readonly ACTIVE = 'Active';
    static readonly INACTIVE = 'Inactive';
  };
  backendStatus = class BackendStatus {
    static readonly STATUS_REGISTERED = 'Registered';
    static readonly STATUS_PUBLISHED = 'Published';
    static readonly STATUS_RETIRED = 'Retired';
  };

  constructor(private router: Router,
              private apiDataService: ApiDataService,
              private toasterService: ToasterService,
              private loadingSpinnerService: SpinnerService,
              private keycloak: KeycloakService,
              @Inject('API_MGMT_UI_REST_URL') private apiMgmtUiRestUrl: string) {
  }

  /**
   * An observer to get all gateway details (gateway endpoint) from api data service
   */
  getGatewayDataObservable: Observable<GatewayEndpoint[]> = this.apiDataService.getGateways()
    .pipe(mergeAll())
    .pipe(mergeMap(gateway => this.apiDataService.getGatewayEndpoint(gateway.id)
      .pipe(map(gatewayEndpoint => new GatewayEndpoint(gateway.id, gatewayEndpoint.endpoint)))
    ))
    .pipe(toArray());

  /**
   * An observer to get all public apis
   */
  getPublicApis = this.apiDataService.getPublicApis();

  /**
   * We only need this to get called once because we don't have more than one developer
   */
  ngOnInit() {
    this.loadingSpinnerService.startWaiting();
    const gatewaySubject = new Subject<Array<GatewayEndpoint>>();
    const ApiElementSubject = new Subject<ApiListElement>();
    const errorSubject = new Subject();

    // load the gateway data
    this.getGatewayDataObservable
      .pipe(map(gateways => gatewaySubject.next(gateways)))
      .subscribe(() => this.loadingSpinnerService.stopWaiting(), error => errorSubject.next(error));

    // load the public apis
    gatewaySubject.pipe(switchMap(gateways => {
        this.loadingSpinnerService.startWaiting();
        return this.getPublicApis.pipe( map (publicApis =>
          this.buildPublicApiViewData(gateways, publicApis)
        )).pipe(concatMap(x => x))
          .pipe(map(x => ApiElementSubject.next(x)));
      }
    )).subscribe(() => this.loadingSpinnerService.stopWaiting(), error => errorSubject.next(error));

    // load private apis
    gatewaySubject.pipe(switchMap(gateways => {
      this.loadingSpinnerService.startWaiting();
      return this.getPrivateApis(gateways, this.developerId);
    }))
      .pipe(map(x => ApiElementSubject.next(x)))
      .subscribe(() => this.loadingSpinnerService.stopWaiting(), error => errorSubject.next(error));

    // set the received api data to the view
    ApiElementSubject.subscribe(apiDataElement => {
      this.loadingSpinnerService.startWaiting();
      this.apiData = this.apiData.concat(apiDataElement);
      this.apiData.sort(((a, b) =>
        this.compare(a.apiOrganization + a.apiName + a.publicAPI, b.apiOrganization + b.apiName + b.publicAPI, true)));
      this.loadingSpinnerService.stopWaiting();
    });

    // handle the errors
    errorSubject.subscribe(error => {
      // 404 is fine, we don't have any APIs to load
      // @ts-ignore
      if (error.status && error.status !== 404) {
        const errorMessage = 'Error loading api list';
        console.error(errorMessage, error);
        // @ts-ignore
        this.toasterService.pop('error', errorMessage, error.message);
      }
      this.loadingSpinnerService.stopWaiting();
    });
  }

  /**
   * An observer to get all private apis
   */
  getPrivateApis: (gateways: Array<GatewayEndpoint>, developer: string) => Observable<ApiListElement> =
    (gateways: Array<GatewayEndpoint>, developer: string) =>
      forkJoin([this.apiDataService.getDeveloperClients(developer),
        this.apiDataService.getDeveloperContracts(developer),
        this.apiDataService.getDeveloperApis(developer)]
      ).pipe(mergeMap(forkedData => {
          const [clients, contracts, apiVersions] = forkedData;
          return from(contracts)
            .pipe(map(contract => {
              const apiVersion = apiVersions.find(version => version.api.id === contract.apiId
                && version.version === contract.apiVersion
                && version.api.organization.id === contract.apiOrganizationId);
              const clientVersion = clients.find(version => version.id === contract.clientId
                && version.version === contract.clientVersion
                && version.organizationId === contract.clientOrganizationId);
              const gateway = gateways.find(g => g.id === apiVersion.gateways[0].gatewayId);
              return this.buildPrivateApiViewData(contract, gateway, clientVersion, apiVersion);
        }));
    }))

  /**
   * Builds the data view object
   * @param contract: the contract between client and api
   * @param gateway: the gateway object which contains the gateway endpoint
   * @param clientVersion: the client version
   * @param apiVersionDetails: the api version data containing the apikey and gateway id
   */
  private buildPrivateApiViewData(contract: Contract, gateway: GatewayEndpoint, clientVersion: Client, apiVersionDetails: ApiVersion): ApiListElement {
    return {
      id: contract.apiId,
      apiName: contract.apiName,
      apiOrganization: contract.apiOrganizationName,
      apiVersion: contract.apiVersion,
      clientOrganization: contract.clientOrganizationName,
      clientName: contract.clientName,
      clientVersion: contract.clientVersion,
      status: this.computePrivateApiStatus(clientVersion.status, apiVersionDetails.status),
      endpoint: this.buildApiEndpoint(gateway.endpoint, contract.apiOrganizationId, contract.apiId, contract.apiVersion),
      apikey: clientVersion.apiKey,
      definitionType: apiVersionDetails.definitionType,
      apimanDefinitionUrl: this.getPrivateApiDefinitionUrl(contract),
      swaggerURL: this.getPrivateApiSwaggerURL(contract),
      publicAPI: false,
      createdOn: apiVersionDetails.createdOn,
      modifiedOn: apiVersionDetails.modifiedOn,
      publishedOn: apiVersionDetails.publishedOn,
      retiredOn: apiVersionDetails.retiredOn
    };
  }

  /**
   * Builds the private API Definition URL
   * @param contract the API contract
   */
  private getPrivateApiDefinitionUrl(contract: Contract) {
    return this.apiMgmtUiRestUrl
      + '/developers/' + this.developerId
      + '/organizations/' + contract.apiOrganizationId
      + '/apis/' + contract.apiId
      + '/versions/' + contract.apiVersion
      + '/definition';
  }

  /**
   * Builds the private API Swagger URL
   * @param contract the API contract
   */
  private getPrivateApiSwaggerURL(contract: Contract) {
    return '/swagger/developer/' + this.developerId
      + '/organizations/' + contract.apiOrganizationId
      + '/apis/' + contract.apiId
      + '/versions/' + contract.apiVersion;
  }

  /**
   * Converts the public api data to api list element
   * @param gateways: the gateways
   * @param publicApis the public apis
   */
  buildPublicApiViewData(gateways: Array<GatewayEndpoint> , publicApis: Array<ApiVersion>): Array<ApiListElement> {
    const result: Array<ApiListElement> = [];
    for (const api of publicApis) {
      const gateway = gateways.find(g => g.id === api.gateways[0].gatewayId);
      result.push({
        id: api.api.id,
        apiName: api.api.name,
        apiOrganization: api.api.organization.name,
        apiVersion: api.version,
        clientOrganization: null,
        clientName: null,
        clientVersion: null,
        status: this.computePublicApiStatus(api.status),
        endpoint: this.buildApiEndpoint(gateway.endpoint, api.api.organization.id, api.api.id, api.version),
        apikey: null,
        definitionType: api.definitionType,
        apimanDefinitionUrl: this.getPublicApiDefinitionUrl(api),
        swaggerURL: this.getPublicApiSwaggerUrl(api),
        publicAPI: true,
        createdOn: api.createdOn,
        modifiedOn: api.modifiedOn,
        publishedOn: api.publishedOn,
        retiredOn: api.retiredOn
      });
    }
    return result;
  }

  /**
   * Builds the API Definition URL
   * @param api the API
   */
  private getPublicApiDefinitionUrl(api: ApiVersion) {
    return this.apiMgmtUiRestUrl
      + '/developers/organizations/' + api.api.organization.id
      + '/apis/' + api.api.id
      + '/versions/' + api.version
      + '/definition';
  }

  /**
   * Builds the API Swagger URL
   * @param api the API
   */
  private getPublicApiSwaggerUrl(api: ApiVersion) {
    return '/swagger/developer/' + this.developerId
      + '/organizations/' + api.api.organization.id
      + '/apis/' + api.api.id
      + '/versions/' + api.version;
  }

  /**
   * Builds endpoint for a private api
   * @param gatewayEndpoint: the gateway endpoint
   * @param organizationId: organization of api
   * @param apiId: id of api
   * @param apiVersion: version of api
   */
  buildApiEndpoint(gatewayEndpoint: string, organizationId: string, apiId: string, apiVersion: string): string {
    return gatewayEndpoint + [organizationId, apiId, apiVersion].join('/');
  }

  /**
   * Copies endpoint to clipboard
   * @param entry the table entry
   */
  copyEndpointToClipboard(event, entry: ApiListElement) {
    const el = document.createElement('textarea');
    document.body.appendChild(el);
    el.value = entry.endpoint;
    if (!entry.publicAPI) {
      el.value += '?apiKey=' + entry.apikey;
    }
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);

    event.stopPropagation();
  }

  /**
   * Navigate to swagger view
   * @param entry the table entry
   */
  openSwaggerView(entry: ApiListElement) {
    this.router.navigate([entry.swaggerURL], {
      state: {
        data: {
          apikey: entry.apikey,
          apiStatus: entry.status,
          publicAPI: entry.publicAPI
        }
      }
    });
  }

  /**
   * This method will download the definition file of an API
   * @param event the click event to stop expanding of the table
   * @param entry the table entry
   */
  downloadDefinitionFile(event, entry: ApiListElement) {
    event.stopPropagation();
    this.apiDataService.getApiDefinition(entry.apimanDefinitionUrl).subscribe(data => {
      this.downloadFile(data, entry.apiName, entry.apiVersion, entry.definitionType);
    });
  }

  /**
   * This method will create and name the definition file and creates the download
   * The name pattern is: apiName-apiVersion.fileEnding e.g. Petstore-1.0.json
   * @param data the definition as blob
   * @param apiName the name of the API
   * @param apiVersion the version of the API
   * @param definitionType the definition type of the API (SwaggerJSON, SwaggerYAML, WSDL)
   */
  downloadFile(data: Blob, apiName: string, apiVersion: string, definitionType: string) {
    let type = 'text/json';
    let fileEnding = '.json';
    switch (definitionType) {
      case 'SwaggerJSON':
        type = 'text/json';
        fileEnding = '.json';
        break;
      case 'SwaggerYAML':
        type = 'text/yaml';
        fileEnding = '.yaml';
        break;
      case 'WSDL':
        type = 'text/xml';
        fileEnding = '.wsdl';
        break;
    }

    const downloadLink = document.createElement('a');
    const blob = new Blob([data], {type});
    downloadLink.href = window.URL.createObjectURL(blob);
    downloadLink.setAttribute('download', apiName + '-' + apiVersion + fileEnding);
    document.body.appendChild(downloadLink);
    downloadLink.click();
  }

  /**
   * Sorting data
   * @param sort sort event
   */
  sortData(sort: Sort) {
    const dataToSort = this.apiData.slice();
    if (!sort.active || sort.direction === '') {
      this.apiData = dataToSort;
      return;
    }
    dataToSort.sort((a, b) => {
        const isAsc = sort.direction === 'asc';
        switch (sort.active) {
          case 'api': return this.compare(a.apiOrganization + a.apiName, b.apiOrganization + b.apiName, isAsc);
          case 'version': return this.compare(a.apiVersion, b.apiVersion, isAsc);
          case 'clientVersion': return this.compare(a.clientVersion, b.clientVersion, isAsc);
          case 'publicAPI': return this.compare(a.publicAPI, b.publicAPI, isAsc);
          default: return 0;
        }
      });
    this.apiData = dataToSort;
  }

  /**
   * Compute combined status from API and Client Status
   * @param clientStatus the client status
   * @param apiStatus the api status
   */
  private computePrivateApiStatus(clientStatus, apiStatus): string {
    if (clientStatus === this.backendStatus.STATUS_RETIRED || apiStatus === this.backendStatus.STATUS_RETIRED) {
      return this.viewStatus.RETIRED;
    } else if (clientStatus === this.backendStatus.STATUS_REGISTERED && apiStatus === this.backendStatus.STATUS_PUBLISHED) {
      return this.viewStatus.ACTIVE;
    } else {
      return this.viewStatus.INACTIVE;
    }
  }

  /**
   * Compute status from API Status
   * @param apiStatus the api status
   */
  private computePublicApiStatus(apiStatus): string {
    if (apiStatus === this.backendStatus.STATUS_RETIRED) {
      return this.viewStatus.RETIRED;
    } else if (apiStatus === this.backendStatus.STATUS_PUBLISHED) {
      return this.viewStatus.ACTIVE;
    } else {
      return this.viewStatus.INACTIVE;
    }
  }

  /**
   * Compute tooltip status text
   * @param apiStatus the computed status text
   */
  public computeStatusText(apiElement: ApiListElement) {
    switch (apiElement.status) {
      case this.viewStatus.ACTIVE:
        return 'API is active since ' + formatDate(apiElement.publishedOn, 'short', 'en-US');
      case this.viewStatus.INACTIVE:
        return 'API is coming soon, created on ' + formatDate(apiElement.createdOn, 'short', 'en-US');
      default:
        return 'API is retired since ' + formatDate(apiElement.retiredOn, 'short', 'en-US');
    }
  }

  /**
   * Compares two values
   * @param a value
   * @param b value
   * @param isAsc is ascending sort
   */
  private compare(a: boolean | number | string, b: boolean | number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  /**
   * Is list element expanded
   * @param apiListElement the api list element
   */
  public isExpanded(apiListElement: ApiListElement) {
    return this.expandedElements.find((e) => e === apiListElement) !== undefined;
  }

  /**
   * Collapse list element
   * @param apiListElement the api list element
   */
  public collapse(apiListElement: ApiListElement) {
    this.expandedElements.splice(this.expandedElements.findIndex((e) => e === apiListElement), 1);
  }

  /**
   * Expand list element
   * @param apiListElement the api list element
   */
  public expand(apiListElement: ApiListElement) {
    this.expandedElements.push(apiListElement);
  }

  getApiIsPublicIcon(publicAPI: boolean): string {
    if (publicAPI) {
      return 'public';
    } else {
      return 'public_off';
    }
  }

  computeApiIsPublicText(publicAPI: boolean): string {
    if (publicAPI) {
      return 'API is public';
    } else {
      return 'API is private';
    }
  }
}
