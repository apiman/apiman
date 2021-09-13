import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ApiService} from "../../services/api/api.service";
import {ApiBean} from "../../services/backend/backend.service";
import {HeroService} from '../../services/hero/hero.service';

@Component({
  selector: 'app-marketplace-api-details',
  templateUrl: './marketplace-api-details.component.html',
  styleUrls: ['./marketplace-api-details.component.scss']
})
export class MarketplaceApiDetailsComponent implements OnInit {
  constructor(private route: ActivatedRoute,
              public apiService: ApiService,
              private heroService: HeroService,
              private router: Router) {  }

  api!:ApiBean;


  ngOnInit(): void {
    this.getApi();
    this.setUpHero();
  }

  private setUpHero(){
    this.heroService.setUpHero({
      title: this.route.snapshot.paramMap.get('apiId')
    });
  }

  getApi() {
    const orgId = this.route.snapshot.paramMap.get('orgId')!;
    const apiId = this.route.snapshot.paramMap.get('apiId')!;

    this.apiService.getApi(orgId, apiId).subscribe(
      api => {
        this.api = api;
      }, error => {
        console.log(error.status);
        this.router.navigate(['marketplace'])
      });
  };
}
