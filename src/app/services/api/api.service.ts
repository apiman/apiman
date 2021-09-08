import { Injectable } from '@angular/core';
import {ApiBean, ApiSummaryBean, BackendService, SearchCriteriaBean} from "../backend/backend.service";

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  apis: ApiSummaryBean[] = [];
  currentApi: ApiBean = {};
  totalSize: number = 0;
  searchCriteria: SearchCriteriaBean = {
    filters: [{
      name: "name",
      value: "*",
      operator: "like"
    }],
    paging: {
      page: 1,
      pageSize: 8
    }
  };

  constructor(private backendService: BackendService) { }

  getFeaturedApis(): void {
    this.searchApis();
    this.apis= this.apis.slice(0, 4);
  }

  getApi(orgId: String, apiId: String): Promise<any> {
    return this.backendService.getApi(orgId, apiId);
  }

  searchApis(): void {
    this.backendService.searchApis(this.searchCriteria)
      .then(response => {
        this.totalSize = response.data.totalSize;
        this.apis = response.data.beans;
    });
  }
}
