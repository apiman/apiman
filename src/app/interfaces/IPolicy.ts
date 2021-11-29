/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

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
