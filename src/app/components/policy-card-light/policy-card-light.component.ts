import { Component, EventEmitter, Input, Output } from '@angular/core';
import { IPolicyExt } from '../../interfaces/IPolicy';
import { IContractExt } from '../../interfaces/IContractExt';

@Component({
  selector: 'app-policy-card-light',
  templateUrl: './policy-card-light.component.html',
  styleUrls: ['./policy-card-light.component.scss']
})
export class PolicyCardLightComponent {
  @Input() policy!: IPolicyExt;
  @Input() contract?: IContractExt;

  @Output() sectionChanged = new EventEmitter();

  constructor() {}

  setSectionToPolicies(): void {
    if (!this.contract) return;

    this.sectionChanged.emit({ contract: this.contract, section: 'policies' });
  }
}
