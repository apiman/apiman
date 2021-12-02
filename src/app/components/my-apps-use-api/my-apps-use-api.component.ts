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

import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { ISignUpInfo } from '../../interfaces/ISignUpInfo';
import { SignUpService } from '../../services/sign-up/sign-up.service';
import { IContractExt } from '../../interfaces/IContractExt';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-my-apps-use-api',
  templateUrl: './my-apps-use-api.component.html',
  styleUrls: ['./my-apps-use-api.component.scss']
})
export class MyAppsUseApiComponent implements OnInit, OnChanges {
  @Input() contract?: IContractExt;
  newContractDetails: ISignUpInfo;

  orgId = '';
  apiId = '';
  apiVersion = '';
  docsAvailable = false;
  apiKey: string | null = null;
  apiKeyUi = '';
  endpoint = '';
  oAuthServerUrl = '';
  oAuthClientSecret = '';
  target = '_self';

  previewText = {
    apiKey: this.translator.instant('WIZARD.API_KEY_PREVIEW') as string,
    endpoint: this.translator.instant('WIZARD.API_ENDPOINT_PREVIEW') as string,
    oAuthServerUrl: 'http://example.org/auth/realms/example/foo',
    oAuthClientSecret: '73b985d1-98b8-4d80-a89d-9dbee3b21a17'
  };
  disableButtons = false;
  hasOAuth = false;

  constructor(
    private snackbar: SnackbarService,
    private signUpService: SignUpService,
    private translator: TranslateService
  ) {
    this.newContractDetails = this.signUpService.getSignUpInfo();
  }

  ngOnChanges(): void {
    this.initProperties();
  }

  ngOnInit(): void {
    this.initProperties();
  }

  private initProperties() {
    if (this.contract) {
      this.disableButtons = false;
      this.apiKey = this.contract.client.apikey;
      this.apiKeyUi = `X-API-Key: ${this.apiKey}`;
      this.endpoint = `${this.contract.managedEndpoint}?apikey=${this.apiKey}`;
      this.orgId = this.contract.api.api.organization.id;
      this.apiId = this.contract.api.api.id;
      this.apiVersion = this.contract.api.version;
      this.docsAvailable = this.contract.docsAvailable;
    } else {
      this.disableButtons = true;
      this.apiKeyUi = this.previewText.apiKey;
      this.endpoint = this.previewText.endpoint;
      this.oAuthServerUrl = this.previewText.oAuthServerUrl;
      this.oAuthClientSecret = this.previewText.oAuthClientSecret;
      this.orgId = this.newContractDetails.organizationId;
      this.apiId = this.newContractDetails.apiVersion.api.id;
      this.apiVersion = this.newContractDetails.apiVersion.version;
      this.docsAvailable = this.newContractDetails.docsAvailable;
      this.target = '_blank';
    }
  }
}
