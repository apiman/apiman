/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

import { Component, OnInit } from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { KeycloakHelperService } from '../../services/keycloak-helper/keycloak-helper.service';
import { KeycloakProfile } from 'keycloak-js';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss']
})
export class AccountComponent implements OnInit {
  public userProfile: KeycloakProfile | null = null;

  constructor(
    private heroService: HeroService,
    private translator: TranslateService,
    private keycloakHelper: KeycloakHelperService
  ) {}

  async ngOnInit(): Promise<void> {
    this.setUpHero();
    this.userProfile = await this.keycloakHelper.getUserProfile();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('ACCOUNT.TITLE') as string,
      subtitle: this.translator.instant('ACCOUNT.SUBTITLE') as string
    });
  }

  public logout(): void {
    this.keycloakHelper.logout();
  }
}
