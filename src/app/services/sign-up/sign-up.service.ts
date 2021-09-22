import { Injectable } from '@angular/core';
import { ISignUpInfo } from '../../interfaces/ISignUpInfo';
import { IApiVersion } from '../../interfaces/ICommunication';
import { Plan } from '../../interfaces/plan';

@Injectable({
  providedIn: 'root',
})
export class SignUpService {
  signUpInfo?: ISignUpInfo;

  public getSignUpInfo(): ISignUpInfo {
    return this.signUpInfo!;
  }

  public setSignUpInfo(
    apiVersion: IApiVersion,
    plan: Plan,
    organizationId: string
  ): void {
    this.signUpInfo = {
      apiVersion: apiVersion,
      plan: plan,
      organizationId: organizationId,
    };
  }
}
