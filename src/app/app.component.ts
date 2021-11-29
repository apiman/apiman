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

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { HeroService } from './services/hero/hero.service';
import { IHero, INavigation } from './interfaces/IConfig';
import { NavigationService } from './services/navigation/navigation.service';
import { SpinnerService } from './services/spinner/spinner.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  @ViewChild('content') content!: ElementRef;
  title = 'devportal';

  constructor(
    private router: Router,
    private heroService: HeroService,
    private navigationService: NavigationService,
    public loadingSpinnerService: SpinnerService
  ) {}

  ngOnInit(): void {
    this.initHeroEmitter();
    this.initNavigationEmitter();
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
