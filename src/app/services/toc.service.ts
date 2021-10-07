import { Injectable } from '@angular/core';
import {IContractExt} from "../interfaces/IContractExt";

@Injectable({
  providedIn: 'root'
})
export class TocService {

  constructor() { }

  /**
   * Schema Example: applications#SpringCorp.-1.0-SupportManager-1.0
   * @param contract
   */
  formatApiVersionPlanId(contract: IContractExt): string {
    return this.formatClientId(contract) + '-' + contract.api.api.id + '-' + contract.api.version;
  }

  /**
   * Schema Example: applications#SpringCorp.-1.0
   * @param contract
   */
  formatClientId(contract: IContractExt) {
    return contract.client.client.id + '-' + contract.client.version;
  }
}
