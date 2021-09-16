import { Component, OnInit } from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-my-apps',
  templateUrl: './my-apps.component.html',
  styleUrls: ['./my-apps.component.scss'],
})
export class MyAppsComponent implements OnInit {
  constructor(
    private heroService: HeroService,
    private translator: TranslateService
  ) {}

  ngOnInit(): void {
    this.setUpHero();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('APPS.TITLE'),
      subtitle: this.translator.instant('APPS.SUBTITLE'),
    });
  }
}
