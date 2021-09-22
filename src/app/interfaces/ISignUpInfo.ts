import { IApiVersion } from './ICommunication';
import { Plan } from './plan';

export interface ISignUpInfo {
  apiVersion: IApiVersion;
  plan: Plan;
  organizationId: string;
}
