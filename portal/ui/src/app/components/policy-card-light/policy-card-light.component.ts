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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { IPolicyExt } from '../../interfaces/IPolicy';
import { IContractExt } from '../../interfaces/IContractExt';

@Component({
  selector: 'app-policy-card-light',
  templateUrl: './policy-card-light.component.html',
  styleUrls: ['./policy-card-light.component.scss']
})
export class PolicyCardLightComponent {
  @Input() policy!: IPolicyExt;
  @Input() contract?: IContractExt;

  @Output() sectionChanged = new EventEmitter();

  constructor() {}

  setSectionToPolicies(): void {
    if (!this.contract) return;

    this.sectionChanged.emit({ contract: this.contract, section: 'policies' });
  }
}
