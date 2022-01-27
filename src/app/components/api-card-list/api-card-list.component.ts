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

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ApiService } from '../../services/api/api.service';
import { PageEvent } from '@angular/material/paginator';
import { debounceTime, map, switchMap, takeUntil } from 'rxjs/operators';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import { SpinnerService } from '../../services/spinner/spinner.service';
import { IApiSummary, ISearchCriteria } from '../../interfaces/ICommunication';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { IApiSummaryExt } from '../../interfaces/IApiSummaryExt';

@Component({
  selector: 'app-api-card-list',
  templateUrl: './api-card-list.component.html',
  styleUrls: ['./api-card-list.component.scss']
})
export class ApiCardListComponent implements OnInit, OnDestroy {
  apis: IApiSummaryExt[] = [];
  totalSize = 0;
  ready = false;
  error = false;
  searchTerm = '';
  searchTermNotifier = new Subject();
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
    this.initSearchDebounce();
    this.handleQueryParams();
  }

  private initSearchDebounce() {
    // https://m.clearbluedesign.com/how-to-simple-angular-debounce-using-rxjs-e7b86fde6167
    this.searchTermNotifier.pipe(debounceTime(300)).subscribe(() => {
      this.search(this.searchTerm);
    });
  }

  handleQueryParams(): void {
    this.route.queryParams.subscribe((params: Params) => {
      if (params && params.page) {
        this.searchCriteria.paging.page = params.page as number;
        this.pageIndex = params.page - 1;
      } else {
        this.searchCriteria.paging.page = 1;
      }
      if (params && params.pageSize) {
        this.searchCriteria.paging.pageSize = params.pageSize as number;
      } else {
        this.searchCriteria.paging.pageSize = 8;
      }
      let searchTerm = '';
      if (params && params.searchTerm) {
        searchTerm = params.searchTerm as string;
        this.searchTerm = searchTerm.replaceAll('*', '');
        this.searchCriteria.filters[0].value = searchTerm;
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

  public search(searchTerm: string): void {
    void this.router.navigate(['/marketplace'], {
      queryParams: {
        page: 1,
        pageSize: this.searchCriteria.paging.pageSize,
        searchTerm: `*${searchTerm}*`
      }
    });
  }

  OnPageChange(event: PageEvent): void {
    void this.router.navigate(['/marketplace'], {
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

    this.apiService
      .searchApis(this.searchCriteria)
      .pipe(
        // takeUntil cancels the search if a new search input is made
        takeUntil(this.searchTermNotifier),
        // map from SearchResultsBeanApiSummaryBean to IApiSummary[]
        map((searchResult) => {
          this.totalSize = searchResult.totalSize;
          return searchResult.beans;
        }),

        switchMap((apiSummaries: IApiSummary[]) => {
          return this.checkApisDocsInApiVersions(apiSummaries);
        })
      )
      .subscribe({
        next: (apiList: IApiSummaryExt[]) => {
          this.apis = apiList;
          this.loadingSpinnerService.stopWaiting();
          this.ready = true;
        },
        // eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any
        error: (err: any) => {
          console.error(err);
          this.error = true;
          this.loadingSpinnerService.stopWaiting();
        }
      });
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
      .subscribe({
        next: (apiList: IApiSummaryExt[]) => {
          this.apis = apiList;
          this.loadingSpinnerService.stopWaiting();
          this.ready = true;
        },
        // eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any
        error: (err: any) => {
          console.error(err);
          this.error = true;
          this.loadingSpinnerService.stopWaiting();
        }
      });
  }

  private checkApisDocsInApiVersions(apiSummaries: IApiSummary[]) {
    if (apiSummaries.length > 0) {
      return forkJoin(
        apiSummaries.map((apiSummary) => {
          return this.apiService
            .getLatestApiVersion(apiSummary.organizationId, apiSummary.id)
            .pipe(
              map((latestApiVersion) => {
                return {
                  ...apiSummary,
                  latestApiVersion: latestApiVersion,
                  docsAvailable:
                    this.apiService.isApiDocAvailable(latestApiVersion)
                } as IApiSummaryExt;
              })
            );
        })
      );
    } else {
      return of([]) as Observable<IApiSummaryExt[]>;
    }
  }

  ngOnDestroy(): void {
    this.searchTermNotifier.complete();
  }
}
