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

import { EventEmitter, Injectable } from '@angular/core';
import { ILink, INavigation } from '../../interfaces/IConfig';
import { ConfigService } from '../config/config.service';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  navigation: INavigation;

  links: ILink[] = [
    {
      name: this.translator.instant('MPLACE.TITLE') as string,
      link: 'marketplace',
      openInNewTab: false,
      useRouter: true
    },
    {
      name: this.translator.instant('CLIENTS.TITLE') as string,
      link: 'applications',
      openInNewTab: false,
      useRouter: true
    }
    // To-Do
    // Show account link again once the account page is implemented correctly
    // {
    //   name: this.translator.instant('ACCOUNT.TITLE'),
    //   link: 'account',
    //   openInNewTab: false,
    //   useRouter: true
    // }
  ];

  navigationChanged: EventEmitter<INavigation> =
    new EventEmitter<INavigation>();

  constructor(
    private configService: ConfigService,
    private translator: TranslateService
  ) {
    this.navigation = this.initLinks();
  }

  private initLinks(): INavigation {
    if (this.configService.getShowHomeLink()) {
      this.links.unshift({
        name: this.translator.instant('COMMON.HOME') as string,
        link: 'home',
        openInNewTab: false,
        useRouter: true
      });
    }

    const navigationConfig = this.configService.getNavigation();
    navigationConfig.links = this.links.concat(navigationConfig.links);

    return navigationConfig;
  }
}
