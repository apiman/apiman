import { Injectable } from '@angular/core';
import { ISignUpInfo } from '../../interfaces/ISignUpInfo';
import { IApiPlanSummary, IApiVersion } from '../../interfaces/ICommunication';
import { IPolicyExt } from '../../interfaces/IPolicy';

@Injectable({
  providedIn: 'root'
})
export class SignUpService {
  private readonly key = 'APIMAN_NEW_CONTRACT_DETAILS';
  public getSignUpInfo(): ISignUpInfo {
    return JSON.parse(
      window.sessionStorage.getItem(this.key) as string
    ) as ISignUpInfo;
  }

  public setSignUpInfo(
    orgId: string,
    apiVersion: IApiVersion,
    plan: IApiPlanSummary,
    policies: IPolicyExt[],
    docsAvailable: boolean
  ): void {
    window.sessionStorage.setItem(
      this.key,
      JSON.stringify({
        apiVersion: apiVersion,
        plan: plan,
        organizationId: orgId,
        policies: policies,
        docsAvailable: docsAvailable
      })
    );
  }
}
