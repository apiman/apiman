import {Component, Input, OnInit} from '@angular/core';
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

  // string because of bytes
  currentValue = '0';

  constructor(private policyService: PolicyService) {}

  ngOnInit(): void {
    this.policy = this.policyService.initPolicy(this.policy);
    this.setCurrentValue();
  }

  setSectionToPolicies() {
    if (this.contract) this.contract.section = 'policies';
  }

  setCurrentValue() {
    this.policyService.getPolicyProbe(this.contract!, this.policy).subscribe(
      (probeResult) => {
        if (this.policy.policyIdentifier === this.policyService.policyIds.TRANSFER_QUOTA) {
          this.currentValue = formatBytes((probeResult[0].config.limit - probeResult[0].status.remaining));
        } else {
          this.currentValue = String(probeResult[0].config.limit - probeResult[0].status.remaining);
        }
      },
      (error) => console.warn(error)
    );
  }
}
