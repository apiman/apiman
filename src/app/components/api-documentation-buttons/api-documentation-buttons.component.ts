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

import { Component, Input, OnInit } from '@angular/core';
import { IApiVersion, IContract } from '../../interfaces/ICommunication';
import { ApiService } from '../../services/api/api.service';

@Component({
  selector: 'app-api-documentation-buttons',
  templateUrl: './api-documentation-buttons.component.html',
  styleUrls: ['./api-documentation-buttons.component.scss']
})
export class ApiDocumentationButtonsComponent implements OnInit {
  @Input() target = '_self';
  @Input() downloadEnabled = false;
  @Input() apiVersion?: IApiVersion;
  @Input() contract?: IContract;

  clientId: null | number = null;
  tryItOut: null | boolean = null;
  publicApi = false;
  orgId = '';
  apiId = '';
  apiVersionNumber = '';

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    if (this.contract) {
      this.tryItOut = true;
      this.orgId = this.contract.api.api.organization.id;
      this.apiId = this.contract.api.api.id;
      this.apiVersionNumber = this.contract.api.version;
      this.clientId = this.contract.id;

      localStorage.setItem(
        `APIMAN_DEVPORTAL-${this.orgId}-${this.apiId}-${this.apiVersionNumber}-${this.contract.id}`,
        this.contract.client.apikey
      );
    }
    if (this.apiVersion) {
      // if it's not public we disable try it out
      this.tryItOut = this.apiVersion.publicAPI;
      this.publicApi = this.apiVersion.publicAPI;
      this.orgId = this.apiVersion.api.organization.id;
      this.apiId = this.apiVersion.api.id;
      this.apiVersionNumber = this.apiVersion.version;
    }
  }

  download(): void {
    if (this.contract) {
      this.apiService.downloadDefinitionFile(
        this.contract.api.api.organization.id,
        this.contract.api.api.id,
        this.contract.api.version,
        this.contract.api.definitionType,
        true
      );
    }
    if (this.apiVersion) {
      this.apiService.downloadDefinitionFile(
        this.apiVersion.api.organization.id,
        this.apiVersion.api.id,
        this.apiVersion.version,
        this.apiVersion.definitionType,
        false
      );
    }
  }
}
