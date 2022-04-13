/*
 * Copyright 2022 Scheer PAS Schweiz AG
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
import { catchError, retry, switchMap } from 'rxjs/operators';
import {
  IApiSummary,
  IApiVersion,
  IApiVersionEndpointSummary,
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

  getFeaturedApis(): Observable<ISearchResultsApiSummary> {
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

  getManagedApiEndpoint(
    orgId: string,
    apiId: string,
    apiVersion: string
  ): Observable<IApiVersionEndpointSummary> {
    return this.backendService
      .getManagedApiEndpoint(orgId, apiId, apiVersion)
      .pipe(
        catchError(() => {
          return of({} as IApiVersionEndpointSummary);
        })
      );
  }

  isApiDocAvailable(apiVersion: IApiVersion): boolean {
    return (
      apiVersion.definitionType === 'SwaggerYAML' ||
      apiVersion.definitionType === 'SwaggerJSON'
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
    return throwError(() => new Error(errorMessage));
  }

  /**
   * This method will download the definition file of an API
   */
  downloadDefinitionFile(
    organizationId: string,
    apiId: string,
    apiVersion: string,
    definitionType: string
  ): void {
    this.backendService
      .getApiDefinition(organizationId, apiId, apiVersion)
      .subscribe((data) => {
        this.downloadFile(data, apiId, apiVersion, definitionType);
      });
  }

  /**
   * This method will create and name the definition file and creates the download
   * The name pattern is: apiName-apiVersion.fileEnding e.g. Petstore-1.0.json
   * @param data the definition as blob
   * @param apiId the name of the API
   * @param apiVersion the version of the API
   * @param definitionType the definition type of the API (SwaggerJSON, SwaggerYAML, WSDL)
   */
  private downloadFile(
    data: Blob,
    apiId: string,
    apiVersion: string,
    definitionType: string
  ): void {
    let type = 'text/json';
    let fileEnding = '.json';
    switch (definitionType) {
      case 'SwaggerJSON':
        type = 'text/json';
        fileEnding = '.json';
        break;
      case 'SwaggerYAML':
        type = 'text/yaml';
        fileEnding = '.yaml';
        break;
      case 'WSDL':
        type = 'text/xml';
        fileEnding = '.wsdl';
        break;
    }

    const downloadLink = document.createElement('a');
    const blob = new Blob([data], { type });
    downloadLink.href = window.URL.createObjectURL(blob);
    downloadLink.setAttribute(
      'download',
      apiId + '-' + apiVersion + fileEnding
    );
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
  }
}
