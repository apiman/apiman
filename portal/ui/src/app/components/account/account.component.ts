/*
 * Copyright 2023 Scheer PAS Schweiz AG
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
import { KeycloakProfile, KeycloakTokenParsed } from 'keycloak-js';
import { BackendService } from '../../services/backend/backend.service';
import { ICurrentUser } from '../../interfaces/ICommunication';
import { from, Observable, of } from 'rxjs';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss']
})
export class AccountComponent implements OnInit {
  public userProfile$: Observable<KeycloakProfile | null> = of({});
  public keycloakToken?: KeycloakTokenParsed;
  public accountUrl = '';
  public apimanAccount$: Observable<ICurrentUser> = of({} as ICurrentUser);

  constructor(
    private heroService: HeroService,
    private translator: TranslateService,
    private keycloakHelper: KeycloakHelperService,
    private backendService: BackendService
  ) {}

  ngOnInit() {
    this.setUpHero();
    this.accountUrl = this.keycloakHelper.getKeycloakAccountUrl();
    this.userProfile$ = from(this.keycloakHelper.getUserProfile());
    this.apimanAccount$ = this.backendService.getCurrentUser();
    this.keycloakToken = this.keycloakHelper.decodeCurrentKeycloakToken();
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
