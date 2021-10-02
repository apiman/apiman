import { Injectable } from '@angular/core';
import {flatMap} from "rxjs/internal/operators";
import {IPolicy} from "../../interfaces/ICommunication";
import {IPolicySummaryExt} from "../../interfaces/IPolicySummaryExt";
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {map, tap} from "rxjs/operators";
import {BackendService} from "../backend/backend.service";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class PolicyService {

  constructor(private backendService: BackendService) {}

  public getPlanPolicies(orgId: string, planId: string, planVersion: string): Observable<IPolicyExt> {
    return this.getExtendedPolicySummaries(orgId, planId, planVersion).pipe(
      flatMap((policySummaries: IPolicySummaryExt[]) => {
        return policySummaries
      }),
      flatMap((policySummary: IPolicySummaryExt) => {
        return this.getExtendedPlanPolicy(orgId, policySummary);
      })
    )
  };

  public getApiPolicies(orgId: string, apiId: string, apiVersion: string): Observable<IPolicyExt> {
    return this.backendService.getApiPolicySummaries(orgId, apiId, apiVersion).pipe(
      flatMap((apiPolicySummaries: IPolicySummaryExt[]) => {
        return apiPolicySummaries;
      }),
      flatMap((apiPolicySummary: IPolicySummaryExt) => {
        return this.getExtendedApiPolicy(orgId, apiId, apiVersion, apiPolicySummary.id.toString());
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
        extendedPolicy.shortDescription = `${this.formatBytes(policyConfig.limit)} per ${policyConfig.period}`
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

  private formatBytes(bytes: number, decimals = 0): string {
    // Thankfully taken from https://stackoverflow.com/a/18650828
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }
}
