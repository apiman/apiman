import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {forkJoin, from, Observable, ObservedValueOf} from 'rxjs';
import {map, mergeAll, mergeMap, toArray} from 'rxjs/operators';
import {ApiDataService, ApiVersion, Client, Contract} from '../../../services/api-data.service';
import {SpinnerService} from '../../../services/spinner.service';
import {Toast, ToasterService} from 'angular2-toaster';
import {Router} from '@angular/router';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {Sort} from '@angular/material';

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
  swaggerDefinitionType: string;
  swaggerURL: string;
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

export class ApiListComponent implements OnChanges {

  columnHeaders: string[] = ['api', 'apiVersion', 'tryApi', 'options'];
  expandedElements: Array<ApiListElement> = [];

  apiData: Array<ApiListElement> = [];
  sortedApiData: Array<ApiListElement>;

  status = class ApiStatus {
    static retired = 'Retired';
    static active = 'Active';
    static inactive = 'Inactive';
  };

  @Input('developerId') developerId;

  constructor(private router: Router,
              private apiDataService: ApiDataService,
              private toasterService: ToasterService,
              private loadingSpinnerService: SpinnerService) {
    this.sortedApiData = this.apiData;
  }

  /**
   * An observer to get all gateway details (gateway endpoint) from api data service
   */
  getGatewayDataObservable = this.apiDataService.getGateways()
    .pipe(mergeAll())
    .pipe(mergeMap(gateway => this.apiDataService.getGatewayEndpoint(gateway.id)
      .pipe(map(gatewayEndpoint => ({id: gateway.id, endpoint: gatewayEndpoint.endpoint})))
    ))
    .pipe(toArray());

  /**
   * An observer to get all developer data from api data service
   */
  getDeveloperData: (developer: string) => Observable<ApiListElement> = (developer: string) => forkJoin([this.apiDataService.getDeveloperClients(developer),
    this.apiDataService.getDeveloperContracts(developer),
    this.apiDataService.getDeveloperApis(developer),
    this.getGatewayDataObservable])
    .pipe(mergeMap(forkedData => {
      const [clients, contracts, apiVersions, gateways] = forkedData;
      this.checkReceivedData(clients, contracts, apiVersions, gateways);
      return from(contracts)
        .pipe(map(contract => {
          const apiVersion = apiVersions.find(version => version.api.id === contract.apiId
            && version.version === contract.apiVersion
            && version.api.organization.id === contract.apiOrganizationId);
          const clientVersion = clients.find(version => version.id === contract.clientId
            && version.version === contract.clientVersion
            && version.organizationId === contract.clientOrganizationId);
          const gateway = gateways.find(g => g.id === apiVersion.gateways[0].gatewayId);
          return this.buildViewData(contract, gateway, clientVersion, apiVersion);
        }));
    }));

  /**
   * ngOnChanges is executed if the changes are applied to component
   */
  ngOnChanges(changes: SimpleChanges): void {
    this.loadingSpinnerService.startWaiting();
    // subscribe for the private api data to fill the view
    this.getDeveloperData(this.developerId).subscribe(data => {
      this.apiData = this.apiData.concat(data).sort(((a, b) => this.compare(a.apiOrganization + a.apiName, b.apiOrganization + b.apiName, true)));
      this.sortedApiData = this.apiData;
      this.loadingSpinnerService.stopWaiting();
    }, error => {
      const errorMessage = 'Error loading api list';
      console.error(errorMessage, error);
      this.toasterService.pop('error', errorMessage, error.message);
      this.loadingSpinnerService.stopWaiting();
    });
  }

  /**
   * Builds the data view object
   * @param contract: the contract between client and api
   * @param gateways: the gateways which contains the gateway endpoints
   * @param apiVersionDetails: the api version data containing the apikey and gateway id
   */
  private buildViewData(contract: Contract, gateway, clientVersion: Client, apiVersionDetails: ApiVersion): ApiListElement {
    return {
      id: contract.apiId,
      apiName: contract.apiName,
      apiOrganization: contract.apiOrganizationName,
      apiVersion: contract.apiVersion,
      clientOrganization: contract.clientOrganizationName,
      clientName: contract.clientName,
      clientVersion: contract.clientVersion,
      status: this.computeStatus(clientVersion.status, apiVersionDetails.status),
      endpoint: this.buildApiEndpoint(gateway.endpoint, contract.apiOrganizationId, contract.apiId, contract.apiVersion, clientVersion.apiKey),
      apikey: clientVersion.apiKey,
      swaggerDefinitionType: apiVersionDetails.definitionType,
      swaggerURL: '/swagger/developer/' + this.developerId + '/organizations/' + contract.apiOrganizationId + '/apis/' + contract.apiId + '/versions/' + contract.apiVersion
    };
  }

  /**
   * Builds endpoint for a private api
   * @param gatewayEndpoint: the gateway endpoint
   * @param organizationId: organization of api
   * @param apiId: id of api
   * @param apiVersion: version of api
   * @param apiKey: apikey of api
   */
  buildApiEndpoint(gatewayEndpoint: string, organizationId: string, apiId: string, apiVersion: string, apiKey: string): string {
    return gatewayEndpoint + [organizationId, apiId, apiVersion].join('/');
  }

  /**
   * Copies endpoint to clipboard
   * @param entry the table entry
   */
  copyEndpointToClipboard(event, entry: ApiListElement) {
    const el = document.createElement('textarea');
    document.body.appendChild(el);
    el.value = entry.endpoint + '?apiKey=' + entry.apikey;
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
          apiStatus: entry.status
        }
      }
    });
  }

  /**
   * Sorting data
   * @param sort sort event
   */
  sortData(sort: Sort) {
    const dataToSort = this.apiData.slice();
    if (!sort.active || sort.direction === '') {
      this.sortedApiData = dataToSort;
      return;
    }

    this.sortedApiData = dataToSort.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case 'api': return this.compare(a.apiOrganization + a.apiName, b.apiOrganization + b.apiName, isAsc);
        case 'version': return this.compare(a.apiVersion, b.apiVersion, isAsc);
        case 'clientVersion': return this.compare(a.clientVersion, b.clientVersion, isAsc);
        default: return 0;
      }
    });
  }

  /**
   * Compute combined status from API and Client Status
   * @param clientStatus the client status
   * @param apiStatus the api status
   */
  private computeStatus(clientStatus, apiStatus): string {
    const registered = 'Registered';
    const published = 'Published';
    const retired = 'Retired';

    if (clientStatus === retired || apiStatus === retired) {
      return this.status.retired;
    } else if (clientStatus === registered && apiStatus === published) {
      return this.status.active;
    } else {
      return this.status.inactive;
    }
  }

  /**
   * Compute tooltip status text
   * @param apiStatus the computed status text
   */
  public computeStatusText(apiStatus) {
    switch (apiStatus) {
      case this.status.active:
        return 'API is active';
      case this.status.inactive:
        return 'API is coming soon';
      default:
        return 'API is retired';
    }
  }

  /**
   * Compares two values
   * @param a value
   * @param b value
   * @param isAsc is ascending sort
   */
  private compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  /**
   * Check the received data and provide user feedback
   * @param clients the loaded clients
   * @param contracts the loaded contracts
   * @param apiVersions the loaded api versions
   * @param gateways the loaded gateways
   */
  private checkReceivedData(clients: Array<Client>,
                            contracts: Array<Contract>,
                            apiVersions: Array<ApiVersion>,
                            gateways: ObservedValueOf<Observable<{ endpoint: string; id: any }>>[]) {
    let hasError = false;
    if (clients.length === 0) {
      hasError = true;
      this.loadingSpinnerService.stopWaiting();
      this.toasterService.pop('warning', 'No clients available', 'Let the admin check the client mapping');
    }
    if (!hasError && contracts.length === 0) {
      hasError = true;
      this.loadingSpinnerService.stopWaiting();
      this.toasterService.pop('warning', 'No contracts available');
    }
    if (!hasError && apiVersions.length === 0) {
      hasError = true;
      this.loadingSpinnerService.stopWaiting();
      this.toasterService.pop('info', 'No api versions available');
    }
    if (gateways.length === 0) {
      this.loadingSpinnerService.stopWaiting();
      this.toasterService.pop('warning', 'No gateway data available');
    }
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
}
