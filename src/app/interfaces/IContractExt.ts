import { IContract } from './ICommunication';
import { IPolicyExt } from './IPolicyExt';

/**
 * Extends the Contract interface
 * E.g. used for My-Apps component to keep the selected section of a client
 * Further it extends the contract object with policies
 */
export interface IContractExt extends IContract {
  managedEndpoint: string;
  section: 'summary' | 'use-api' | 'policies' | 'manage-api';
  policies: IPolicyExt[];
  docsAvailable: boolean;
}
