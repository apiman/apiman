import { IApiSummary } from './ICommunication';

export interface IApiSummaryExt extends IApiSummary {
  docsAvailable: boolean;
  latestVersion: string;
}
