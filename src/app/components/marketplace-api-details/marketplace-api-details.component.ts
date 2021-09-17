import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api/api.service';
import { HeroService } from '../../services/hero/hero.service';
import { switchMap } from 'rxjs/operators';
import { forkJoin, Observable } from 'rxjs';
import { IApi, IApiVersion } from '../../interfaces/ICommunication';

@Component({
  selector: 'app-marketplace-api-details',
  templateUrl: './marketplace-api-details.component.html',
  styleUrls: ['./marketplace-api-details.component.scss'],
})
export class MarketplaceApiDetailsComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    public apiService: ApiService,
    private heroService: HeroService,
    private router: Router
  ) {}

  api!: IApi;
  apis!: IApiVersion[];

  ngOnInit(): void {
    this.getApiVersions();
    this.setUpHero();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.route.snapshot.paramMap.get('apiId'),
    });
  }

  getApiVersions(): void {
    const orgId = this.route.snapshot.paramMap.get('orgId')!;
    const apiId = this.route.snapshot.paramMap.get('apiId')!;
    const newVersions: Array<Observable<IApiVersion>> = [];
    this.apiService
      .getApiVersionSummaries(orgId, apiId)
      .pipe(
        switchMap((apiVersionSummarys) => {
          for (const apiVersionSummary of apiVersionSummarys) {
            newVersions.push(
              this.apiService.getApiVersion(
                apiVersionSummary.organizationId,
                apiVersionSummary.id,
                apiVersionSummary.version
              )
            );
          }
          return forkJoin(newVersions);
        })
      )
      .subscribe((data) => (this.apis = data));
  }
}
