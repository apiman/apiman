import { Component, Input, OnInit } from '@angular/core';
import { IPolicy } from '../../interfaces/ICommunication';

export interface PolicyHeaders {
  headerLimit: string;
  headerRemaining: string;
  headerReset: string;
}

@Component({
  selector: 'app-policy-card',
  templateUrl: './policy-card.component.html',
  styleUrls: ['./policy-card.component.scss'],
})
export class PolicyCardComponent implements OnInit {
  @Input() policy?: IPolicy;
  headers: PolicyHeaders;
  icon: string;
  policyIdentifier: string;

  // Default Headers
  private rateLimitPolicyHeaders: PolicyHeaders = {
    headerLimit: 'X-RateLimit-Limit',
    headerRemaining: 'X-RateLimit-Remaining',
    headerReset: 'X-RateLimit-Reset',
  };
  private transferQuotaPolicyHeaders: PolicyHeaders = {
    headerLimit: 'X-TransferQuota-Limit',
    headerRemaining: 'X-TransferQuota-Remaining',
    headerReset: 'X-TransferQuota-Reset',
  };

  constructor() {
    if (this.policy?.definition.id === 'RateLimitingPolicy') {
      this.headers = this.rateLimitPolicyHeaders;
      this.icon = 'tune';
      this.policyIdentifier = 'RATE_LIMIT';
    } else {
      this.headers = this.transferQuotaPolicyHeaders;
      this.icon = 'import_export';
      this.policyIdentifier = 'QUOTA';
    }
  }

  policyConfig = {
    limit: '300',
    unit: 'MB',
    timeUnit: 'day',
  };

  ngOnInit(): void {}
}
