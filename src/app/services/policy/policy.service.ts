import { Injectable } from '@angular/core';
import {IPolicy} from "../../interfaces/ICommunication";
import {IPolicySummaryExt} from "../../interfaces/IPolicySummaryExt";
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {map, mergeMap, tap} from "rxjs/operators";
import {BackendService} from "../backend/backend.service";
import {forkJoin, Observable, of} from "rxjs";
import {formatBytes} from "../../shared/utility";

@Injectable({
  providedIn: 'root'
})
export class PolicyService {

  constructor(private backendService: BackendService) {}

  public getPlanPolicies(orgId: string, planId: string, planVersion: string): Observable<IPolicyExt[]> {
    return this.getExtendedPolicySummaries(orgId, planId, planVersion).pipe(
      mergeMap((extendedPolicySummaries: IPolicySummaryExt[]) => {
        if (extendedPolicySummaries.length > 0){
          return forkJoin(extendedPolicySummaries.map(policySummary => {
            return this.getExtendedPlanPolicy(orgId, policySummary)
          }))
        } else {
         return of([] as IPolicyExt[])
        }
      })
    )
  };

  public getApiPolicies(orgId: string, apiId: string, apiVersion: string): Observable<IPolicyExt[]> {
    return this.backendService.getApiPolicySummaries(orgId, apiId, apiVersion).pipe(
      mergeMap((extendedPolicySummaries: IPolicySummaryExt[]) => {
        if (extendedPolicySummaries.length > 0) {
          return forkJoin(extendedPolicySummaries.map(extendedPolicySummary => {
            return this.getExtendedApiPolicy(orgId, apiId, apiVersion, extendedPolicySummary.id.toString())
          }))
        } else {
          return of([] as IPolicyExt[])
        }
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
      case 'RateLimitingPolicy': {
        extendedPolicy.shortName = 'Rate Limit'
        extendedPolicy.shortDescription = `${policyConfig.limit} Request${policyConfig.limit > 1 ? 's' : ''} per ${policyConfig.period}`
        break;
      }
      case 'TransferQuotaPolicy': {
        extendedPolicy.shortName = 'Quota'
        extendedPolicy.shortDescription = `${formatBytes(policyConfig.limit)} per ${policyConfig.period}`
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
          policySummaryExt.planId = planId
          policySummaryExt.planVersion = planVersion
        })
      })
    )
  }
}
