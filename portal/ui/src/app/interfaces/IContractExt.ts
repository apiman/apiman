/*
 * Copyright 2022 Scheer PAS Schweiz AG
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

import { IContract } from './ICommunication';
import { IPolicyExt } from './IPolicy';

/**
 * Extends the Contract interface
 * E.g. used for My-Apps component to keep the selected section of a client
 * Further it extends the contract object with policies
 */
export interface IContractExt extends IContract {
  managedEndpoint: string;
  section: 'summary' | 'description' | 'use-api' | 'policies' | 'manage-api';
  policies: IPolicyExt[];
  docsAvailable: boolean;
}
