import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConfigService } from '../config/config.service';
import { KeycloakService } from 'keycloak-angular';
import {
  IApi,
  IApiVersion,
  IApiVersionSummary,
  IClient,
  IOrganization,
  IOrganizationSummary,
  ISearchCriteria,
  ISearchResultsApiSummary,
} from '../../interfaces/ICommunication';

@Injectable({
  providedIn: 'root',
})
export class BackendService {
  private readonly endpoint: string;

  constructor(
    private http: HttpClient,
    private configService: ConfigService,
    private keycloak: KeycloakService
  ) {
    this.endpoint = configService.getEndpoint();
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
}
