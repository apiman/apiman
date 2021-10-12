import {IPolicy} from "./ICommunication";
import {IGaugeChartData} from "./IGaugeChartData";

export interface IPolicyExt extends IPolicy {
  planId: string;
  planVersion: string;
  shortName: string;
  shortDescription: string;
  configAsObject: any;
  icon: string;
  policyIdentifier: string;
  restrictions: {
    limit: string;
    timeUnit: string;
  }
  headers: any;
  mainGaugeData: IGaugeChartData,
  timeGaugeData: IGaugeChartData
}
