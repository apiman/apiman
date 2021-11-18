import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { BackendService } from '../../services/backend/backend.service';
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

  apiKey = '';
  endpoint = '';
  oAuthServerUrl = '';
  oAuthClientSecret = '';

  previewText = {
    apiKey: this.translator.instant('WIZARD.API_KEY_PREVIEW') as string,
    endpoint: this.translator.instant('WIZARD.API_ENDPOINT_PREVIEW') as string,
    oAuthServerUrl: 'http://example.org/auth/realms/example/foo',
    oAuthClientSecret: '73b985d1-98b8-4d80-a89d-9dbee3b21a17'
  };
  disableButtons = false;
  hasOAuth = false;

  constructor(
    private backend: BackendService,
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
      this.apiKey = `X-API-Key: ${this.contract.client.apikey}`;
      this.endpoint = `${this.contract.managedEndpoint}?apikey=${this.contract.client.apikey}`;
      this.orgId = this.contract.api.api.organization.id;
      this.apiId = this.contract.api.api.id;
      this.apiVersion = this.contract.api.version;
      this.docsAvailable = this.contract.docsAvailable;
    } else {
      this.disableButtons = true;
      this.apiKey = this.previewText.apiKey;
      this.endpoint = this.previewText.endpoint;
      this.oAuthServerUrl = this.previewText.oAuthServerUrl;
      this.oAuthClientSecret = this.previewText.oAuthClientSecret;
      this.orgId = this.newContractDetails.organizationId;
      this.apiId = this.newContractDetails.apiVersion.api.id;
      this.apiVersion = this.newContractDetails.apiVersion.version;
      this.docsAvailable = this.newContractDetails.docsAvailable;
    }
  }
}
