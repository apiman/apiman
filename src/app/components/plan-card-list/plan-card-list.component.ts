/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

import { Component, Input, OnInit } from '@angular/core';
import { PlanService } from '../../services/plan/plan.service';
import { ActivatedRoute, Router } from '@angular/router';
import { SignUpService } from '../../services/sign-up/sign-up.service';
import { IApiPlanSummary } from '../../interfaces/ICommunication';
import { IPolicyExt } from '../../interfaces/IPolicy';
import { PolicyService } from '../../services/policy/policy.service';
import { IApiVersionExt } from '../../interfaces/IApiVersionExt';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-plan-card-list',
  templateUrl: './plan-card-list.component.html',
  styleUrls: ['./plan-card-list.component.scss']
})
export class PlanCardListComponent implements OnInit {
  @Input() apiVersion!: IApiVersionExt;
  plans: IApiPlanSummary[] = [];
  readonly defaultPolicies: string[] = ['Rate Limit', 'Transfer Quota'];

  constructor(
    private route: ActivatedRoute,
    private planService: PlanService,
    private policyService: PolicyService,
    private signUpService: SignUpService,
    private router: Router
  ) {}

  apiPolicies: IPolicyExt[] = [];
  orgId = '';
  apiId = '';

  ngOnInit(): void {
    this.orgId = this.route.snapshot.paramMap.get('orgId') ?? '';
    this.apiId = this.route.snapshot.paramMap.get('apiId') ?? '';
    this.fetchPlansAndPolicies();
  }

  onSignUp(plan: IApiPlanSummary): void {
    this.signUpService.setSignUpInfo(
      this.orgId,
      this.apiVersion,
      plan,
      plan.planPolicies,
      this.apiVersion.docsAvailable
    );
    void this.router.navigate(['/api-signup']);
  }

  private fetchPlansAndPolicies(): void {
    this.planService
      .getPlans(this.orgId, this.apiId, this.apiVersion.version)
      .pipe(
        map((apiPlanSummaries: IApiPlanSummary[]) => {
          apiPlanSummaries.forEach((apiPlanSummary) => {
            // in v1 only certain policies will be displayed
            apiPlanSummary.planPolicies = this.policyService.filterPolicies(
              apiPlanSummary.planPolicies
            );
            apiPlanSummary.planPolicies.forEach((policy: IPolicyExt) => {
              this.policyService.extendPolicy(policy);
            });
          });
          return apiPlanSummaries;
        })
      )
      .subscribe((apiPlanSummaries: IApiPlanSummary[]) => {
        this.plans = apiPlanSummaries;
      });
  }
}
