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

import { EventEmitter, Injectable } from '@angular/core';
import { ConfigService } from '../config/config.service';
import { IHero } from '../../interfaces/IConfig';
import { Title } from '@angular/platform-browser';
import { NotificationService } from '../notification/notification.service';
import { SnackbarService } from '../snackbar/snackbar.service';
import { KeycloakService } from 'keycloak-angular';
import { EMPTY, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class HeroService {
  hero: IHero;
  heroChanged: EventEmitter<IHero> = new EventEmitter<IHero>();

  constructor(
    private configService: ConfigService,
    private notificationService: NotificationService,
    private snackbarService: SnackbarService,
    private keycloakService: KeycloakService,
    private titleService: Title
  ) {
    this.hero = configService.getHero();
  }

  setUpHero(hero: IHero): void {
    this.hero.large = hero.large ?? false;
    this.hero.title = hero.title ?? '';
    this.hero.subtitle = hero.subtitle ?? '';
    this.updateNotificationCount(false);
    this.titleService.setTitle(this.hero.title);
    this.heroChanged.emit(this.hero);
  }

  updateNotificationCount(emitEvent: boolean): void {
    this.hero.notificationCount = '0';

    from(this.keycloakService.isLoggedIn())
      .pipe(
        switchMap((isLoggedIn) => {
          if (isLoggedIn) {
            return this.notificationService.headNotifications();
          } else {
            return EMPTY;
          }
        })
      )
      .subscribe((resp) => {
        this.hero.notificationCount = resp.headers.get('total-count') ?? '0';
        if (emitEvent) {
          this.heroChanged.emit(this.hero);
        }
      });
  }
}
