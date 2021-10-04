import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { BackendService } from '../../services/backend/backend.service';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import {ISignUpInfo} from "../../interfaces/ISignUpInfo";
import {SignUpService} from "../../services/sign-up/sign-up.service";
import {IContractExt} from "../../interfaces/IContractExt";

@Component({
  selector: 'app-my-apps-use-api',
  templateUrl: './my-apps-use-api.component.html',
  styleUrls: ['./my-apps-use-api.component.scss'],
})
export class MyAppsUseApiComponent implements OnInit, OnChanges {
  @Input() contract?: IContractExt;
  newContractDetails: ISignUpInfo;

  mockText =
    'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et';

  apiKey = '';
  endpoint = '';
  oAuthServerUrl = '';
  oAuthClientSecret = '';

  previewText = {
    apiKey: 'You will be provided with an API-Key after sign-up',
    endpoint: 'You will be provided with an endpoint after sign-up',
    oAuthServerUrl: 'http://example.org/auth/realms/example/foo',
    oAuthClientSecret: '73b985d1-98b8-4d80-a89d-9dbee3b21a17',
  };
  disableButtons = false;
  hasOAuth = false;

  constructor(
    private backend: BackendService,
    private snackbar: SnackbarService,
    private signUpService: SignUpService,
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
      this.apiKey = this.contract!.client.apikey;
      this.endpoint = this.contract.managedEndpoint;
    } else {
      this.disableButtons = true;
      this.apiKey = this.previewText.apiKey;
      this.endpoint = this.previewText.endpoint;
      this.oAuthServerUrl = this.previewText.oAuthServerUrl;
      this.oAuthClientSecret = this.previewText.oAuthClientSecret;
    }
  }
}
