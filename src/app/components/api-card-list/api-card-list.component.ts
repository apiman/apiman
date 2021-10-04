import { Component, Input, OnInit } from '@angular/core';
import { ApiService } from '../../services/api/api.service';
import { PageEvent } from '@angular/material/paginator';
import {map, switchMap } from 'rxjs/operators';
import { forkJoin } from 'rxjs';
import { SpinnerService } from '../../services/spinner/spinner.service';
import { IApiSummary, ISearchCriteria } from '../../interfaces/ICommunication';
import {ActivatedRoute, Router} from "@angular/router";
import {IApiSummaryExt} from "../../interfaces/IApiSummaryExt";
import {isApiDocAvailable} from "../../shared/utility";

@Component({
  selector: 'app-api-card-list',
  templateUrl: './api-card-list.component.html',
  styleUrls: ['./api-card-list.component.scss'],
})
export class ApiCardListComponent implements OnInit {
  apis: IApiSummaryExt[] = [];
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
    this.loadingSpinnerService.startWaiting();
    this.ready = false;

    this.apiService
      .searchApis(this.searchCriteria)
      .pipe(
        // map from SearchResultsBeanApiSummaryBean to IApiSummary[]
        map((searchResult) => {
          this.totalSize = searchResult.totalSize;
          return searchResult.beans;
        }),

        switchMap((apiSummaries: IApiSummary[]) => {
          return forkJoin(apiSummaries.map(apiSummary => {
            return this.apiService.getLatestApiVersion(apiSummary.organizationId, apiSummary.id).pipe(
              map(latestApiVersion => {
                return {
                  ...apiSummary,
                  latestVersion: latestApiVersion.version,
                  docsAvailable: isApiDocAvailable(latestApiVersion)
                } as IApiSummaryExt;
              })
            );
          }));
        })
      ).subscribe((apiList: IApiSummaryExt[]) => {
        if (this.listType === 'api') {
          this.apis = apiList;
        } else if (this.listType === 'featuredApi') {
          this.apis = apiList.slice(0,4);
        }
        this.loadingSpinnerService.stopWaiting();
        this.ready = true;
      });
  }

  //TODO implement real api call
/*  getFeaturedApis(): void {
    this.apiService
      .getFeaturedApis(this.searchCriteria)
      .subscribe((searchResult) => {
        this.apis = searchResult.beans;
      });
  }*/
}
