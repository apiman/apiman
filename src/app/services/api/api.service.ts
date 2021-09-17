import { Injectable } from '@angular/core';
import { BackendService } from '../backend/backend.service';
import { Observable, throwError } from 'rxjs';
import { catchError, map, retry, switchMap } from 'rxjs/operators';
import {
  IApi,
  IApiSummary,
  IApiVersion,
  IApiVersionSummary,
  ISearchCriteria,
  ISearchResultsApiSummary,
} from '../../interfaces/ICommunication';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  apis: IApiSummary[] = [];
  currentApi!: Observable<IApi>;
  currentApiVersions!: Observable<IApiVersionSummary[]>;
  totalSize = 0;

  constructor(private backendService: BackendService) {}

  getFeaturedApis(
    searchCriteria: ISearchCriteria
  ): Observable<ISearchResultsApiSummary> {
    return this.searchApis(searchCriteria).pipe(
      map((searchResult) => {
        searchResult.beans = searchResult.beans.slice(0, 4);
        return searchResult;
      })
    );
  }

  getApi(orgId: string, apiId: string): Observable<IApi> {
    return (this.currentApi = this.backendService
      .getApi(orgId, apiId)
      .pipe(retry(1), catchError(this.handleError)));
  }

  getApiVersionSummaries(
    orgId: string,
    apiId: string
  ): Observable<IApiVersionSummary[]> {
    return (this.currentApiVersions = this.backendService
      .getApiVersionSummaries(orgId, apiId)
      .pipe(retry(1), catchError(this.handleError)));
  }

  searchApis(
    searchCriteria: ISearchCriteria
  ): Observable<ISearchResultsApiSummary> {
    return this.backendService
      .searchApis(searchCriteria)
      .pipe(retry(1), catchError(this.handleError));
  }

  getApiVersion(
    orgId: string,
    apiId: string,
    apiVersion: string
  ): Observable<IApiVersion> {
    return this.backendService.getApiVersion(orgId, apiId, apiVersion);
  }

  getLatestApiVersion(orgId: string, apiId: string): Observable<IApiVersion> {
    let latestApiVersionSummary: IApiVersionSummary;
    return this.backendService.getApiVersionSummaries(orgId, apiId).pipe(
      switchMap((apiVersions) => {
        latestApiVersionSummary = apiVersions[0];
        return this.backendService.getApiVersion(
          latestApiVersionSummary.organizationId,
          latestApiVersionSummary.id,
          latestApiVersionSummary.version
        );
      })
    );
  }

  handleError(error: any): Observable<never> {
    let errorMessage = '';
    if (error.error instanceof ErrorEvent) {
      // client-side error
      // ToDo do something meaningful with error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // server-side error
      // ToDo do something meaningful with error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    window.alert(errorMessage);
    return throwError(errorMessage);
  }
}
