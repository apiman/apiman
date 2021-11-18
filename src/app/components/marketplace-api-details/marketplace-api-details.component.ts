import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api/api.service';
import { HeroService } from '../../services/hero/hero.service';
import { map, switchMap } from 'rxjs/operators';
import { forkJoin } from 'rxjs';
import {
  IApiSummary,
  IApiVersion,
  ISearchCriteria,
  ISearchResultsApiSummary
} from '../../interfaces/ICommunication';
import { SpinnerService } from '../../services/spinner/spinner.service';
import { IApiVersionExt } from '../../interfaces/IApiVersionExt';
import { BackendService } from '../../services/backend/backend.service';

@Component({
  selector: 'app-marketplace-api-details',
  templateUrl: './marketplace-api-details.component.html',
  styleUrls: ['./marketplace-api-details.component.scss']
})
export class MarketplaceApiDetailsComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    private heroService: HeroService,
    private spinnerService: SpinnerService,
    private backend: BackendService
  ) {}

  apiImgUrl?: string;
  apis!: IApiVersionExt[];

  ngOnInit(): void {
    this.getApiVersions();
    this.setUpHero();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.route.snapshot.paramMap.get('apiId') ?? '',
      subtitle: ''
    });
  }

  getApiVersions(): void {
    this.spinnerService.startWaiting();
    const orgId = this.route.snapshot.paramMap.get('orgId') ?? '';
    const apiId = this.route.snapshot.paramMap.get('apiId') ?? '';
    this.apiService
      .getApiVersionSummaries(orgId, apiId)
      .pipe(
        switchMap((apiVersionSummaries) => {
          return forkJoin(
            apiVersionSummaries.map((apiVersionSummary) => {
              return this.apiService.getApiVersion(
                apiVersionSummary.organizationId,
                apiVersionSummary.id,
                apiVersionSummary.version
              );
            })
          );
        }),
        switchMap((apiVersions: IApiVersion[]) => {
          return forkJoin(
            apiVersions.map((apiVersion) => {
              return this.apiService.isApiDocAvailable(apiVersion).pipe(
                map((docsAvailable) => {
                  return {
                    ...apiVersion,
                    docsAvailable: docsAvailable
                  } as IApiVersionExt;
                })
              );
            })
          );
        })
      )
      .subscribe((apiVersions) => {
        this.spinnerService.stopWaiting();
        this.apis = apiVersions;
        this.getApiImage();
      });
  }

  private getApiImage() {
    const searchCriteria: ISearchCriteria = {
      filters: [{ name: 'name', value: this.apis[0].api.name, operator: 'eq' }],
      paging: { page: 1, pageSize: 1 }
    };

    // TODO: At the moment search endpoint returns every API
    this.backend
      .searchApis(searchCriteria)
      .subscribe((summary: ISearchResultsApiSummary) => {
        const found = summary.beans.find((bean: IApiSummary) => {
          return bean.name === this.apis[0].api.name;
        });

        if (found) {
          this.apiImgUrl = found.image;
        }
      });
  }
}
