import { Component, Input, SimpleChanges, OnChanges} from '@angular/core';
import {forkJoin, Observable, from, pipe} from 'rxjs';
import {map, mergeMap, toArray, mergeAll} from 'rxjs/operators';
import {ApiDataService, ApiVersion, Client, Contract} from '../api-data.service';
import {element} from 'protractor';
import {emit} from 'cluster';

export interface ApiListElement {
  id: string;
  name: string;
  version: string;
  endpoint: string;
  organizationName: string;
}

@Component({
  selector: 'api-list',
  templateUrl: './api-list.component.html',
  styleUrls: ['./api-list.component.scss']
})

export class ApiListComponent implements OnChanges {

  columnHeaders: string[] = ['ispublic', 'name', 'version', 'endpoint'];

  apiData: Array<ApiListElement> = [];

  @Input('developerId') developerId;

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
  getDeveloperData: (developer: string) => Observable<ApiListElement> = (developer: string) => forkJoin(this.apiDataService.getDeveloperClients(developer),
    this.apiDataService.getDeveloperContracts(developer),
    this.apiDataService.getDeveloperApis(developer),
    this.getGatewayDataObservable)
    .pipe(mergeMap(forkedData => {
      const [clients, contracts, apiVersions, gateways] = forkedData;
      return from(contracts)
        .pipe(map(contract => {
          const apiVersion = apiVersions.find(version => version.api.id === contract.apiId);
          const clientVersion = clients.find(version => version.id === contract.clientId);
          const gateway = gateways.find(g => g.id === apiVersion.gateways[0].gatewayId);
          return this.buildViewData(contract, gateway, clientVersion, apiVersion);
        }));
    }))

  constructor(private apiDataService: ApiDataService) {
  }

  /**
   * ngOnChanges is executed if the changes are applied to component
   */
  ngOnChanges(changes: SimpleChanges): void {
    //subscribe for the private api data to fill the view
    this.getDeveloperData(this.developerId).subscribe(data => {
      this.apiData = this.apiData.concat(data);
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
      name: contract.apiName,
      version: contract.apiVersion,
      endpoint: this.buildApiEndpoint(gateway.endpoint, contract.apiOrganizationId, contract.apiId, contract.apiVersion, clientVersion.apiKey),
      organizationName: contract.apiOrganizationId
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
    return gatewayEndpoint + [organizationId, apiId, apiVersion].join('/') + '?apiKey=' + apiKey;
  }

}
