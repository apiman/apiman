import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ConfigService} from '../config/config.service';
import {
  IAction,
  IApi,
  IApiPlanSummary,
  IApiSummary,
  IApiVersion,
  IApiVersionEndpointSummary,
  IApiVersionSummary,
  IClient,
  IClientSummary,
  IClientVersionSummary,
  IContract,
  IContractSummary,
  INewContract,
  INewOrganization,
  IOrganization,
  IOrganizationSummary,
  IPolicy,
  IPolicySummary,
  ISearchCriteria,
  ISearchResultsApiSummary,
  IUserPermissions
} from '../../interfaces/ICommunication';
import {IPolicySummaryExt} from '../../interfaces/IPolicySummaryExt';
import {KeycloakHelperService} from '../keycloak-helper/keycloak-helper.service';

@Injectable({
  providedIn: 'root'
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
      'Content-Type': 'application/json'
    })
  };

  /**
   * Searches apis
   */
  public searchApis(searchCriteria: ISearchCriteria): Observable<ISearchResultsApiSummary> {
    const path = 'devportal/apis/search';
    return this.http.post(this.generateUrl(path), searchCriteria, this.httpOptions) as Observable<ISearchResultsApiSummary>;
  }

  public getFeaturedApis(): Observable<IApiSummary[]> {
    const path = 'devportal/apis/featured';
    return this.http.get(this.generateUrl(path), this.httpOptions) as Observable<IApiSummary[]>;
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
    const path = `devportal/organizations/${orgId}/apis/${apiId}/versions`;
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
    const path = `devportal/organizations/${orgId}/apis/${apiId}/versions/${version}`;
    return this.http.get<IApiVersion>(this.generateUrl(path), this.httpOptions);
  }

  public getClientOrgs(): Observable<Array<IOrganizationSummary>> {
    const username = this.keycloakHelper.getUsername();
    const path = `users/${username}/clientorgs`;
    return this.http.get(
      this.generateUrl(path),
      this.httpOptions
    ) as Observable<Array<IOrganizationSummary>>;
  }

  public createClient(orgId: string, clientName: string): Observable<IClient> {
    const path = `organizations/${orgId}/clients`;
    return this.http.post(
      this.generateUrl(path),
      {
        name: clientName,
        initialVersion: '1.0',
        description: ''
      },
      this.httpOptions
    ) as Observable<IClient>;
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
      contract,
      this.httpOptions
    ) as Observable<IContract>;
  }

  public createOrganization(): Observable<IOrganization> {
    const org: INewOrganization = {
      name: this.keycloakHelper.getUsername(),
      description: ''
    };
    const path = 'devportal/organizations';
    return this.http.post<IOrganization>(
      this.generateUrl(path),
      org,
      this.httpOptions
    );
  }

  public getEditableClients(): Observable<IClientSummary[]> {
    const username = this.keycloakHelper.getUsername();
    const path = `users/${username}/editable-clients`;
    return this.http.get<IClientSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getClientVersions(
    organizationId: string,
    clientId: string
  ): Observable<IClientVersionSummary[]> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions`;
    return this.http.get<IClientVersionSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getContracts(
    organizationId: string,
    clientId: string,
    versionName: string
  ): Observable<IContractSummary[]> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts`;
    return this.http.get<IContractSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getContract(
    organizationId: string,
    clientId: string,
    versionName: string,
    contractId: number
  ): Observable<IContract> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${versionName}/contracts/${contractId}`;
    return this.http.get<IContract>(this.generateUrl(path), this.httpOptions);
  }

  public getManagedApiEndpoint(
    organizationId: string,
    apiId: string,
    versionName: string
  ): Observable<IApiVersionEndpointSummary> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${versionName}/endpoint`;
    return this.http.get<IApiVersionEndpointSummary>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getApiVersionPlans(
    organizationId: string,
    apiId: string,
    apiVersion: string
  ): Observable<IApiPlanSummary[]> {
    const path = `devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/plans`;
    return this.http.get<IApiPlanSummary[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getPlanPolicySummaries(
    organizationId: string,
    planId: string,
    planVersion: string
  ): Observable<IPolicySummaryExt[]> {
    const path = `devportal/organizations/${organizationId}/plans/${planId}/versions/${planVersion}/policies`;
    return this.http.get<IPolicySummaryExt[]>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public getPlanPolicy(
    organizationId: string,
    planId: string,
    planVersion: string,
    policyId: string
  ): Observable<IPolicy> {
    const path = `devportal/organizations/${organizationId}/plans/${planId}/versions/${planVersion}/policies/${policyId}`;
    return this.http.get<IPolicy>(this.generateUrl(path), this.httpOptions);
  }

  public getApiPolicySummaries(
    organizationId: string,
    apiId: string,
    apiVersion: string
  ): Observable<IPolicySummaryExt[]> {
    const path = `devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/policies`;
    return this.http.get<IPolicySummary[]>(
      this.generateUrl(path),
      this.httpOptions
    ) as Observable<IPolicySummaryExt[]>;
  }

  public getApiPolicy(
    organizationId: string,
    apiId: string,
    apiVersion: string,
    policyId: string
  ): Observable<IPolicy> {
    const path = `organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/policies/${policyId}`;
    return this.http.get<IPolicy>(this.generateUrl(path), this.httpOptions);
  }

  public headApiDefinition(
    organizationId: string,
    apiId: string,
    apiVersion: string
  ): Observable<any> {
    const path = `devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/definition`;
    return this.http.head(this.generateUrl(path), this.httpOptions);
  }

  public getPermissions(): Observable<IUserPermissions> {
    const userId = this.keycloakHelper.getUsername();

    const path = `users/${userId}/permissions`;
    return this.http.get<IUserPermissions>(
      this.generateUrl(path),
      this.httpOptions
    );
  }

  public sendAction(action: IAction): Observable<void> {
    const path = `actions`;
    return this.http.post<void>(
      this.generateUrl(path),
      action,
      this.httpOptions
    );
  }

  public breakAllContracts(
    organizationId: string,
    clientId: string,
    clientVersion: string
  ): Observable<void> {
    const path = `organizations/${organizationId}/clients/${clientId}/versions/${clientVersion}/contracts`;
    return this.http.delete<void>(this.generateUrl(path), this.httpOptions);
  }

  public deleteClient(organizationId: string, clientId: string) {
    const path = `organizations/${organizationId}/clients/${clientId}`;
    return this.http.delete<void>(this.generateUrl(path), this.httpOptions);
  }

  public getPolicyProbe(contract: IContract, policy: IPolicy): Observable<any> {
    const path = `organizations/${contract.client.client.organization.id}/clients/${contract.client.id}/versions/${contract.client.version}/contracts/${contract.id}/policies/${policy.id}`;
    return this.http.post(
      this.generateUrl(path),
      {apiKey: contract.client.apikey},
      this.httpOptions
    ) as Observable<any>;
  }

  /********* Helper **********/
  private generateUrl(path: string) {
    return `${this.endpoint}/${path}`;
  }
}
