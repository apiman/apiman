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

import { AfterViewInit, Component, Input } from '@angular/core';
import { IContractExt } from '../../interfaces/IContractExt';
import { IPolicyExt } from '../../interfaces/IPolicy';

@Component({
  selector: 'app-my-apps-policies',
  templateUrl: './my-apps-policies.component.html',
  styleUrls: ['./my-apps-policies.component.scss']
})
export class MyAppsPoliciesComponent implements AfterViewInit {
  @Input() contract?: IContractExt;

  policies: IPolicyExt[] | undefined;

  constructor() {}

  ngAfterViewInit(): void {
    this.extractGaugeData();
  }

  private extractGaugeData() {
    if (!this.contract) {
      return;
    }

    this.policies = this.contract.policies;
  }
}
