import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ApiService} from "../../services/api/api.service";
import {ApiBean} from "../../services/backend/backend.service";
import {Api} from "../../interfaces/api";
import {HeroService} from '../../services/hero/hero.service';

@Component({
  selector: 'app-marketplace-api-details',
  templateUrl: './marketplace-api-details.component.html',
  styleUrls: ['./marketplace-api-details.component.sass']
})
export class MarketplaceApiDetailsComponent implements OnInit {

  @Input() id: string = "";

  api: ApiBean = {};

  constructor(private route: ActivatedRoute,
              public apiService: ApiService,
              private heroService: HeroService,
              private router: Router) { }

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
    const orgId = this.route.snapshot.paramMap.get('orgId');
    const apiId = this.route.snapshot.paramMap.get('apiId');

    this.apiService.getApi(orgId!, apiId!).then((response: any) => {
      if (response.data) {
        this.apiService.currentApi = response.data
      }else{
        console.warn('Could not find API: ' + apiId);
        this.router.navigate(['marketplace']);
      }
    });;
  };
}
