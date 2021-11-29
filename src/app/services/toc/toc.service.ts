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
import { IContractExt } from '../../interfaces/IContractExt';

@Injectable({
  providedIn: 'root'
})
export class TocService {
  constructor() {}

  /**
   * Schema Example: applications#SpringCorp.-1.0-SupportManager-1.0
   * @param contract
   */
  formatApiVersionPlanId(contract: IContractExt): string {
    return (
      this.formatClientId(contract) +
      '-' +
      contract.api.api.id +
      '-' +
      contract.api.version
    );
  }

  /**
   * Schema Example: applications#SpringCorp.-1.0
   * @param contract
   */
  formatClientId(contract: IContractExt): string {
    return contract.client.client.id + '-' + contract.client.version;
  }
}
