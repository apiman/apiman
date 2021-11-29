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

import { Component, Input, OnInit } from '@angular/core';
import { IApiVersionExt } from '../../interfaces/IApiVersionExt';
import { TranslateService } from '@ngx-translate/core';
import { ConfigService } from '../../services/config/config.service';

@Component({
  selector: 'app-marketplace-api-description',
  templateUrl: './marketplace-api-description.component.html',
  styleUrls: ['./marketplace-api-description.component.scss']
})
export class MarketplaceApiDescriptionComponent implements OnInit {
  @Input() api!: IApiVersionExt;
  @Input() isLatest!: boolean;
  @Input() apiImgUrl!: string;

  // TODO get features from backend as soon as the endpoint is created
  features = [];
  public markDownText = this.translator.instant(
    'API_DETAILS.NO_EXT_DESCRIPTION'
  ) as string;

  constructor(
    private translator: TranslateService,
    public configService: ConfigService
  ) {}

  ngOnInit(): void {
    if (this.api.extendedDescription)
      this.markDownText = this.api.extendedDescription;
  }

  hasFeatures(): boolean {
    return this.features.length > 0;
  }
}
