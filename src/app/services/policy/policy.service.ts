import { Injectable } from '@angular/core';
import {IPolicy} from "../../interfaces/ICommunication";
import {IPolicySummaryExt} from "../../interfaces/IPolicySummaryExt";
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {map, switchMap, tap} from "rxjs/operators";
import {BackendService} from "../backend/backend.service";
import {forkJoin, Observable, of} from "rxjs";
import {formatBytes, formatBytesAsObject} from "../../shared/utility";
import {IContractExt} from "../../interfaces/IContractExt";
import {constantCase} from "constant-case";
import {TranslateService} from "@ngx-translate/core";
import {IGaugeChartData} from "../../interfaces/IGaugeChartData";

export interface PolicyHeaders {
  headerLimit: string;
  headerRemaining: string;
  headerReset: string;
}

interface ITimeFactor{
  [timeUnit: string]: ITimeUnitData
}

interface ITimeUnitData{
  multiplier: number,
  inSeconds: number
}

@Injectable({
  providedIn: 'root'
})
export class PolicyService {
  // Default Headers
  private rateLimitPolicyHeaders: PolicyHeaders = {
    headerLimit: 'X-RateLimit-Limit',
    headerRemaining: 'X-RateLimit-Remaining',
    headerReset: 'X-RateLimit-Reset',
  };

  private transferQuotaPolicyHeaders: PolicyHeaders = {
    headerLimit: 'X-TransferQuota-Limit',
    headerRemaining: 'X-TransferQuota-Remaining',
    headerReset: 'X-TransferQuota-Reset',
  };

  public readonly policyIds = {
    RATE_LIMIT: 'RateLimitingPolicy',
    T_QUOTA: 'TransferQuotaPolicy'
  };

  constructor(private backendService: BackendService, private translator: TranslateService) {}

  public getPlanPolicies(orgId: string, planId: string, planVersion: string): Observable<IPolicyExt[]> {
    return this.getExtendedPolicySummaries(orgId, planId, planVersion).pipe(
      switchMap((extendedPolicySummaries: IPolicySummaryExt[]) => {
        if (extendedPolicySummaries.length > 0){
          return forkJoin(extendedPolicySummaries.map(policySummary => {
            return this.getExtendedPlanPolicy(orgId, policySummary)
          }))
        } else {
         return of([] as IPolicyExt[])
        }
      }),
      // in v1 only certain policies will be displayed
      switchMap((extendedPolicies: IPolicyExt[]) => {
        return this.filterPolicies(extendedPolicies);
      })
    )
  };

  public getApiPolicies(orgId: string, apiId: string, apiVersion: string): Observable<IPolicyExt[]> {
    return this.backendService.getApiPolicySummaries(orgId, apiId, apiVersion).pipe(
      switchMap((extendedPolicySummaries: IPolicySummaryExt[]) => {
        if (extendedPolicySummaries.length > 0) {
          return forkJoin(extendedPolicySummaries.map(extendedPolicySummary => {
            return this.getExtendedApiPolicy(orgId, apiId, apiVersion, extendedPolicySummary.id.toString())
          }))
        } else {
          return of([] as IPolicyExt[])
        }
      }),
      // in v1 only certain policies will be displayed
      switchMap((extendedPolicies: IPolicyExt[]) => {
        return this.filterPolicies(extendedPolicies);
      })
    )
  }

  private getExtendedApiPolicy(orgId: string, apiId: string, apiVersion: string, apiPolicyId: string) {
    return this.backendService.getApiPolicy(orgId, apiId, apiVersion, apiPolicyId).pipe(
      map((policy: IPolicy) => {
        const extendedPolicy: IPolicyExt = policy as IPolicyExt;
        this.extendPolicy(extendedPolicy);
        return extendedPolicy;
      })
    )
  }

  private getExtendedPlanPolicy(orgId: string, policySummary: IPolicySummaryExt) {
    return this.backendService.getPlanPolicy(orgId,
      policySummary.planId,
      policySummary.planVersion,
      policySummary.id.toString()).pipe(
      map((policy: IPolicy) => {
        return this.extendPlanPolicy(policySummary, policy);
      })
    )
  }

  private extendPolicy(extendedPolicy: IPolicyExt) {
    const perTranslated = this.translator.instant('COMMON.PER');
    const policyConfig = this.getPolicyConfiguration(extendedPolicy);
    const periodTranslated = this.getTranslationForPeriod(policyConfig.period);

    switch (extendedPolicy.definition.id) {
      case this.policyIds.RATE_LIMIT: {
        extendedPolicy.shortName = 'Rate Limit';
        extendedPolicy.shortDescription = `${policyConfig.limit} Request${policyConfig.limit > 1 ? 's' : ''} ${perTranslated} ${periodTranslated}`;
        break;
      }
      case this.policyIds.T_QUOTA: {
        extendedPolicy.shortName = 'Quota';
        extendedPolicy.shortDescription = `${formatBytes(policyConfig.limit)} ${perTranslated} ${periodTranslated}`;
        break;
      }
    }
  }

  private extendPlanPolicy(policySummary: IPolicySummaryExt, policy: IPolicy): IPolicyExt {
    const extendedPolicy: IPolicyExt = policy as IPolicyExt;
    extendedPolicy.planId = policySummary.planId;
    extendedPolicy.planVersion = policySummary.planVersion;
    this.extendPolicy(extendedPolicy);
    return extendedPolicy;
  }

  private getExtendedPolicySummaries(orgId: string, planId: string, planVersion: string) {
    return this.backendService.getPlanPolicySummaries(
      orgId,
      planId,
      planVersion).pipe<IPolicySummaryExt[]>(
      tap(policySummaries => {
        policySummaries.forEach(policySummary => {
          const policySummaryExt = policySummary as IPolicySummaryExt;
          policySummaryExt.planId = planId;
          policySummaryExt.planVersion = planVersion;
        })
      })
    )
  }

  private filterPolicies(extendedPolicies: IPolicyExt[]): Observable<IPolicyExt[]> {
    const filteredExtendedPolicies: IPolicyExt[] = [];
    extendedPolicies.forEach((extendedPolicy) => {
      if ([this.policyIds.RATE_LIMIT, this.policyIds.T_QUOTA].includes(extendedPolicy.definition.id)) {
        filteredExtendedPolicies.push(extendedPolicy);
      }
    })
    return of(filteredExtendedPolicies);
  }

  public initPolicy(policy: IPolicyExt) {
    policy.configAsObject = this.getPolicyConfiguration(policy);

    switch (policy.definition.id) {
      case this.policyIds.RATE_LIMIT: {
        this.checkHeaders(policy, this.rateLimitPolicyHeaders);
        policy.icon = 'tune';
        policy.restrictions = this.getRestrictions(policy.configAsObject.limit, this.getTranslationForPeriod(policy.configAsObject.period));
        break;
      }
      case this.policyIds.T_QUOTA: {
        this.checkHeaders(policy, this.transferQuotaPolicyHeaders);
        policy.icon = 'import_export';
        policy.restrictions = this.getRestrictions(formatBytes(policy.configAsObject.limit), this.getTranslationForPeriod(policy.configAsObject.period));
        break;
      }
    }

    return policy;
  }

  private getPolicyConfiguration(policy: IPolicyExt) {
    // Config from backend is a JSON object in string representation
    return JSON.parse(policy.configuration);
  }

  private getRestrictions(limit: string, timeUnit: string){
    return {
      limit: limit,
      timeUnit: timeUnit
    };
  }

  public checkHeaders(policy: IPolicyExt, defaultHeaders: PolicyHeaders) {
    // Copy default values, we need these multiple times
    let newHeaders = {...defaultHeaders};

    if (policy.configAsObject.headerLimit) {
      newHeaders.headerLimit = policy.configAsObject.headerLimit;
    }
    if (policy.configAsObject.headerRemaining) {
      newHeaders.headerRemaining = policy.configAsObject.headerRemaining;
    }
    if (policy.configAsObject.headerReset) {
      newHeaders.headerReset = policy.configAsObject.headerReset;
    }

    policy.headers = newHeaders;
  }

  public getPolicyProbe(contract: IContractExt, policy: IPolicy): Observable<any> {
    return this.backendService.getPolicyProbe(contract, policy);
  }

  public getTranslationForPeriod(period: string) {
    return this.translator.instant('POLICIES.PERIODS.' + constantCase(period));
  }

  public setGaugeDataForPolicy(policy: IPolicyExt) {
    if (!policy.probe)
      return;

    policy.mainGaugeData = this.generateMainGaugeData(policy);
    policy.timeGaugeData = this.generateTimeGaugeData(policy);
  }

  private generateMainGaugeData(policy: IPolicyExt){
    const mainGaugeData: IGaugeChartData = {
      name: policy.shortName,
      period: policy.configAsObject.period,
      infoHeader: `${this.translator.instant('POLICIES.USAGE')} (${this.getTranslationForPeriod(policy.configAsObject.period)})`,
      currentVal: 0,
    };

    policy.mainGaugeData = mainGaugeData;
    this.generateGaugeDataForPolicyType(policy);

    return mainGaugeData;
  }

  private generateTimeGaugeData(policy: IPolicyExt){
    const timeData = this.getTimeDataForProbe(policy.probe);
    return {
      name: 'Reset Timer',
      limit: timeData.limit,
      currentVal: timeData.current,
      divisorSuffix: timeData.suffix,
      period: policy.configAsObject.period,
      remaining: policy.probe.status.reset,
      infoHeader: 'Countdown',
      bottomText: timeData.bottomText
    };
  }

  private generateGaugeDataForPolicyType(policy: IPolicyExt): any{
    switch (policy.definition.id){
      case this.policyIds.RATE_LIMIT: return this.generateDataForRateLimit(policy);
      case this.policyIds.T_QUOTA: return this.generateDataForQuota(policy);
      default: return {};
    }
  }

  generateDataForRateLimit(policy: IPolicyExt){
    const currentVal = policy.probe.config.limit - policy.probe.status.remaining
    policy.mainGaugeData.currentVal = currentVal;
    policy.mainGaugeData.dividendSuffix = '';
    policy.mainGaugeData.divisorSuffix = '';
    policy.mainGaugeData.bottomText = this.generateCardBottomTextForRateLimit(policy, currentVal);
    policy.mainGaugeData.limit = policy.probe.config.limit;
  }

  generateDataForQuota(policy: IPolicyExt){
    const currentBytesValue = policy.probe.config.limit - policy.probe.status.remaining;
    const bytesLimitFormatted = formatBytesAsObject(policy.probe.config.limit);

    policy.mainGaugeData.currentVal = Math.floor(currentBytesValue / policy.probe.config.limit * bytesLimitFormatted.value * 100) / 100;
    policy.mainGaugeData.bottomText = this.generateCardBottomTextForQuota(currentBytesValue, policy.probe.config.limit);
    policy.mainGaugeData.limit = formatBytesAsObject(policy.probe.config.limit).value;
    policy.mainGaugeData.dividendSuffix = '';
    policy.mainGaugeData.divisorSuffix = bytesLimitFormatted.unit;
  }

  private generateCardBottomTextForRateLimit(policy: IPolicyExt, currentVal: number) {
    return ((currentVal) * 100 / policy.probe.config.limit).toFixed(2)
      + ' % '
      + this.translator.instant('APPS.USED');
  }

  private generateCardBottomTextForQuota(currentValFormatted: number, limitFormatted: number) {
    return ((currentValFormatted * 100) / limitFormatted).toFixed(2)
      + ' % '
      + this.translator.instant('APPS.USED');
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
        suffix: 's', limit: 60, bottomText
      };
      case 'Hour': return {
        current: this.calcCurrentTimeValue(timeFactors.hour, reset),
        suffix: 'min', limit: 60, bottomText
      };
      case 'Day': return {
        current: this.calcCurrentTimeValue(timeFactors.day, reset),
        suffix: 'h', limit: 24, bottomText
      };
      case 'Month': return {
        current: this.calcCurrentTimeValue(timeFactors.month, reset),
        suffix: 'd', limit: this.getDayCountForMonth(), bottomText
      };
      case 'Year': return {
        current: this.calcCurrentTimeValue(timeFactors.year, reset) + 1,
        suffix: 'm', limit: 12, bottomText
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
}
