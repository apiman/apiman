import { Component, Input } from '@angular/core';
import { IContractExt } from '../../interfaces/IContractExt';

@Component({
  selector: 'app-my-apps-manage-api',
  templateUrl: './my-apps-manage-api.component.html',
  styleUrls: ['./my-apps-manage-api.component.scss']
})
export class MyAppsManageApiComponent {
  @Input() api: IContractExt;

  constructor() {
    this.api = {} as IContractExt;
  }
}
