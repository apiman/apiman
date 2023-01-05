/*
 * Copyright 2022 Scheer PAS Schweiz AG
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
import { Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-approval',
  templateUrl: './approval.component.html',
  styleUrls: ['./approval.component.scss']
})
export class ApprovalComponent implements OnInit {
  apiApproval = true;
  private titleKey = 'APPROVAL.API.HERO_TITLE';

  constructor(
    private heroService: HeroService,
    private translator: TranslateService,
    private router: Router,
    private keycloak: KeycloakService
  ) {}

  async ngOnInit(): Promise<void> {
    await this.setUpApiOrAccountApprovalPage();
    this.setUpHero();
  }

  private async setUpApiOrAccountApprovalPage() {
    if (this.router.url.includes('/approval/account')) {
      if (await this.keycloak.isLoggedIn()) {
        await this.router.navigate(['home']);
      }
      this.apiApproval = false;
      this.titleKey = 'APPROVAL.ACCOUNT.HERO_TITLE';
    }
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant(this.titleKey) as string,
      subtitle: ''
    });
  }
}
