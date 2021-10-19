import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api/api.service';
import { HeroService } from '../../services/hero/hero.service';
import {map, switchMap} from 'rxjs/operators';
import {forkJoin} from 'rxjs';
import { IApi, IApiVersion } from '../../interfaces/ICommunication';
import {SpinnerService} from "../../services/spinner/spinner.service";
import {IApiVersionExt} from "../../interfaces/IApiVersionExt";
import {BackendService} from "../../services/backend/backend.service";

@Component({
  selector: 'app-marketplace-api-details',
  templateUrl: './marketplace-api-details.component.html',
  styleUrls: ['./marketplace-api-details.component.scss'],
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
    this.getApiImage();
    this.getApiVersions();
    this.setUpHero();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.route.snapshot.paramMap.get('apiId'),
    });
  }

  getApiVersions(): void {
    this.spinnerService.startWaiting();
    const orgId = this.route.snapshot.paramMap.get('orgId')!;
    const apiId = this.route.snapshot.paramMap.get('apiId')!;
    this.apiService
      .getApiVersionSummaries(orgId, apiId)
      .pipe(
        switchMap((apiVersionSummaries) => {
          return forkJoin(apiVersionSummaries.map(apiVersionSummary => {
            return this.apiService.getApiVersion(
              apiVersionSummary.organizationId,
              apiVersionSummary.id,
              apiVersionSummary.version
            )
          }))
        }),
        switchMap((apiVersions: IApiVersion[]) => {
          return forkJoin(apiVersions.map(apiVersion => {
            return this.apiService.isApiDocAvailable(apiVersion).pipe(
              map(docsAvailable => {
                return {
                  ...apiVersion,
                  docsAvailable: docsAvailable
                } as IApiVersionExt;
              })
            )
          }))
        })
      )
      .subscribe((apiVersions) => {
        this.spinnerService.stopWaiting();
        this.apis = apiVersions;
      });
  }

  private getApiImage() {
    const orgId = this.route.snapshot.paramMap.get('orgId')!;
    const apiId = this.route.snapshot.paramMap.get('apiId')!;

    this.backend.getApi(orgId, apiId).subscribe((api: IApi) => {
      this.apiImgUrl = api.image;
    });
  }
}
