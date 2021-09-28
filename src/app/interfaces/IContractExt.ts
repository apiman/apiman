import { IContract, IPolicy } from './ICommunication';

/**
 * Extends the Contract interface
 * E.g. used for My-Apps component to keep the selected section of a client
 * Further it extends the contract object with policies
 */
export interface IContractExt extends IContract {
  section: 'summary' | 'use-api' | 'policies' | 'manage-api';
  policies: IPolicy[];
}
