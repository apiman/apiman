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
