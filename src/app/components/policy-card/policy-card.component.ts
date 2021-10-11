import { Component, Input, OnInit } from '@angular/core';
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {PolicyService} from "../../services/policy/policy.service";

@Component({
  selector: 'app-policy-card',
  templateUrl: './policy-card.component.html',
  styleUrls: ['./policy-card.component.scss'],
})
export class PolicyCardComponent implements OnInit {
  @Input() policy!: IPolicyExt;

  constructor(private policyService: PolicyService) {}

  ngOnInit(): void {
    this.policy = this.policyService.initPolicy(this.policy);
  }
}
