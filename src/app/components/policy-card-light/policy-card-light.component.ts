import {Component, Input, OnInit} from '@angular/core';
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {PolicyService} from "../../services/policy/policy.service";
import {IContractExt} from "../../interfaces/IContractExt";

@Component({
  selector: 'app-policy-card-light',
  templateUrl: './policy-card-light.component.html',
  styleUrls: ['./policy-card-light.component.scss']
})
export class PolicyCardLightComponent implements OnInit {
  @Input() policy!: IPolicyExt;
  @Input() contract?: IContractExt;

  constructor(private policyService: PolicyService) {}

  ngOnInit(): void {
    this.policy = this.policyService.initPolicy(this.policy!);
  }

  setSectionToPolicies() {
    if (this.contract)
      this.contract.section = 'policies';
  }
}
