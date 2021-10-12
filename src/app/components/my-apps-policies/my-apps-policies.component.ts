import { Component, Input, OnInit } from '@angular/core';
import { IContractExt } from '../../interfaces/IContractExt';
import { IGaugeChartData } from '../../interfaces/IGaugeChartData';
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-my-apps-policies',
  templateUrl: './my-apps-policies.component.html',
  styleUrls: ['./my-apps-policies.component.scss'],
})
export class MyAppsPoliciesComponent implements OnInit {
  @Input() contract?: IContractExt;

  policies: IPolicyExt[] | undefined;
  gaugeData: IGaugeChartData | undefined;

  constructor(private translator: TranslateService) {}

  ngOnInit(): void {
    this.extractGaugeData();
  }

  private extractGaugeData() {
    if (!this.contract)
      return;

    this.policies = this.contract.policies;

    this.policies.forEach((policy: IPolicyExt) => {
      const period = policy.configAsObject.period;

      policy.mainGaugeData = {
        name: policy.shortName + ' ' + this.translator.instant('POLICIES.USAGE'),
        limit: Number.parseInt(policy.restrictions.limit),
        period,
        currentValue: 1,
        infoHeader: `${this.translator.instant('POLICIES.USAGE')} (${period})`
      };

      policy.timeGaugeData = {
        name: 'Reset Timer',
        limit: 24,
        period: policy.configAsObject.period,
        currentValue: 1,
        infoHeader: 'Countdown'
      };
    });
  }
}
