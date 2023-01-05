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

import { AfterViewInit, Component, OnInit } from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakHelperService } from '../../services/keycloak-helper/keycloak-helper.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, AfterViewInit {
  loggedIn = false;
  username = '';

  constructor(
    public heroService: HeroService,
    private keycloak: KeycloakService,
    private keycloakHelper: KeycloakHelperService
  ) {}

  async ngOnInit(): Promise<void> {
    this.loggedIn = await this.keycloak.isLoggedIn();
    if (this.loggedIn) {
      this.username = this.keycloakHelper.getUsername();
    }
  }

  async ngAfterViewInit(): Promise<void> {
    this.loggedIn = await this.keycloak.isLoggedIn();
    if (this.loggedIn) {
      this.username = this.keycloakHelper.getUsername();
    }
  }

  public login(): void {
    this.keycloakHelper.login();
  }

  public logout(): void {
    this.keycloakHelper.logout();
  }
}
