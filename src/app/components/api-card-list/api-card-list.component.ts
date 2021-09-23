import { Component, Input, OnInit } from '@angular/core';
import { ApiService } from '../../services/api/api.service';
import { PageEvent } from '@angular/material/paginator';
import { switchMap, tap } from 'rxjs/operators';
import { forkJoin, Observable, of } from 'rxjs';
import { SpinnerService } from '../../services/spinner/spinner.service';
import {
  IApiListData,
  IApiSummary,
  ISearchCriteria,
  ISearchResult,
} from '../../interfaces/ICommunication';
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-api-card-list',
  templateUrl: './api-card-list.component.html',
  styleUrls: ['./api-card-list.component.scss'],
})
export class ApiCardListComponent implements OnInit {
  apis: IApiSummary[] = [];
  totalSize = 0;
  ready = false;
  searchTerm = '';
  searchCriteria: ISearchCriteria = {
    filters: [
      {
        name: 'name',
        value: '*',
        operator: 'like',
      },
    ],
    paging: {
      page: 1,
      pageSize: 8,
    },
  };

  @Input() listType = '';

  constructor(
    public apiService: ApiService,
    public loadingSpinnerService: SpinnerService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.apis = [];
    this.searchTerm = '';
    this.handleQueryParams();
  }

  handleQueryParams(): void {
    this.route.queryParams
      .subscribe((params) => {
        if (params.page) {
          this.searchCriteria.paging.page = params.page;
        } else {
          this.searchCriteria.paging.page = 1;
        }
        if (params.pageSize) {
          this.searchCriteria.paging.pageSize = params.pageSize;
        } else {
          this.searchCriteria.paging.pageSize = 8;
        }
        if (params.searchTerm) {
          this.searchTerm = params.searchTerm.replaceAll('*', '');
          this.searchCriteria.filters[0].value = params.searchTerm;
        } else{
          this.searchTerm = '';
          this.searchCriteria.filters[0].value = '*';
        }
        this.getApiList();
      })
  }

  OnInput(event: any): void {
    this.searchTerm = event.target.value;
    this.router.navigate(['/marketplace'],
      {
        queryParams: {
          page: 1,
          pageSize: this.searchCriteria.paging.pageSize,
          searchTerm: `*${event.target.value}*`
      }});
  }

  OnPageChange(event: PageEvent): void {
    this.router.navigate(['/marketplace'],
      {
        queryParams: {
          page: event.pageIndex + 1,
          pageSize: event.pageSize,
          searchTerm: this.searchCriteria.filters[0].value
        }});
  }

  getApiList(): void {
    const docsAvailable: Array<Observable<boolean>> = [];
    const result: ISearchResult = { apis: [], totalSize: 0 };

    this.loadingSpinnerService.startWaiting();
    this.ready = false;

    this.apiService
      .searchApis(this.searchCriteria)
      .pipe(
        // switch from SearchResultsBeanApiSummaryBean to ISearchResult
        switchMap((searchResult) => {
          result.apis = searchResult.beans;
          result.totalSize = searchResult.totalSize;
          return of(result);
        }),
        tap((searchResult) => {
          // Check API docs for every API
          searchResult.apis.forEach((api) => {
            docsAvailable.push(this.checkApiDocs(api));
          });
          // Set docsAvailable once every Request has finished
          forkJoin(docsAvailable).subscribe((result) => {
            result.forEach((eachResult, index) => {
              searchResult.apis[index].docsAvailable = eachResult;
            });
            this.loadingSpinnerService.stopWaiting();
            this.ready = true;
          });
        })
      )
      .subscribe((searchResult) => {
        if (this.listType === 'api') {
          this.apis = searchResult.apis;
        } else if (this.listType === 'featuredApi') {
          this.apis = searchResult.apis.slice(0,4);
        }
        this.totalSize = searchResult.totalSize;
      });
  }

  checkApiDocs(api: IApiListData): Observable<boolean> {
    return this.apiService.getLatestApiVersion(api.organizationId, api.id).pipe(
      tap((apiVersion) => (api.latestVersion = apiVersion.version)),
      switchMap((apiVersion) => {
        return of(
            apiVersion.definitionType !== null &&
            apiVersion.definitionType !== 'None'
        );
      })
    );
  }

  getFeaturedApis(): void {
    this.apiService
      .getFeaturedApis(this.searchCriteria)
      .subscribe((searchResult) => {
        this.apis = searchResult.beans;
      });
  }
}
