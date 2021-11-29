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

@Injectable({
  providedIn: 'root'
})
export class HeroService {
  hero: IHero;
  heroChanged: EventEmitter<IHero> = new EventEmitter<IHero>();

  constructor(
    private configService: ConfigService,
    private titleService: Title
  ) {
    this.hero = configService.getHero();
  }

  setUpHero(hero: IHero): void {
    this.hero.large = hero.large ?? false;
    this.hero.title = hero.title ?? '';
    this.hero.subtitle = hero.subtitle ?? '';

    this.titleService.setTitle(this.hero.title);
    this.heroChanged.emit(this.hero);
  }
}
