import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IPolicyExt} from '../../interfaces/IPolicyExt';
import {PolicyService} from '../../services/policy/policy.service';
import {IContractExt} from '../../interfaces/IContractExt';
import {formatBytes} from "../../shared/utility";

@Component({
  selector: 'app-policy-card-light',
  templateUrl: './policy-card-light.component.html',
  styleUrls: ['./policy-card-light.component.scss']
})
export class PolicyCardLightComponent implements OnInit {
  @Input() policy!: IPolicyExt;
  @Input() contract?: IContractExt;

  @Output() sectionChanged = new EventEmitter();

  constructor() {}

  ngOnInit(): void {}

  setSectionToPolicies() {
    if (!this.contract)
      return

    this.sectionChanged.emit({contract: this.contract, section: 'policies'})
  }
}
