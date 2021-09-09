import { Component, OnInit } from '@angular/core';
import {HeroService} from '../../services/hero/hero.service';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.sass']
})
export class AccountComponent implements OnInit {

  constructor(private heroService: HeroService,
              private translater: TranslateService) { }

  ngOnInit(): void {
    this.setUpHero();
  }

  private setUpHero(){
    this.heroService.setUpHero({
      title: this.translater.instant('ACCOUNT.TITLE'),
      subtitle: this.translater.instant('ACCOUNT.SUBTITLE')
    });
  }
}
