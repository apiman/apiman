/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

import { Injectable } from '@angular/core';
import { BackendService } from '../backend/backend.service';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, retry, switchMap } from 'rxjs/operators';
import {
  IApiSummary,
  IApiVersion,
  IApiVersionSummary,
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
