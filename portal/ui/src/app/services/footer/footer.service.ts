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
import { ConfigService } from '../config/config.service';
import { IFooter, ILink } from '../../interfaces/IConfig';

@Injectable({
  providedIn: 'root'
})
export class FooterService {
  footer: IFooter;

  links: ILink[] = [];

  constructor(private configService: ConfigService) {
    this.footer = this.initLinks();
  }

  private initLinks(): IFooter {
    const footerConfig = this.configService.getFooter();
    footerConfig.links = this.links.concat(footerConfig.links);

    return footerConfig;
  }
}
