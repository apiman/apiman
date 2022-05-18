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
import {
  debounceTime,
  map,
  switchMap,
  takeUntil,
  throttleTime
} from 'rxjs/operators';
import { forkJoin, fromEvent, Observable, of, Subject } from 'rxjs';
import { SpinnerService } from '../../services/spinner/spinner.service';
import {
  IApiSummary,
  ISearchCriteria,
  ISearchResultsApiSummary
} from '../../interfaces/ICommunication';
import { IApiSummaryExt } from '../../interfaces/IApiSummaryExt';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-api-card-list',
  templateUrl: './api-card-list.component.html',
  styleUrls: ['./api-card-list.component.scss']
})
export class ApiCardListComponent implements OnInit, OnDestroy {
  apis: IApiSummaryExt[] = [];
  apis$: Observable<IApiSummaryExt[]> = of([]);
  currentPage = 0;
  pageSize = 24;
  totalSize = 0;
  ready = false;
  error = false;
  loadingMoreApis = false;
  searchTerm = '';
  searchTermNotifier = new Subject();
  pageIndex = 0;
  showErrorSnackbar = false;

  @Input() listType = '';

  constructor(
    public apiService: ApiService,
    public loadingSpinnerService: SpinnerService,
    private snackbarService: SnackbarService,
    private translatorService: TranslateService
  ) {
    this.initScrollEventListener();
  }

  /**
   * Initializes the event listener for scroll events.
   */
  private initScrollEventListener() {
    fromEvent(window, 'scroll')
      .pipe(throttleTime(200))
      .subscribe(() => {
        const scrollPosition = window.scrollY;
        const windowSize = window.innerHeight;
        const bodyHeight = document.body.offsetHeight;
        if (
          bodyHeight - (scrollPosition + windowSize) < 400 &&
          this.totalSize > this.currentPage * this.pageSize
        ) {
          this.fetchApis(false);
        }
      });
  }

  ngOnInit(): void {
    this.apis = [];
    this.searchTerm = '';
    this.initSearchDebounce();
    this.fetchApis(true);
  }

  /**
   * Fetches APIs from the backend, which will extend or reset the entire API list.
   *
   * @param resetApiList - If set to true, the apiList will be reset and not extended.
   */
  public fetchApis(resetApiList: boolean) {
    let apiSearchResult$: Observable<ISearchResultsApiSummary>;
    if (resetApiList) {
      this.loadingSpinnerService.startWaiting();
      this.ready = false;
      this.error = false;
      this.currentPage = 1;
    } else {
      this.currentPage = this.currentPage + 1;
      this.loadingMoreApis = true;
    }

    const searchCriteria: ISearchCriteria = {
      filters: [
        {
          name: 'name',
          value: this.searchTerm ? `*${this.searchTerm}*` : '*',
          operator: 'like'
        }
      ],
      paging: {
        page: this.currentPage,
        pageSize: this.pageSize
      }
    };
    if (this.listType === 'api') {
      apiSearchResult$ = this.apiService.searchApis(searchCriteria);
    } else {
      apiSearchResult$ = this.apiService.getFeaturedApis();
    }
    apiSearchResult$
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
          if (resetApiList) {
            this.apis = apiList;
          } else {
            this.apis = [...this.apis, ...apiList];
          }
          if (this.showErrorSnackbar) {
            this.snackbarService.showErrorSnackBar(
              this.translatorService.instant(
                'MPLACE.ERROR_WHILE_FETCHING_APIS'
              ) as string
            );
            this.showErrorSnackbar = false;
          }
          this.apis$ = of(this.apis);
          this.loadingSpinnerService.stopWaiting();
          this.loadingMoreApis = false;
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

  /**
   * Initializes the debounceTime for the search input.
   */
  private initSearchDebounce() {
    // https://m.clearbluedesign.com/how-to-simple-angular-debounce-using-rxjs-e7b86fde6167
    this.searchTermNotifier.pipe(debounceTime(300)).subscribe(() => {
      this.fetchApis(true);
    });
  }

  /**
   * Checks if API Documentation is available for the APIs.
   *
   * @param apiSummaries - Array of API summaries
   *
   * @returns Array of extended API summaries
   */
  private checkApisDocsInApiVersions(apiSummaries: IApiSummary[]) {
    if (apiSummaries.length > 0) {
      return forkJoin(
        apiSummaries.map((apiSummary) => {
          return this.apiService
            .getLatestApiVersion(apiSummary.organizationId, apiSummary.id)
            .pipe(
              map((latestApiVersion) => {
                if (latestApiVersion.api) {
                  return {
                    ...apiSummary,
                    latestApiVersion: latestApiVersion,
                    docsAvailable:
                      this.apiService.isApiDocAvailable(latestApiVersion)
                  } as IApiSummaryExt;
                } else {
                  this.showErrorSnackbar = true;
                  return {} as IApiSummaryExt;
                }
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
