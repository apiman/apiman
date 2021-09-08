import { Injectable } from '@angular/core';
import Axios from 'axios';

export interface ApiSummaryBean {
  organizationId?: string;
  organizationName?: string;
  id?: string;
  name?: string;
  description?: string;

  /** @format date-time */
  createdOn?: string;
}

export interface ApiBean {
  organization?: OrganizationBean;
  id?: string;
  name?: string;
  description?: string;
  createdBy?: string;

  /** @format date-time */
  createdOn?: string;

  /** @format int32 */
  numPublished?: number;
}

export interface ApiVersionSummaryBean {
  organizationId?: string;
  organizationName?: string;
  id?: string;
  name?: string;
  description?: string;
  status?: "Created" | "Ready" | "Published" | "Retired";
  version?: string;
  publicAPI?: boolean;
}

export interface OrganizationBean {
  id?: string;
  name?: string;
  description?: string;
  createdBy?: string;

  /** @format date-time */
  createdOn?: string;
  modifiedBy?: string;

  /** @format date-time */
  modifiedOn?: string;
}

export interface SearchResultsBeanApiSummaryBean {
  beans?: ApiSummaryBean[];

  /** @format int32 */
  totalSize?: number;
}

export interface SearchCriteriaBean {
  filters: SearchCriteriaFilterBean[];
  orderBy?: OrderByBean;
  paging: PagingBean;
}

export interface SearchCriteriaFilterBean {
  name?: string;
  value?: string;
  operator?: "bool_eq" | "eq" | "neq" | "gt" | "gte" | "lt" | "lte" | "like";
}

export interface OrderByBean {
  ascending?: boolean;
  name?: string;
}

export interface PagingBean {
  page?: number;
  pageSize?: number;
}

@Injectable({
  providedIn: 'root'
})

export class BackendService {

  constructor() { }

  // ToDo replace hard-coded URL with variable URL in config File
  private apiMgmtUiRestUrl: string = 'https://vagrantguest/pas/apiman';
  // ToDo remove credentials and use anonymous call
  private credentials: string = 'test:test1234' // Format username:password

  /**
   * Searches apis
   */
  public async searchApis(searchCriteria: SearchCriteriaBean): Promise<any> {
    const url = this.apiMgmtUiRestUrl + '/search/apis';
    return await Axios.post(url, searchCriteria,{
      headers: {
        'accept':  'application/json',
        'Authorization': 'Basic ' + btoa(this.credentials)
      }});
  }

  /**
   * Get Api
   */
  public async getApi(orgId: String, apiId: String): Promise<any> {
    const url = this.apiMgmtUiRestUrl + '/organizations/' + orgId + '/apis/' + apiId
    return await Axios.get(url,{
      headers: {
        'accept':  'application/json',
        'Authorization': 'Basic ' + btoa(this.credentials)
      }});
  }

  /**
   * Get Api Versions
   */
  public async getApiVersions(orgId: String, apiId: String): Promise<ApiVersionSummaryBean[]> {
    const url = this.apiMgmtUiRestUrl + '/organizations/' + orgId + '/apis/' + apiId + '/versions'
    return await Axios.get(url,{
      headers: {
        'accept':  'application/json',
        'Authorization': 'Basic ' + btoa(this.credentials)
      }});
  }
}
