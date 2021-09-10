import { Injectable } from '@angular/core';
import {
  ApiBean,
  ApiSummaryBean,
  ApiVersionSummaryBean,
  BackendService,
  SearchCriteriaBean, SearchResultsBeanApiSummaryBean
} from "../backend/backend.service";
import {Observable, throwError} from "rxjs";
import {catchError, map, retry} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  apis: ApiSummaryBean[] = [];
  currentApi!: Observable<ApiBean>;
  currentApiVersions!: Observable<ApiVersionSummaryBean[]>
  totalSize: number = 0;
  searchCriteria: SearchCriteriaBean = {
    filters: [{
      name: "name",
      value: "*",
      operator: "like"
    }],
    paging: {
      page: 1,
      pageSize: 8
    }
  };

  constructor(private backendService: BackendService) { }

  getFeaturedApis(searchCriteria: SearchCriteriaBean): Observable<SearchResultsBeanApiSummaryBean> {
    return this.searchApis(searchCriteria).pipe(
      map( searchResult => {
        searchResult.beans = searchResult.beans.slice(0,4)
        return searchResult
      })
     );
  }

  getApi(orgId: String, apiId: String): Observable<ApiBean> {
    return this.currentApi = this.backendService.getApi(orgId, apiId).pipe(
      retry(1),
      catchError(this.handleError)
    );
  }

  getApiVersions(orgId: String, apiId: String): Observable<ApiVersionSummaryBean[]> {
    return this.currentApiVersions = this.backendService.getApiVersions(orgId, apiId).pipe(
      retry(1),
      catchError(this.handleError)
    );
  }



  searchApis(searchCriteria: SearchCriteriaBean): Observable<SearchResultsBeanApiSummaryBean> {
    return this.backendService.searchApis(searchCriteria).pipe(
      retry(1),
      catchError(this.handleError)
    );
  }


  handleError(error: any) {
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
