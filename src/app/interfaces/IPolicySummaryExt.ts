import { IPolicySummary } from './ICommunication';

export interface IPolicySummaryExt extends IPolicySummary {
  planId: string;
  planVersion: string;
}
