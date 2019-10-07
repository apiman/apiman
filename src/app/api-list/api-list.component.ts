import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import {ApiDataService, ClientDetails, ClientVersionDetails} from '../api-data.service';

export interface ApiListView {
  id: string;
  name: string;
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

  displayedColumns: string[] = ['id', 'ispublic', 'name', 'endpoint'];

  constructor(private apiDataService: ApiDataService) { }

  ngOnInit() {
    this.apiDataService.getUserClients().subscribe((data: Array<ClientDetails>) => {
      data.forEach((client) => {
        this.apiDataService.getClientVersions(client.organizationId, client.id).subscribe((clientData: Array<ClientVersionDetails>) => {
          clientData.forEach((clientDataDetail) => {
            this.apiDataService.getContracts(client.organizationId, client.id, clientDataDetail.version).subscribe((contractDetails) => {
              contractDetails.forEach((contractDetail) => {
                forkJoin(this.apiDataService.getApiEndpoint(contractDetail.apiOrganizationId, contractDetail.apiId, contractDetail.apiVersion), this.apiDataService.getApiKey(contractDetail.apiOrganizationId, contractDetail.clientId, contractDetail.clientVersion),
                  this.apiDataService.getApiVersionDetails(contractDetail.clientOrganizationId, contractDetail.apiId, contractDetail.apiVersion)).subscribe((forkedData) => {
                  this.apiData = this.apiData.concat({
                    id: contractDetail.apiId,
                    name: contractDetail.apiName,
                    endpoint: forkedData[0].managedEndpoint + '?apikey=' + forkedData[1].apiKey,
                    ispublic: forkedData[2].publicAPI
                  });
                });
              });
            });
          });
        });
      });
      });
    }
}
