import { Injectable } from '@angular/core';
import {ISignUpInfo} from "../../interfaces/ISignUpInfo";
import {IApiVersion} from "../../interfaces/ICommunication";
import {Plan} from "../../interfaces/plan";

@Injectable({
  providedIn: 'root'
})
export class SignUpService {

  signUpInfo: ISignUpInfo | undefined;

  constructor() { }

  public getSignUpInfo(): ISignUpInfo | undefined {
    return this.signUpInfo;
  }

  public setSignUpInfo(apiVersion: IApiVersion, plan: Plan, organizationId: string): void {
    this.signUpInfo = {
      api: apiVersion,
      plan: plan,
      organizationId: organizationId
    }
  }
}
