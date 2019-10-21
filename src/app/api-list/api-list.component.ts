import { Component, OnInit } from '@angular/core';
import {forkJoin, Observable, from, pipe} from 'rxjs';
import {map, mergeMap, toArray, mergeAll} from 'rxjs/operators';
import {ApiDataService} from '../api-data.service';
import {element} from 'protractor';

export interface ApiListView {
  id: string;
  name: string;
  version: string;
  endpoint: string;
  ispublic: boolean;
}

@Component({
  selector: 'app-api-list',
  templateUrl: './api-list.component.html',
  styleUrls: ['./api-list.component.scss']
})

export class ApiListComponent implements OnInit {

  apiData: Array<ApiListView> = [];

  displayedColumns: string[] = ['ispublic', 'name', 'version', 'endpoint'];

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
   * An observer to access all data of private apis
   */
  getPrivateApiData: Observable<ApiListView> = this.getGatewayDataObservable.pipe(mergeMap(gateways =>
    this.apiDataService.getUserClients()
      .pipe(mergeAll())
      .pipe(mergeMap(userClient =>
        this.apiDataService.getClientVersions(userClient.organizationId, userClient.id)
          .pipe(mergeAll())
          .pipe(mergeMap(clientVersion =>
            this.apiDataService.getContracts(userClient.organizationId, userClient.id, clientVersion.version)
              .pipe(mergeAll())
              .pipe(mergeMap(contract =>
                //fork data to receive api key and api details
                forkJoin(
                  this.apiDataService.getApiKey(contract.clientOrganizationId, contract.clientId, contract.clientVersion)
                  //extract the api key
                    .pipe(map(source => source.apiKey)),
                  this.apiDataService.getApiVersionDetails(contract.clientOrganizationId, contract.apiId, contract.apiVersion)
                  //determine the first possible gateway endpoint and wether the api is public
                    .pipe(map(source => ({gatewayId: source.gateways[0].gatewayId, isPublic: source.publicAPI})))
                  //use the contract, gateway and forked api data to construct the view data
                ).pipe(map((forkedApiData) => (this.buildViewData(contract, gateways, forkedApiData[0], forkedApiData[1]))))
              ))
          ))
      ))
  ));

  constructor(private apiDataService: ApiDataService) {
  }

  /**
   * ngOnInit is executed if the component is initialized
   */
  ngOnInit() {
    this.getGatewayDataObservable.subscribe( gwData => {
      const test = gwData;
    });

    //subscribe for the private api data to fill the view
    this.getPrivateApiData.subscribe(data => {
      this.apiData = this.apiData.concat(data);
    });
  }

  /**
   * Builds the data view object
   * @param contract: the contract between client and api
   * @param gateways: the gateways which contains the gateway endpoints
   * @param apiVersionDetails: the api version data containing the apikey and gateway id
   */
  private buildViewData(contract, gateways, apiKey, apiVersionDetails) {
    return {
      id: contract.apiId,
      name: contract.apiName,
      version: contract.apiVersion,
      endpoint: this.buildApiEndpoint(gateways.find(g => g.id === apiVersionDetails.gatewayId).endpoint, contract.clientOrganizationId, contract.apiId, contract.apiVersion, apiKey),
      ispublic: apiVersionDetails.isPublic
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
  buildApiEndpoint(gatewayEndpoint: string, organizationId: string, apiId: string, apiVersion: string, apiKey: string) {
    return gatewayEndpoint + [organizationId, apiId, apiVersion].join('/') + '?apiKey=' + apiKey;
  }

}
