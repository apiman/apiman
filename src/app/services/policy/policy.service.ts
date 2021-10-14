import { Injectable } from '@angular/core';
import {IPolicy} from "../../interfaces/ICommunication";
import {IPolicySummaryExt} from "../../interfaces/IPolicySummaryExt";
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {map, switchMap, tap} from "rxjs/operators";
import {BackendService} from "../backend/backend.service";
import {forkJoin, Observable, of} from "rxjs";
import {formatBytes} from "../../shared/utility";
import {IContractExt} from "../../interfaces/IContractExt";

export interface PolicyHeaders {
  headerLimit: string;
  headerRemaining: string;
  headerReset: string;
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
    RATE_LIMITING: 'RateLimitingPolicy',
    TRANSFER_QUOTA: 'TransferQuotaPolicy'
  };

  constructor(private backendService: BackendService) {}

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
    const policyConfig = JSON.parse(extendedPolicy.configuration);
    switch (extendedPolicy.definition.id) {
      case this.policyIds.RATE_LIMITING: {
        extendedPolicy.shortName = 'Rate Limit';
        extendedPolicy.shortDescription = `${policyConfig.limit} Request${policyConfig.limit > 1 ? 's' : ''} per ${policyConfig.period}`;
        break;
      }
      case this.policyIds.TRANSFER_QUOTA: {
        extendedPolicy.shortName = 'Quota';
        extendedPolicy.shortDescription = `${formatBytes(policyConfig.limit)} per ${policyConfig.period}`;
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
      if ([this.policyIds.RATE_LIMITING, this.policyIds.TRANSFER_QUOTA].includes(extendedPolicy.definition.id)) {
        filteredExtendedPolicies.push(extendedPolicy);
      }
    })
    return of(filteredExtendedPolicies);
  }

  public initPolicy(policy: IPolicyExt) {
    // Config from backend is a JSON object in string representation
    policy.configAsObject = JSON.parse(policy.configuration);

    switch (policy.definition.id) {
      case this.policyIds.RATE_LIMITING: {
        this.checkHeaders(policy, this.rateLimitPolicyHeaders);
        policy.icon = 'tune';
        policy.policyIdentifier = 'RATE_LIMIT';
        policy.restrictions = this.getRestrictions(policy.configAsObject.limit, policy.configAsObject.period);
        break;
      }
      case this.policyIds.TRANSFER_QUOTA: {
        this.checkHeaders(policy, this.transferQuotaPolicyHeaders);
        policy.icon = 'import_export';
        policy.policyIdentifier = 'QUOTA';
        policy.restrictions = this.getRestrictions(formatBytes(policy.configAsObject.limit), policy.configAsObject.period);
        break;
      }
    }

    return policy;
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
}
