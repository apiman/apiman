import { IApiPlanSummary, IApiVersion } from './ICommunication';
import { IPolicyExt } from './IPolicy';

export interface ISignUpInfo {
  apiVersion: IApiVersion;
  plan: IApiPlanSummary;
  policies: IPolicyExt[];
  organizationId: string;
  docsAvailable: boolean;
}
