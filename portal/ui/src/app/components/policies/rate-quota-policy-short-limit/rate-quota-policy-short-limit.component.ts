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

import { AfterViewInit, Component, Input } from '@angular/core';
import { IPolicyExt } from '../../../interfaces/IPolicy';
import { removeCssClass } from '../../../shared/utility';

@Component({
  selector: 'app-rate-quota-policy-short-limit',
  templateUrl: './rate-quota-policy-short-limit.component.html',
  styleUrls: ['./rate-quota-policy-short-limit.component.scss']
})
export class RateQuotaPolicyShortLimitComponent implements AfterViewInit {
  @Input() policy!: IPolicyExt;
  @Input() bold = true;

  constructor() {}

  ngAfterViewInit(): void {
    if (!this.bold) {
      removeCssClass(
        'app-rate-quota-policy-short-limit > p > span.bold',
        'bold'
      );
    }
  }
}
