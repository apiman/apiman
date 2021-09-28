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
          limit = this.formatBytes(config.limit);
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
      defaultHeaders.headerLimit = config.headerRemaining;
    }
    if (config.hasOwnProperty('headerReset')) {
      defaultHeaders.headerLimit = config.headerReset;
    }
    this.headers = defaultHeaders;
  }

  private formatBytes(bytes: number, decimals = 0): string {
    // Thankfully taken from https://stackoverflow.com/a/18650828
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }
}
