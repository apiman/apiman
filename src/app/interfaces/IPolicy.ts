import { IPolicy } from './ICommunication';
import { IGaugeChartData } from './IGaugeChartData';

export interface IPolicyExt extends IPolicy {
  planId: string;
  planVersion: string;
  shortName: string;
  shortDescription: string;
  configAsObject: IPolicyConfiguration;
  icon: string;
  restrictions: {
    limit: string;
    timeUnit: string;
  };
  headers: IPolicyHeaders;
  mainGaugeData: IGaugeChartData;
  timeGaugeData: IGaugeChartData;
  probe: IPolicyProbe;
}

export interface IPolicyHeaders {
  headerLimit: string;
  headerRemaining: string;
  headerReset: string;
}

export interface IPolicyProbe {
  probeType: string;
  config: IPolicyProbeConfig;
  status: IPolicyProbeStatus;
}

export interface IPolicyProbeConfig {
  limit: number;
  granularity: string;
  period: string;
  userHeader: null;
  headerRemaining: null;
  headerLimit: null;
  headerReset: null;
}

export interface IPolicyProbeStatus {
  accepted: boolean;
  remaining: number;
  reset: number;
}

export interface IPolicyConfiguration {
  limit: number;
  direction?: string;
  granularity: string;
  period: string;
  headerLimit?: string;
  headerRemaining?: string;
  headerReset?: string;
}
