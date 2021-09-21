import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConfigService } from '../config/config.service';
import { KeycloakService } from 'keycloak-angular';
import {
  IApi,
  IApiVersion,
  IApiVersionSummary,
  IClient, IContract, IContractSummary,
  IOrganization,
  IOrganizationSummary,
  ISearchCriteria,
  ISearchResultsApiSummary,
} from '../../interfaces/ICommunication';
import {
  IClientSummaryBean,
  IClientVersionSummaryBean
} from '../../interfaces/ICommunication';

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
  status?: 'Created' | 'Ready' | 'Published' | 'Retired';
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
  beans: ApiSummaryBean[];

  /** @format int32 */
  totalSize: number;
}

export interface SearchCriteriaBean {
  filters: SearchCriteriaFilterBean[];
  orderBy?: OrderByBean;
  paging: PagingBean;
}

export interface SearchCriteriaFilterBean {
  name?: string;
  value?: string;
  operator?: 'bool_eq' | 'eq' | 'neq' | 'gt' | 'gte' | 'lt' | 'lte' | 'like';
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
  providedIn: 'root',
})
export class BackendService {
  private readonly endpoint: string;
  userName: string;

  constructor(private http: HttpClient,
              private configService: ConfigService,
              private keycloak: KeycloakService) {
    this.endpoint = configService.getEndpoint();
    this.userName = this.keycloak.getKeycloakInstance().profile?.username!;
  }

  // ToDo remove credentials and use anonymous call
  private credentials = 'test:test1234'; // Format username:password
  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: 'Basic ' + btoa(this.credentials),
    }),
  };
  /**
   * Searches apis
   */
  public searchApis(
    searchCriteria: ISearchCriteria
  ): Observable<ISearchResultsApiSummary> {
    const url = this.endpoint + '/search/apis';
    return this.http.post(
      url,
      searchCriteria,
      this.httpOptions
    ) as Observable<ISearchResultsApiSummary>;
  }

  /**
   * Get Api
   */
  public getApi(orgId: string, apiId: string): Observable<IApi> {
    const url = this.endpoint + `/organizations/${orgId}/apis/${apiId}`;
    return this.http.get(url, this.httpOptions) as Observable<IApi>;
  }

  /**
   * Get Api Versions
   */
  public getApiVersionSummaries(
    orgId: string,
    apiId: string
  ): Observable<IApiVersionSummary[]> {
    const url =
      this.endpoint + `/organizations/${orgId}/apis/${apiId}/versions`;
    return this.http.get<IApiVersionSummary[]>(url, this.httpOptions);
  }

  /**
   * Get Api Version
   */
  public getApiVersion(
    orgId: string,
    apiId: string,
    version: string
  ): Observable<IApiVersion> {
    const url =
      this.endpoint +
      `/organizations/${orgId}/apis/${apiId}/versions/${version}`;
    return this.http.get<IApiVersion>(url, this.httpOptions);
  }

  public getClients(): Observable<Array<IClient>> {
    const username = this.keycloak.getKeycloakInstance().profile?.username;
    const url = this.endpoint + `/users/${username}/editable-clients`;
    return this.http.get(url) as Observable<Array<IClient>>;
  }

  public getClientOrgs(): Observable<Array<IOrganizationSummary>> {
    const username = this.keycloak.getKeycloakInstance().profile?.username;
    const url = this.endpoint + `/users/${username}/clientorgs`;
    return this.http.get(url) as Observable<Array<IOrganizationSummary>>;
  }

  public createClient(
    orgId: string,
    clientName: string
  ): Observable<IOrganization> {
    const url = this.endpoint + `/organizations/${orgId}/clients`;
    return this.http.post(url, {
      name: clientName,
      initialVersion: '1.0',
      description: '',
    }) as Observable<IOrganization>;
  }

  public getEditableClients(): Observable<Array<IClientSummaryBean>> {
    const path = `users/${this.userName}/editable-clients`;
    return this.http.get(this.generateUrl(path)) as Observable<Array<IClientSummaryBean>>;
  }

  public getClientVersions(organizationId: string, clientId: string): Observable<Array<IClientVersionSummaryBean>> {
    const path = `/organizations/${organizationId}/clients/${clientId}/versions`;
    return this.http.get(this.generateUrl(path)) as Observable<Array<IClientVersionSummaryBean>>;
  }

  public getContracts(organizationId: string, clientId: string, versionName: string): Observable<IContractSummary[]>  {
    const path = `/organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts`;
    return this.http.get(this.generateUrl(path)) as Observable<IContractSummary[]>;
  }

  public getContract(organizationId: string, clientId: string, versionName: string, contractId: number): Observable<Array<IContract>>  {
    const path = `/organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts/${contractId}`;
    return this.http.get(this.generateUrl(path)) as Observable<Array<IContract>>;
  }

  /********* Helper **********/
  private generateUrl(path: string){
    return `${this.endpoint}/${path}`;
  }

}
