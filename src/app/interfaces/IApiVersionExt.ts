import { IApiVersion } from './ICommunication';

export interface IApiVersionExt extends IApiVersion {
  docsAvailable: boolean;
}
