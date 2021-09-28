import { Component, Input, OnInit } from '@angular/core';
import { IPolicy } from '../../interfaces/ICommunication';
import { IContractExt } from '../../interfaces/IContractExt';

@Component({
  selector: 'app-my-apps-policies',
  templateUrl: './my-apps-policies.component.html',
  styleUrls: ['./my-apps-policies.component.scss'],
})
export class MyAppsPoliciesComponent implements OnInit {
  @Input() contract?: IContractExt;
  policies: IPolicy[] | undefined;

  ngOnInit(): void {
    this.getAllPolicies();
  }

  private getAllPolicies() {
    // TODO
    const policy: IPolicy = {
      id: 1632385073748121,
      type: 'Api',
      organizationId: 'Petstore',
      entityId: 'Petstore',
      entityVersion: '1.0',
      name: 'Rate Limiting Policy',
      description: 'Consumers are limited to 2 requests per Client per Second.',
      configuration:
        '{"limit":2,"granularity":"Client","period":"Second","headerLimit":"ABC","headerRemaining":"ABCD","headerReset":"ABCDEF"}',
      createdBy: 'support.is',
      createdOn: 1632385073687,
      modifiedBy: 'support.is',
      modifiedOn: 1632385147692,
      definition: {
        id: 'RateLimitingPolicy',
        policyImpl:
          'class:io.apiman.gateway.engine.policies.RateLimitingPolicy',
        name: 'Rate Limiting Policy',
        description:
          "Enforces rate configurable request rate limits on an API.  This ensures that consumers can't overload an API with too many requests.",
        icon: 'sliders',
        templates: [
          {
            language: null,
            template:
              'Consumers are limited to ${limit} requests per ${granularity} per ${period}.',
          },
        ],
        formType: 'Default',
        deleted: false,
      },
      orderIndex: 2,
    };
    if (this.contract) {
      this.policies = this.contract.policies;
    }
    this.policies = [policy, policy];
  }
}
