import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ConfigService } from '../config/config.service';

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

  constructor(private http: HttpClient, private configService: ConfigService) {
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
    searchCriteria: SearchCriteriaBean
  ): Observable<SearchResultsBeanApiSummaryBean> {
    const url = this.endpoint + '/search/apis';
    return this.http.post(
      url,
      searchCriteria,
      this.httpOptions
    ) as Observable<SearchResultsBeanApiSummaryBean>;
  }

  /**
   * Get Api
   */
  public getApi(orgId: string, apiId: string): Observable<ApiBean> {
    const url = this.endpoint + `/organizations/${orgId}/apis/${apiId}`;
    return this.http.get(url, this.httpOptions) as Observable<ApiBean>;
  }

  /**
   * Get Api Versions
   */
  public getApiVersions(
    orgId: string,
    apiId: string
  ): Observable<ApiVersionSummaryBean[]> {
    const url =
      this.endpoint + `/organizations/${orgId}/apis/${apiId}/versions`;
    return this.http.get<ApiVersionSummaryBean[]>(url, this.httpOptions).pipe(
      map((apiVersions) => {
        apiVersions.sort((a, b) => {
          // @ts-ignore
          return a.version > b.version ? -1 : 1;
        });
        return apiVersions;
      })
    );
  }
}
