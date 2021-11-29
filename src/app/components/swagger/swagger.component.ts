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

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { ConfigService } from '../../services/config/config.service';
import SwaggerUI from 'swagger-ui';

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.scss']
})
export class SwaggerComponent implements OnInit {
  endpoint = '';

  constructor(
    private route: ActivatedRoute,
    private heroService: HeroService,
    private translator: TranslateService,
    private config: ConfigService
  ) {
    this.endpoint = config.getEndpoint();
  }

  /**
   * Load the swagger definition and display it with the swagger ui bundle library on component initialization
   */
  ngOnInit(): void {
    const organizationId = this.route.snapshot.paramMap.get('orgId') ?? '';
    const apiId: string = this.route.snapshot.paramMap.get('apiId') ?? '';
    const apiVersion = this.route.snapshot.paramMap.get('apiVersion') ?? '';
    this.heroService.setUpHero({
      title: this.translator.instant('COMMON.API_DOCS') as string,
      subtitle: `${apiId} - ${apiVersion}`
    });

    const swaggerURL = `${this.endpoint}/devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/definition`;

    const swaggerOptions: SwaggerUI.SwaggerUIOptions = {
      dom_id: '#swagger-editor',
      layout: 'BaseLayout',
      url: swaggerURL,
      docExpansion: 'list',
      operationsSorter: 'alpha',
      tryItOutEnabled: false,
      requestInterceptor: (request: SwaggerUI.Request) => {
        return request;
      },
      responseInterceptor: (response: SwaggerUI.Response) => {
        return response;
      },
      onComplete: () => {}
    };

    // Loads the swagger ui with its options
    SwaggerUI(swaggerOptions);
  }
}
