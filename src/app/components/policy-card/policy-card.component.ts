import { Component, Input, OnInit } from '@angular/core';
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {formatBytes} from "../../shared/utility";

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
  @Input() policy?: IPolicyExt;

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

  headers: PolicyHeaders = {
    headerLimit: '',
    headerReset: '',
    headerRemaining: '',
  };
  icon: string | undefined;
  policyIdentifier: string | undefined;

  policyConfig = {
    limit: '',
    timeUnit: '',
  };

  ngOnInit(): void {
    if (this.policy) {
      const config = JSON.parse(this.policy?.configuration);
      const timeUnit = config.period;
      let limit = '';

      const policyId = this.policy?.definition.id;
      switch (policyId) {
        case 'RateLimitingPolicy': {
          this.checkHeaders(config, this.rateLimitPolicyHeaders);
          this.icon = 'tune';
          this.policyIdentifier = 'RATE_LIMIT';
          limit = config.limit;
          break;
        }
        case 'TransferQuotaPolicy': {
          this.checkHeaders(config, this.transferQuotaPolicyHeaders);
          this.icon = 'import_export';
          this.policyIdentifier = 'QUOTA';
          limit = formatBytes(config.limit);
          break;
        }
      }
      this.policyConfig = {
        limit: limit,
        timeUnit: timeUnit,
      };
    }
  }

  private checkHeaders(config: any, defaultHeaders: PolicyHeaders) {
    if (config.hasOwnProperty('headerLimit')) {
      defaultHeaders.headerLimit = config.headerLimit;
    }
    if (config.hasOwnProperty('headerRemaining')) {
      defaultHeaders.headerRemaining = config.headerRemaining;
    }
    if (config.hasOwnProperty('headerReset')) {
      defaultHeaders.headerReset = config.headerReset;
    }
    this.headers = defaultHeaders;
  }
}
