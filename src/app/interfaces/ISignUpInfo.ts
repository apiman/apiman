import {IApiVersion} from "./ICommunication";
import {Plan} from "./plan";

export interface ISignUpInfo {
  api: IApiVersion;
  plan: Plan;
  organizationId: string;
}
