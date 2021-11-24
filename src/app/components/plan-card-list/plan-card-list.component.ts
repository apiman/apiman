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
    const policies: IPolicyExt[] = plan.planPolicies;
    policies.forEach((policy: IPolicyExt) =>
      this.policyService.initPolicy(policy)
    );
    this.signUpService.setSignUpInfo(
      this.orgId,
      this.apiVersion,
      plan,
      policies,
      this.apiVersion.docsAvailable
    );
    void this.router.navigate(['/api-signup', this.orgId, this.apiId]);
  }

  private fetchPlansAndPolicies(): void {
    this.planService
      .getPlans(this.orgId, this.apiId, this.apiVersion.version)
      .pipe(
        map((apiPlanSummaries: IApiPlanSummary[]) => {
          apiPlanSummaries.forEach((apiPlanSummary) => {
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
