import { Component, Input, OnInit } from '@angular/core';
import { IContract } from '../../interfaces/ICommunication';

@Component({
  selector: 'app-my-apps-use-api',
  templateUrl: './my-apps-use-api.component.html',
  styleUrls: ['./my-apps-use-api.component.scss'],
})
export class MyAppsUseApiComponent implements OnInit {
  @Input() contract?: IContract;
  mockText =
    'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et';

  apiKey = '';
  endpoint = '';
  oAuthServerUrl = '';
  oAuthClientSecret = '';

  previewText = {
    apiKey: 'c9ec8c4b-c735-4b5a-9fe3-795f559c588b',
    endpoint: 'http://example.com/ex/testOrg/test123/clientThing/1.0',
    oAuthServerUrl: 'http://example.org/auth/realms/example/foo',
    oAuthClientSecret: '73b985d1-98b8-4d80-a89d-9dbee3b21a17'
  }


  constructor() {}

  ngOnInit(): void {
    this.initProperties();
  }

  private initProperties(){
    if (this.contract){
      this.apiKey = this.contract.client.apikey;
      this.endpoint = this.contract.api.endpoint;
    } else{
      this.apiKey = this.previewText.apiKey;
      this.endpoint = this.previewText.endpoint;
      this.oAuthServerUrl = this.previewText.oAuthServerUrl;
      this.oAuthClientSecret = this.previewText.oAuthClientSecret;
    }
  }
}
