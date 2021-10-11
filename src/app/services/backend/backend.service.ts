import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConfigService } from '../config/config.service';
import {
  IApi, IApiPlanSummary,
  IApiVersion,
  IApiVersionEndpointSummary,
  IApiVersionSummary, IClient, IClientSummary, IClientVersionSummary,
  IContract,
  IContractSummary,
  INewContract, INewOrganization,
  IOrganization,
  IOrganizationSummary, IPolicy, IPolicySummary,
  ISearchCriteria,
  ISearchResultsApiSummary,
} from '../../interfaces/ICommunication';
import { IPolicySummaryExt } from '../../interfaces/IPolicySummaryExt';
import {KeycloakHelperService} from '../keycloak-helper/keycloak-helper.service';

@Injectable({
  providedIn: 'root',
})
export class BackendService {
  private readonly endpoint: string;

  constructor(
    private http: HttpClient,
    private configService: ConfigService,
    private keycloakHelper: KeycloakHelperService
  ) {
    this.endpoint = configService.getEndpoint();
  }

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
    }),
  };

  /**
   * Searches apis
   */
  public searchApis(
    searchCriteria: ISearchCriteria
  ): Observable<ISearchResultsApiSummary> {
    const path = 'search/apis';
    return this.http.post(
      this.generateUrl(path),
      searchCriteria,
      this.httpOptions
    ) as Observable<ISearchResultsApiSummary>;
  }

  /**
   * Get Api
   */
  public getApi(orgId: string, apiId: string): Observable<IApi> {
    const path = `organizations/${orgId}/apis/${apiId}`;
    return this.http.get(
      this.generateUrl(path),
      this.httpOptions
    ) as Observable<IApi>;
  }

  /**
   * Get Api Versions
   */
  public getApiVersionSummaries(
    orgId: string,
    apiId: string
  ): Observable<IApiVersionSummary[]> {
    const path = `organizations/${orgId}/apis/${apiId}/versions`;
    return this.http.get<IApiVersionSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  /**
   * Get Api Version
   */
  public getApiVersion(
    orgId: string,
    apiId: string,
    version: string
  ): Observable<IApiVersion> {
    const path = `organizations/${orgId}/apis/${apiId}/versions/${version}`;
    return this.http.get<IApiVersion>(this.generateUrl(path), this.httpOptions);
  }

  public getClientOrgs(): Observable<Array<IOrganizationSummary>> {
    const username = this.keycloakHelper.getUsername();
    const path = `users/${username}/clientorgs`;
    return this.http.get(this.generateUrl(path)) as Observable<
      Array<IOrganizationSummary>
    >;
  }

  public createClient(
    orgId: string,
    clientName: string
  ): Observable<IClient> {
    const path = `organizations/${orgId}/clients`;
    return this.http.post(this.generateUrl(path), {
      name: clientName,
      initialVersion: '1.0',
      description: '',
    }) as Observable<IClient>;
  }

  public createContract(
    organizationId: string,
    clientId: string,
    versionName: string,
    contract: INewContract
  ): Observable<IContract> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts`;
    return this.http.post(
      this.generateUrl(path),
      contract
    ) as Observable<IContract>;
  }

  public createOrganization(): Observable<IOrganization> {
    const org: INewOrganization = {
      name: this.keycloakHelper.getUsername(),
      description: ''
    };
    const path = 'devportal/organizations';
    return this.http.post<IOrganization>(this.generateUrl(path), org);
  }

  public getEditableClients(): Observable<IClientSummary[]> {
    const username = this.keycloakHelper.getUsername();
    const path = `users/${username}/editable-clients`;
    return this.http.get<IClientSummary[]>(this.generateUrl(path));
  }

  public getClientVersions(
    organizationId: string,
    clientId: string
  ): Observable<IClientVersionSummary[]> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions`;
    return this.http.get<IClientVersionSummary[]>(this.generateUrl(path));
  }

  public getContracts(
    organizationId: string,
    clientId: string,
    versionName: string
  ): Observable<IContractSummary[]> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts`;
    return this.http.get<IContractSummary[]>(this.generateUrl(path));
  }

  public getContract(
    organizationId: string,
    clientId: string,
    versionName: string,
    contractId: number
  ): Observable<IContract> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts/${contractId}`;
    return this.http.get<IContract>(this.generateUrl(path)) ;
  }

  public getManagedApiEndpoint(
    organizationId: string,
    apiId: string,
    versionName: string
  ): Observable<IApiVersionEndpointSummary> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${versionName}/endpoint`;
    return this.http.get<IApiVersionEndpointSummary>(
      this.generateUrl(path)
    );
  }

  public getApiVersionPlans(organizationId: string,
                     apiId: string,
                     apiVersion: string): Observable<IApiPlanSummary[]> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/plans`
    return this.http.get<IApiPlanSummary[]>(this.generateUrl(path));
  }

  public getPlanPolicySummaries(organizationId: string,
                         planId: string,
                         planVersion: string): Observable<IPolicySummaryExt[]> {
    const path = `organizations/${organizationId}/plans/${planId}/versions/${planVersion}/policies`
    return this.http.get<IPolicySummaryExt[]>(this.generateUrl(path))
  }

  public getPlanPolicy(organizationId: string,
                       planId: string,
                       planVersion: string,
                       policyId: string): Observable<IPolicy> {
    const path = `organizations/${organizationId}/plans/${planId}/versions/${planVersion}/policies/${policyId}`
    return this.http.get<IPolicy>(this.generateUrl(path))
  }

  public getApiPolicySummaries(organizationId: string,
                               apiId: string,
                               apiVersion: string): Observable<IPolicySummaryExt[]> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/policies`
    return this.http.get<IPolicySummary[]>(this.generateUrl(path)) as Observable<IPolicySummaryExt[]>
  }

  public getApiPolicy(organizationId: string,
                       apiId: string,
                       apiVersion: string,
                       policyId: string): Observable<IPolicy> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/policies/${policyId}`
    return this.http.get<IPolicy>(this.generateUrl(path))
  }

  public headApiDefinition(organizationId: string,
                           apiId: string,
                           apiVersion:string): Observable<any> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/definition`;
    return this.http.head(this.generateUrl(path));
  }

  /********* Helper **********/
  private generateUrl(path: string) {
    return `${this.endpoint}/${path}`;
  }
}
