import { Component, OnInit } from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { KeycloakHelperService } from '../../services/keycloak-helper/keycloak-helper.service';
import { KeycloakProfile } from 'keycloak-js';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss'],
})
export class AccountComponent implements OnInit {
  public userProfile: KeycloakProfile | null = null;

  constructor(
    private heroService: HeroService,
    private translator: TranslateService,
    private keycloakHelper: KeycloakHelperService
  ) {}

  async ngOnInit() {
    this.setUpHero();
    this.userProfile = await this.keycloakHelper.getUserProfile();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('ACCOUNT.TITLE'),
      subtitle: this.translator.instant('ACCOUNT.SUBTITLE'),
    });
  }

  public logout() {
    this.keycloakHelper.logout();
  }
}
