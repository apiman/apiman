import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import { IContractExt } from '../../interfaces/IContractExt';
import {IPolicyExt} from '../../interfaces/IPolicyExt';
import {TranslateService} from '@ngx-translate/core';
import {PolicyService} from '../../services/policy/policy.service';
import {IGaugeChartData} from "../../interfaces/IGaugeChartData";

interface ITimeFactor{
  [timeUnit: string]: ITimeUnitData
}


interface ITimeUnitData{
  multiplier: number,
  inSeconds: number
}

@Component({
  selector: 'app-my-apps-policies',
  templateUrl: './my-apps-policies.component.html',
  styleUrls: ['./my-apps-policies.component.scss'],
})
export class MyAppsPoliciesComponent implements AfterViewInit {
  @Input() contract?: IContractExt;

  policies: IPolicyExt[] | undefined;

  constructor(
    private translator: TranslateService,
    private policyService: PolicyService
  ) {}

  ngAfterViewInit(): void {
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

  private setGaugeDataForPolicy(policy: IPolicyExt, policyProbe: any) {
    const period = policy.configAsObject.period;

    policy.mainGaugeData = {
      name: policy.shortName + ' ' + this.translator.instant('POLICIES.USAGE'),
      limit: Number.parseInt(policy.restrictions.limit),
      currentVal: Number.parseInt(policy.restrictions.limit) - policyProbe[0].status.remaining,
      unitSuffix: this.getDataUnit(policy),
      period: period,
      remaining: policyProbe[0].status.remaining,
      infoHeader: `${this.translator.instant('POLICIES.USAGE')} (${period})`,
      bottomText: ''
    };

    policy.mainGaugeData.bottomText = this.generateCardBottomText(policy);

    const timeData = this.getTimeDataForProbe(policyProbe[0]);
    policy.timeGaugeData = {
      name: 'Reset Timer',
      limit: timeData.limit,
      currentVal: timeData.current,
      unitSuffix: timeData.suffix,
      period: policy.configAsObject.period,
      remaining: policyProbe[0].status.reset,
      infoHeader: 'Countdown',
      bottomText: timeData.bottomText
    };
  }

  generateCardBottomText(policy: IPolicyExt) {
    switch (policy.policyIdentifier){
      case 'RATE_LIMIT': return this.generateCardBottomTextForRateLimit(policy.mainGaugeData);
      case 'QUOTA': return this.generateCardBottomTextForQuota(policy.timeGaugeData);
      default: return 'Unsupported policy type';
    }
  }

  private generateCardBottomTextForRateLimit(gaugeDate: IGaugeChartData) {
    return ((gaugeDate.currentVal) * 100 / gaugeDate.limit).toFixed(1)
      + ' % '
      + this.translator.instant('APPS.USED');
  }

  private generateCardBottomTextForQuota(policy: IGaugeChartData) {
    // TODO
    return 'soon';
  }

  private getResetTimeForMonth(){
    const date = new Date();
    const nextMonthDate = new Date(date.getFullYear(), date.getMonth() + 1);

    const month = nextMonthDate.toLocaleString('default', { month: 'long' });
    return this.translator.instant('APPS.FIRST_OF') + ' ' + month;
  }

  private getTimeDataForProbe(probe: any) {
    const reset = probe.status.reset;

    const monthMultiplier = this.getDayCountForMonth();

    const timeFactors: ITimeFactor = {
      second: {multiplier: 1000, inSeconds: 1},
      minute: {multiplier: 60, inSeconds: 60},
      hour: {multiplier: 60, inSeconds: 3600},
      day: {multiplier: 24, inSeconds: 86600},
      month: {multiplier: monthMultiplier, inSeconds: monthMultiplier * 86600},
      year: {multiplier: 12, inSeconds: 31536000}
    }

    const bottomText = this.getResetBottomText(reset);

    switch (probe.config.period) {
      case 'Second': return {
        current: new Date().getMilliseconds(), suffix: 'ms',
        limit: 1000, bottomText: this.translator.instant('APPS.RESETS_EVERY_SECOND')
      };
      case 'Minute': return {
        current: this.calcCurrentTimeValue(timeFactors.minute, reset),
        suffix: 'sec', limit: 60, bottomText
      };
      case 'Hour': return {
        current: this.calcCurrentTimeValue(timeFactors.hour, reset),
        suffix: 'min', limit: 60, bottomText
      };
      case 'Day': return {
        current: this.calcCurrentTimeValue(timeFactors.day, reset),
        suffix: 'hour', limit: 24, bottomText
      };
      case 'Month': return {
        current: this.calcCurrentTimeValue(timeFactors.month, reset),
        suffix: 'day', limit: this.getDayCountForMonth(), bottomText
      };
      case 'Year': return {
        current: this.calcCurrentTimeValue(timeFactors.year, reset) + 1,
        suffix: 'month', limit: 12, bottomText
      };
      default: return {current: -1, suffix: '', limit: -1, bottomText: 'invalid period'};
    }
  }

  /**
   * The function calculates in reference to the max value of a policy the remaining time
   * Schema: ([max value in sec] - [remaining time in sec]) / [max value in sec] * [unit multiplier]
   * @param timeUnitData
   * @param reset the value once the policy will reset the current value in seconds
   * @private
   */
  private calcCurrentTimeValue(timeUnitData: ITimeUnitData, reset: number){
    const currentTime = (timeUnitData.inSeconds - reset) / timeUnitData.inSeconds * timeUnitData.multiplier;
    return Math.floor(currentTime)
  }

  private getResetBottomText(reset: number){
    const date = new Date();
    // Get current time in millis
    // Add reset value (value is in seconds, we have to multiply with 1000 for millis
    let resetTimeStamp = date.getTime() + reset * 1000;

    // Round to the nearest minute because browser time can not show the exact value (e.g. 2021-10-20 18:50:58 -> 2021-10-20 18:51:00)
    // Add / subtract the timezone offset, otherwise the hour information would be wrong in timezones +- 1
    const resetDate = new Date(Math.round(resetTimeStamp / 60000) * 60000 + this.calcTimezoneOffsetInMillis());

    // Create date object and format to yyyy-mm-dd hh:mm:ss
    const dateString = new Date(resetDate.getTime()).toISOString().replace('T', ' ').slice(0, 19);

    return this.translator.instant('APPS.RESETS_AT') + ' ' + dateString;
  }

  private calcTimezoneOffsetInMillis(){
    const offset = new Date().getTimezoneOffset() * 60 * 1000 * -1;
    return offset;
  }


  private getDayCountForMonth () {
    const date = new Date();
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  }

  private getDataUnit(policy: IPolicyExt) {
    switch (policy.policyIdentifier) {
      case 'RATE_LIMIT': return ''; // Could also be 'requests'
      case 'TRANSFER_QUOTA': return 'MB, TODO';
      default: return '';
    }
  }
}
