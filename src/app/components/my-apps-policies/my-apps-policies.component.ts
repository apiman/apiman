import { Component, Input, OnInit } from '@angular/core';
import { IContractExt } from '../../interfaces/IContractExt';
import {IPolicyExt} from '../../interfaces/IPolicyExt';
import {TranslateService} from '@ngx-translate/core';
import {PolicyService} from '../../services/policy/policy.service';

@Component({
  selector: 'app-my-apps-policies',
  templateUrl: './my-apps-policies.component.html',
  styleUrls: ['./my-apps-policies.component.scss'],
})
export class MyAppsPoliciesComponent implements OnInit {
  @Input() contract?: IContractExt;

  policies: IPolicyExt[] | undefined;

  constructor(
    private translator: TranslateService,
    private policyService: PolicyService
  ) {}

  ngOnInit(): void {
    this.extractGaugeData();
  }

  private extractGaugeData() {
    if (!this.contract) {
      return;
    }

    this.policies = this.contract.policies;

    this.policies.forEach((policy: IPolicyExt) => {
      this.policyService
        .getPolicyProbe(this.contract!, policy)
        .subscribe((response) => {
          this.setGaugeDataForPolicy(policy, response);
        });
    });
  }

  private setGaugeDataForPolicy(policy: IPolicyExt, response: any) {
    const period = policy.configAsObject.period;
    policy.mainGaugeData = {
      name: policy.shortName + ' ' + this.translator.instant('POLICIES.USAGE'),
      limit: Number.parseInt(policy.restrictions.limit),
      period: period,
      remaining: response[0].status.remaining,
      infoHeader: `${this.translator.instant('POLICIES.USAGE')} (${period})`
    };

    policy.timeGaugeData = {
      name: 'Reset Timer',
      limit: 3600,
      period: policy.configAsObject.period,
      remaining: response[0].status.reset,
      infoHeader: 'Countdown'
    };
  }
}
