import { Injectable } from '@angular/core';
import { BackendService } from '../backend/backend.service';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, retry, switchMap } from 'rxjs/operators';
import {
  IApiSummary,
  IApiVersion,
  IApiVersionSummary,
  IContract,
  ISearchCriteria,
  ISearchResultsApiSummary
} from '../../interfaces/ICommunication';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private backendService: BackendService) {}

  getFeaturedApis(): Observable<IApiSummary[]> {
    return this.backendService.getFeaturedApis();
  }

  getApiVersionSummaries(
    orgId: string,
    apiId: string
  ): Observable<IApiVersionSummary[]> {
    return (
      this.backendService
        .getApiVersionSummaries(orgId, apiId)
        // eslint-disable-next-line @typescript-eslint/unbound-method
        .pipe(retry(1), catchError(this.handleError))
    );
  }

  searchApis(
    searchCriteria: ISearchCriteria
  ): Observable<ISearchResultsApiSummary> {
    return (
      this.backendService
        .searchApis(searchCriteria)
        // eslint-disable-next-line @typescript-eslint/unbound-method
        .pipe(retry(1), catchError(this.handleError))
    );
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

  isApiDocAvailable(apiSummary: IApiVersion): Observable<boolean> {
    return this.backendService
      .headApiDefinition(
        apiSummary.api.organization.id,
        apiSummary.api.id,
        apiSummary.version
      )
      .pipe(
        map(() => {
          return true;
        }),
        catchError(() => of(false))
      );
  }

  // Reminder: ApiVersions do not have api.api.image property
  // Therefore we need to use the search endpoint to receive the image
  getApiImage(contract: IContract): Observable<ISearchResultsApiSummary> {
    const searchCriteria: ISearchCriteria = {
      filters: [
        { name: 'name', value: contract.api.api.name, operator: 'like' }
      ],
      paging: { page: 1, pageSize: 1 }
    };

    return this.backendService.searchApis(searchCriteria);
  }

  handleError(error: HttpErrorResponse): Observable<never> {
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
    console.warn(errorMessage);
    return throwError(errorMessage);
  }
}
