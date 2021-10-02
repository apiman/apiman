import { Component, Input, OnInit } from '@angular/core';
import { IContractExt } from '../../interfaces/IContractExt';
import { IGaugeChartData } from '../../interfaces/IGaugeChartData';
import {IPolicyExt} from "../../interfaces/IPolicyExt";

@Component({
  selector: 'app-my-apps-policies',
  templateUrl: './my-apps-policies.component.html',
  styleUrls: ['./my-apps-policies.component.scss'],
})
export class MyAppsPoliciesComponent implements OnInit {
  @Input() contract?: IContractExt;
  policies: IPolicyExt[] | undefined;
  gaugeData: IGaugeChartData | undefined;

  ngOnInit(): void {
    this.getAllPolicies();
  }

  private getAllPolicies() {

    if (this.contract) {
      this.policies = this.contract.policies;
    }
    this.gaugeData = {
      name: 'Rate Limit Usage',
      limit: 500,
      period: 'Seconds',
      currentValue: 250,
    };
  }
}
