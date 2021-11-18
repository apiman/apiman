import { Component, OnInit } from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-approval',
  templateUrl: './approval.component.html',
  styleUrls: ['./approval.component.scss']
})
export class ApprovalComponent implements OnInit {
  constructor(
    private heroService: HeroService,
    private translator: TranslateService
  ) {}

  ngOnInit(): void {
    this.setUpHero();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('APPROVAL.APPROVAL') as string,
      subtitle: ''
    });
  }
}
