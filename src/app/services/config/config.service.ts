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

import { Injectable } from '@angular/core';
import {
  IAuthProvider,
  IConfig,
  IFooter,
  IHero,
  INavigation,
  ITerms
} from '../../interfaces/IConfig';
import { HttpClient } from '@angular/common/http';
import * as JSON5 from 'json5';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  config!: IConfig;

  constructor(private http: HttpClient) {}

  async readAndEvaluateConfig(): Promise<IConfig> {
    const configAsJson5String = await this.http
      .get('assets/config.json5', { responseType: 'text' })
      .toPromise();
    try {
      if (configAsJson5String != null) {
        this.config = JSON5.parse(configAsJson5String);
      }
    } catch (e) {
      if (e instanceof SyntaxError) {
        console.error(e.message);
      }
      throw Error('Invalid Config File');
    }
    return this.config;
  }

  getFooter(): IFooter {
    return { ...this.config.footer };
  }

  getHero(): IHero {
    return { ...this.config.hero };
  }

  getNavigation(): INavigation {
    return { ...this.config.navigation };
  }

  getAvailableLanguages(): string[] {
    return this.config.supportedLanguages;
  }

  getLanguage(): string {
    return this.config.language;
  }

  getEndpoint(): string {
    return this.config.endpoint;
  }

  getAuth(): IAuthProvider {
    return { ...this.config.auth };
  }

  getTerms(): ITerms {
    return { ...this.config.terms };
  }

  getShowHomeLink(): boolean {
    return this.config.navigation.showHomeLink;
  }
}
