import { Injectable } from '@angular/core';
import { ISignUpInfo } from '../../interfaces/ISignUpInfo';
import {IApiPlanSummary, IApiVersion} from '../../interfaces/ICommunication';
import {IPolicyExt} from "../../interfaces/IPolicyExt";

@Injectable({
  providedIn: 'root',
})
export class SignUpService {
  signUpInfo?: ISignUpInfo;

  public getSignUpInfo(): ISignUpInfo {
    return this.signUpInfo!;
  }

  public setSignUpInfo(
    orgId: string,
    apiVersion: IApiVersion,
    plan: IApiPlanSummary,
    policies: IPolicyExt[]
  ): void {
    this.signUpInfo = {
      apiVersion: apiVersion,
      plan: plan,
      organizationId: orgId,
      policies: policies
    };
  }
}
