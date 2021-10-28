import {Component, Input, OnInit} from '@angular/core';
import {ApiService} from '../../services/api/api.service';
import {PageEvent} from '@angular/material/paginator';
import {map, switchMap} from 'rxjs/operators';
import {forkJoin, Observable, of} from 'rxjs';
import {SpinnerService} from '../../services/spinner/spinner.service';
import {IApiSummary, ISearchCriteria} from '../../interfaces/ICommunication';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {IApiSummaryExt} from '../../interfaces/IApiSummaryExt';

@Component({
  selector: 'app-api-card-list',
  templateUrl: './api-card-list.component.html',
  styleUrls: ['./api-card-list.component.scss']
})
export class ApiCardListComponent implements OnInit {
  apis: IApiSummaryExt[] = [];
  totalSize = 0;
  ready = false;
  error = false;
  searchTerm = '';
  pageIndex = 0;
  searchCriteria: ISearchCriteria = {
    filters: [
      {
        name: 'name',
        value: '*',
        operator: 'like'
      }
    ],
    paging: {
      page: 1,
      pageSize: 8
    }
  };

  @Input() listType = '';
  allowPagination = false;

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
    this.route.queryParams.subscribe((params: Params) => {
      if (params.page) {
        this.searchCriteria.paging.page = params.page;
        this.pageIndex = params.page - 1;
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
      } else {
        this.searchTerm = '';
        this.searchCriteria.filters[0].value = '*';
      }

      if (this.listType === 'api') {
        this.getApiList();
      } else if (this.listType === 'featuredApi') {
        this.getFeaturedApiList();
      }
    });
  }

  OnInput(event: any): void {
    this.searchTerm = event.target.value;
    this.search(this.searchTerm);
  }

  public search(searchTerm: string) {
    this.router.navigate(['/marketplace'], {
      queryParams: {
        page: 1,
        pageSize: this.searchCriteria.paging.pageSize,
        searchTerm: `*${searchTerm}*`
      }
    });
  }

  OnPageChange(event: PageEvent): void {
    this.router.navigate(['/marketplace'], {
      queryParams: {
        page: event.pageIndex + 1,
        pageSize: event.pageSize,
        searchTerm: this.searchCriteria.filters[0].value
      }
    });
  }

  private getApiList(): void {
    this.loadingSpinnerService.startWaiting();
    this.ready = false;
    this.error = false;

    this.apiService.searchApis(this.searchCriteria).pipe(
      // map from SearchResultsBeanApiSummaryBean to IApiSummary[]
      map((searchResult) => {
        this.totalSize = searchResult.totalSize;
        return searchResult.beans;
      }),

      switchMap((apiSummaries: IApiSummary[]) => {
        return this.checkApisDocsInApiVersions(apiSummaries);
      })
    ).subscribe(
      (apiList: IApiSummaryExt[]) => {
        this.apis = apiList;
        this.loadingSpinnerService.stopWaiting();
        this.ready = true;
      },
      () => {
        this.error = true;
        this.loadingSpinnerService.stopWaiting();
      }
    );
  }

  private getFeaturedApiList() {
    this.loadingSpinnerService.startWaiting();
    this.ready = false;
    this.error = false;

    this.apiService
      .getFeaturedApis()
      .pipe(
        switchMap((apiSummaries: IApiSummary[]) => {
          return this.checkApisDocsInApiVersions(apiSummaries);
        })
      )
      .subscribe(
        (apiList: IApiSummaryExt[]) => {
          this.apis = apiList;
          this.loadingSpinnerService.stopWaiting();
          this.ready = true;
        },
        () => {
          this.error = true;
          this.loadingSpinnerService.stopWaiting();
        }
      );
  }

  private checkApisDocsInApiVersions(apiSummaries: IApiSummary[]) {
    if (apiSummaries.length > 0) {
      return forkJoin(
        apiSummaries.map((apiSummary) => {
          return this.apiService
            .getLatestApiVersion(apiSummary.organizationId, apiSummary.id)
            .pipe(
              switchMap((latestApiVersion) => {
                return this.apiService.isApiDocAvailable(latestApiVersion).pipe(
                  map((docsAvailable) => {
                    return {
                      ...apiSummary,
                      latestVersion: latestApiVersion.version,
                      docsAvailable: docsAvailable
                    } as IApiSummaryExt;
                  })
                );
              })
            );
        })
      );
    } else {
      return of([]) as Observable<IApiSummaryExt[]>;
    }
  }
}
