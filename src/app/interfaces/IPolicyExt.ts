import {IPolicy} from "./ICommunication";

export interface IPolicyExt extends IPolicy {
  planId: string;
  planVersion: string;
  shortName: string;
  shortDescription: string;
}
