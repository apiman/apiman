import { Component, OnInit } from '@angular/core';
import {HeroService} from '../../services/hero/hero.service';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-marketplace',
  templateUrl: './marketplace.component.html',
  styleUrls: ['./marketplace.component.scss']
})
export class MarketplaceComponent implements OnInit {

  constructor(private heroService: HeroService,
              private translator: TranslateService) { }

  ngOnInit(): void {
    this.initHero();
  }

  private initHero(){
    this.heroService.setUpHero({
      title: this.translator.instant('MPLACE.TITLE'),
      subtitle: this.translator.instant('MPLACE.SUBTITLE')
    });
  }
}
