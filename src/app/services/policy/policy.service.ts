/*
 * Copyright 2022 Scheer PAS Schweiz AG
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

import { Injectable } from '@angular/core';
import { IPolicy } from '../../interfaces/ICommunication';
import { IPolicySummaryExt } from '../../interfaces/IPolicySummaryExt';
import {
  IPolicyConfiguration,
  IPolicyExt,
  IPolicyHeaders,
  IPolicyProbe
} from '../../interfaces/IPolicy';
import { map, switchMap, tap } from 'rxjs/operators';
import { BackendService } from '../backend/backend.service';
import { forkJoin, Observable, of } from 'rxjs';
import { formatBytes, formatBytesAsObject } from '../../shared/utility';
import { IContractExt } from '../../interfaces/IContractExt';
import { TranslateService } from '@ngx-translate/core';
import { IGaugeChartData } from '../../interfaces/IGaugeChartData';

interface ITimeFactor {
  [timeUnit: string]: ITimeUnitData;
}

interface ITimeUnitData {
  multiplier: number;
  inSeconds: number;
}

@Injectable({
  providedIn: 'root'
})
export class PolicyService {
  // Default Headers
  private rateLimitPolicyHeaders: IPolicyHeaders = {
    headerLimit: 'X-RateLimit-Limit',
    headerRemaining: 'X-RateLimit-Remaining',
    headerReset: 'X-RateLimit-Reset'
  };

  private transferQuotaPolicyHeaders: IPolicyHeaders = {
    headerLimit: 'X-TransferQuota-Limit',
    headerRemaining: 'X-TransferQuota-Remaining',
    headerReset: 'X-TransferQuota-Reset'
  };

  public readonly policyIds = {
    RATE_LIMIT: 'RateLimitingPolicy',
    TRANSFER_QUOTA: 'TransferQuotaPolicy'
  };

  constructor(
    private backendService: BackendService,
    private translator: TranslateService
  ) {}

  public getPlanPolicies(
    orgId: string,
    planId: string,
    planVersion: string
  ): Observable<IPolicyExt[]> {
    return this.getExtendedPolicySummaries(orgId, planId, planVersion).pipe(
      switchMap((extendedPolicySummaries: IPolicySummaryExt[]) => {
        if (extendedPolicySummaries.length > 0) {
          return forkJoin(
            extendedPolicySummaries.map((policySummary) => {
              return this.getExtendedPlanPolicy(orgId, policySummary);
            })
          );
        } else {
          return of([] as IPolicyExt[]);
        }
      }),
      // in v1 only certain policies will be displayed
      map((extendedPolicies: IPolicyExt[]) => {
        return this.filterPolicies(extendedPolicies);
      })
    );
  }

  private getExtendedPlanPolicy(
    orgId: string,
    policySummary: IPolicySummaryExt
  ) {
    return this.backendService
      .getPlanPolicy(
        orgId,
        policySummary.planId,
        policySummary.planVersion,
        policySummary.id.toString()
      )
      .pipe(
        map((policy: IPolicy) => {
          return this.extendPlanPolicy(policySummary, policy);
        })
      );
  }

  public extendPolicy(policy: IPolicyExt): void {
    policy.configAsObject = this.getPolicyConfiguration(policy);

    switch (policy.definition.id) {
      case this.policyIds.RATE_LIMIT: {
        this.checkHeaders(policy, this.rateLimitPolicyHeaders);
        policy.icon = 'tune';
        policy.shortName = 'Rate Limit';
        policy.restrictions = {
          limit: policy.configAsObject.limit.toString(),
          timeUnit: this.getTranslationForPeriod(policy.configAsObject.period)
        };
        break;
      }
      case this.policyIds.TRANSFER_QUOTA: {
        this.checkHeaders(policy, this.transferQuotaPolicyHeaders);
        policy.icon = 'import_export';
        policy.shortName = 'Transfer Quota';
        policy.restrictions = {
          limit: formatBytes(policy.configAsObject.limit),
          timeUnit: this.getTranslationForPeriod(policy.configAsObject.period)
        };
        break;
      }
    }
  }

  private extendPlanPolicy(
    policySummary: IPolicySummaryExt,
    policy: IPolicy
  ): IPolicyExt {
    const extendedPolicy: IPolicyExt = policy as IPolicyExt;
    extendedPolicy.planId = policySummary.planId;
    extendedPolicy.planVersion = policySummary.planVersion;
    this.extendPolicy(extendedPolicy);
    return extendedPolicy;
  }

  private getExtendedPolicySummaries(
    orgId: string,
    planId: string,
    planVersion: string
  ) {
    return this.backendService
      .getPlanPolicySummaries(orgId, planId, planVersion)
      .pipe<IPolicySummaryExt[]>(
        tap((policySummaries) => {
          policySummaries.forEach((policySummary) => {
            const policySummaryExt = policySummary;
            policySummaryExt.planId = planId;
            policySummaryExt.planVersion = planVersion;
          });
        })
      );
  }

  public filterPolicies(extendedPolicies: IPolicyExt[]): IPolicyExt[] {
    return extendedPolicies
      .filter((extendedPolicy: IPolicyExt) => {
        return [
          this.policyIds.RATE_LIMIT,
          this.policyIds.TRANSFER_QUOTA
        ].includes(extendedPolicy.definition.id);
      })
      .sort((a, b) => (a.definition.id > b.definition.id ? 1 : -1));
  }

  private getPolicyConfiguration(policy: IPolicyExt): IPolicyConfiguration {
    // Config from backend is a JSON object in string representation
    return JSON.parse(policy.configuration) as IPolicyConfiguration;
  }

  public checkHeaders(
    policy: IPolicyExt,
    defaultHeaders: IPolicyHeaders
  ): void {
    // Copy default values, we need these multiple times
    const newHeaders = { ...defaultHeaders };

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

  public getPolicyProbe(
    contract: IContractExt,
    policy: IPolicy
  ): Observable<IPolicyProbe[]> {
    return this.backendService.getPolicyProbe(contract, policy);
  }

  public getTranslationForPeriod(period: string): string {
    return this.translator.instant(
      'POLICIES.PERIODS.' + period.toUpperCase()
    ) as string;
  }

  public setGaugeDataForPolicy(policy: IPolicyExt): void {
    if (!policy.probe) return;

    policy.mainGaugeData = this.generateMainGaugeData(policy);
    policy.timeGaugeData = this.generateTimeGaugeData(policy);
  }

  private generateMainGaugeData(policy: IPolicyExt): IGaugeChartData {
    const translation: string = this.translator.instant(
      'POLICIES.USAGE'
    ) as string;
    const mainGaugeData: IGaugeChartData = {
      name: policy.shortName,
      period: policy.configAsObject.period,
      infoHeader: `${translation} (${this.getTranslationForPeriod(
        policy.configAsObject.period
      )})`,
      currentVal: 0
    };

    policy.mainGaugeData = mainGaugeData;
    this.generateGaugeDataForPolicyType(policy);

    return mainGaugeData;
  }

  private generateTimeGaugeData(policy: IPolicyExt) {
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

  private generateGaugeDataForPolicyType(policy: IPolicyExt): void {
    switch (policy.definition.id) {
      case this.policyIds.RATE_LIMIT:
        return this.generateDataForRateLimit(policy);
      case this.policyIds.TRANSFER_QUOTA:
        return this.generateDataForQuota(policy);
      default:
        return;
    }
  }

  generateDataForRateLimit(policy: IPolicyExt): void {
    const currentVal = this.calcCurrentValue(
      policy.probe.config.limit,
      policy.probe.status.remaining
    );

    policy.mainGaugeData.currentVal = currentVal;
    policy.mainGaugeData.dividendSuffix = '';
    policy.mainGaugeData.divisorSuffix = '';
    policy.mainGaugeData.bottomText = this.generateCardBottomTextForRateLimit(
      policy,
      currentVal
    );
    policy.mainGaugeData.limit = policy.probe.config.limit;
  }

  generateDataForQuota(policy: IPolicyExt): void {
    const currentBytesValue = this.calcCurrentValue(
      policy.probe.config.limit,
      policy.probe.status.remaining
    );
    const bytesLimitFormatted = formatBytesAsObject(policy.probe.config.limit);

    policy.mainGaugeData.currentVal =
      Math.floor(
        (currentBytesValue / policy.probe.config.limit) *
          bytesLimitFormatted.value *
          100
      ) / 100;
    policy.mainGaugeData.bottomText = this.generateCardBottomTextForQuota(
      currentBytesValue,
      policy.probe.config.limit
    );
    policy.mainGaugeData.limit = formatBytesAsObject(
      policy.probe.config.limit
    ).value;
    policy.mainGaugeData.dividendSuffix = '';
    policy.mainGaugeData.divisorSuffix = bytesLimitFormatted.unit;
  }

  private generateCardBottomTextForRateLimit(
    policy: IPolicyExt,
    currentVal: number
  ): string {
    const percentage: string = (
      (currentVal * 100) /
      policy.probe.config.limit
    ).toFixed(2);
    const translation: string = this.translator.instant(
      'CLIENTS.USED'
    ) as string;
    return `${percentage} % ${translation}`;
  }

  private generateCardBottomTextForQuota(
    currentValFormatted: number,
    limitFormatted: number
  ) {
    const percentage: string = (
      (currentValFormatted * 100) /
      limitFormatted
    ).toFixed(2);
    const translation: string = this.translator.instant(
      'CLIENTS.USED'
    ) as string;
    return `${percentage} % ${translation}`;
  }

  private getTimeDataForProbe(probe: IPolicyProbe) {
    const reset = probe.status.reset;

    const monthMultiplier = this.getDayCountForMonth();

    const timeFactors: ITimeFactor = {
      second: { multiplier: 1000, inSeconds: 1 },
      minute: { multiplier: 60, inSeconds: 60 },
      hour: { multiplier: 60, inSeconds: 3600 },
      day: { multiplier: 24, inSeconds: 86600 },
      month: {
        multiplier: monthMultiplier,
        inSeconds: monthMultiplier * 86600
      },
      year: { multiplier: 12, inSeconds: 31536000 }
    };

    const bottomText = this.getResetBottomText(reset);

    switch (probe.config.period) {
      case 'Second':
        return {
          current: new Date().getMilliseconds(),
          suffix: 'ms',
          limit: 1000,
          bottomText: this.translator.instant(
            'CLIENTS.RESETS_EVERY_SECOND'
          ) as string
        };
      case 'Minute':
        return {
          current: this.calcCurrentTimeValue(timeFactors.minute, reset),
          suffix: 's',
          limit: 60,
          bottomText
        };
      case 'Hour':
        return {
          current: this.calcCurrentTimeValue(timeFactors.hour, reset),
          suffix: 'min',
          limit: 60,
          bottomText
        };
      case 'Day':
        return {
          current: this.calcCurrentTimeValue(timeFactors.day, reset),
          suffix: 'h',
          limit: 24,
          bottomText
        };
      case 'Month':
        return {
          current: this.calcCurrentTimeValue(timeFactors.month, reset),
          suffix: 'd',
          limit: this.getDayCountForMonth(),
          bottomText
        };
      case 'Year':
        return {
          current: this.calcCurrentTimeValue(timeFactors.year, reset) + 1,
          suffix: 'm',
          limit: 12,
          bottomText
        };
      default:
        return {
          current: -1,
          suffix: '',
          limit: -1,
          bottomText: 'invalid period'
        };
    }
  }

  /**
   * The function calculates in reference to the max value of a policy the remaining time
   * Schema: ([max value in sec] - [remaining time in sec]) / [max value in sec] * [unit multiplier]
   * @param timeUnitData
   * @param reset the value once the policy will reset the current value in seconds
   * @private
   */
  private calcCurrentTimeValue(
    timeUnitData: ITimeUnitData,
    reset: number
  ): number {
    const currentTime =
      ((timeUnitData.inSeconds - reset) / timeUnitData.inSeconds) *
      timeUnitData.multiplier;
    return Math.floor(currentTime);
  }

  private getResetBottomText(reset: number) {
    const date = new Date();
    // Get current time in millis
    // Add reset value (value is in seconds, we have to multiply with 1000 for millis
    const resetTimeStamp = date.getTime() + reset * 1000;

    // Round to the nearest minute because browser time can not show the exact value (e.g. 2021-10-20 18:50:58 -> 2021-10-20 18:51:00)
    // Add / subtract the timezone offset, otherwise the hour information would be wrong in timezones +- 1
    const resetDate = new Date(
      Math.round(resetTimeStamp / 60000) * 60000 +
        this.calcTimezoneOffsetInMillis()
    );

    // Create date object and format to yyyy-mm-dd hh:mm:ss
    const dateString = new Date(resetDate.getTime())
      .toISOString()
      .replace('T', ' ')
      .slice(0, 19);

    return (
      (this.translator.instant('CLIENTS.RESETS_AT') as string) +
      ' ' +
      dateString
    );
  }

  private calcTimezoneOffsetInMillis() {
    const offset = new Date().getTimezoneOffset() * 60 * 1000 * -1;
    return offset;
  }

  private getDayCountForMonth() {
    const date = new Date();
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  }

  /**
   * Calculates the current value based on the limit and remaining value.
   * Current value cannot be larger than the limit.
   * @param limit the limit value
   * @param remaining the remaining value, remaining will be -1 if limit is reached
   */
  private calcCurrentValue(limit: number, remaining: number): number {
    return Math.min(limit - remaining, limit);
  }
}
