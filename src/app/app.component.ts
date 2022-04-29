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

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { HeroService } from './services/hero/hero.service';
import { IHero, INavigation } from './interfaces/IConfig';
import { NavigationService } from './services/navigation/navigation.service';
import { SpinnerService } from './services/spinner/spinner.service';
import { KeycloakService } from 'keycloak-angular';
import { BackendService } from './services/backend/backend.service';
import { EMPTY, from, Observable, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import { ICurrentUser } from './interfaces/ICommunication';
import { HttpErrorResponse } from '@angular/common/http';
import { ConfigService } from './services/config/config.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  @ViewChild('content') content!: ElementRef;
  title = 'devportal';
  defaultLocale = 'en';

  constructor(
    private router: Router,
    private heroService: HeroService,
    private navigationService: NavigationService,
    public loadingSpinnerService: SpinnerService,
    private keycloak: KeycloakService,
    private backend: BackendService,
    private translate: TranslateService,
    private config: ConfigService
  ) {}

  ngOnInit(): void {
    this.defaultLocale = this.config.getLanguage();

    this.loadUserLocale();
    this.initHeroEmitter();
    this.initNavigationEmitter();
  }

  /**
   * This method changes the language of the application based on the users/browsers locale.
   * If the user is not logged in or a preferred browser language is not available the default (en) will be used.
   */
  private loadUserLocale(): void {
    from(this.keycloak.isLoggedIn())
      .pipe(
        switchMap((isLoggedIn: boolean) => {
          if (isLoggedIn) {
            return this.getUserLanguage();
          } else {
            return of(this.translate.getBrowserLang());
          }
        }),
        switchMap((language: string | undefined) => {
          if (language) {
            console.info('Trying to load language: %s', language);
            // currently no support for locales like en_US
            return this.translate.use(language.substring(0, 2).toLowerCase());
          } else {
            console.info(
              'No preferred locale is configured, using default (%s)',
              this.defaultLocale
            );
            return EMPTY;
          }
        })
      )
      .subscribe({
        next: () => console.info('Language successfully loaded'),
        error: (err: HttpErrorResponse) =>
          console.error(
            'No language is matching the requested locale, using default (%s): %s',
            this.defaultLocale,
            err.message
          )
      });
  }

  /**
   * Gets the locale of a user
   * @returns the locale (string) as observable
   */
  private getUserLanguage(): Observable<string> {
    return this.backend.getCurrentUser().pipe(
      map((user: ICurrentUser) => {
        return user.locale;
      })
    );
  }

  private initHeroEmitter(): void {
    this.heroService.heroChanged.subscribe((hero: IHero) => {
      if (hero.large) {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
        this.content.nativeElement.classList.remove('free-height');
      } else {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
        this.content.nativeElement.classList.add('free-height');
      }
    });
  }

  private initNavigationEmitter(): void {
    this.navigationService.navigationChanged.subscribe(
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      (navigation: INavigation) => {}
    );
  }
}
