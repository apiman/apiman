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

import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ConfigService } from '../config/config.service';

@Injectable({
  providedIn: 'root'
})
export class InitializerService {
  constructor(
    private translator: TranslateService,
    private configService: ConfigService
  ) {}

  /**
   * The functions calls the ngx-translation service to load a language. If the language file can not be found the fallback language
   * is English (loads the file en.json).
   */
  initLanguage(): Promise<void> {
    const language = this.configService
      .getAvailableLanguages()
      .includes(this.configService.getLanguage())
      ? this.configService.getLanguage()
      : 'en';

    return new Promise((resolve) => {
      void this.translator
        .use(language)
        .toPromise()
        .then(() => {
          resolve();
        });
    });
  }
}
