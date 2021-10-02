import {Component, Input, OnInit} from '@angular/core';
import { PlanService } from '../../services/plan/plan.service';
import { ActivatedRoute, Router } from "@angular/router";
import { SignUpService } from "../../services/sign-up/sign-up.service";
import {IApiPlanSummary, IApiVersion} from "../../interfaces/ICommunication";
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {flatMap} from "rxjs/internal/operators";
import {PolicyService} from "../../services/policy/policy.service";

@Component({
  selector: 'app-plan-card-list',
  templateUrl: './plan-card-list.component.html',
  styleUrls: ['./plan-card-list.component.scss'],
})
export class PlanCardListComponent implements OnInit {
  @Input() apiVersion!: IApiVersion;
  plans: IApiPlanSummary[] = [];

  constructor(private route: ActivatedRoute,
              private planService: PlanService,
              private policyService: PolicyService,
              private signUpService: SignUpService,
              private router: Router
  ) {}

  planPoliciesMap = new Map<string, IPolicyExt[]>();
  apiPolicies: IPolicyExt[] = [];
  orgId = '';
  apiId = '';

  ngOnInit(): void {
    this.orgId = this.route.snapshot.paramMap.get('orgId')!;
    this.apiId = this.route.snapshot.paramMap.get('apiId')!;
    this.fetchPlansAndPolicies();
  }

  onSignUp(plan: IApiPlanSummary) {
    const policies: IPolicyExt[] = [];
    const planIdVersionMapped = plan.planId + ':' + plan.version;
    const foundPlanPolicies = this.planPoliciesMap.get(planIdVersionMapped);

    if (foundPlanPolicies) {
      policies.push(...foundPlanPolicies);
    }
    if (this.apiPolicies.length > 0) {
      policies.concat(this.apiPolicies);
    }

    this.signUpService.setSignUpInfo(this.orgId, this.apiVersion, plan, policies);
    this.router.navigate(['/api-signup', this.orgId, this.apiId]);
  }

  private fetchPlansAndPolicies(): void {
    this.planService.getPlans(this.orgId, this.apiId, this.apiVersion.version).pipe(
      flatMap((apiPlanSummaries: IApiPlanSummary[]) => {
        this.plans = apiPlanSummaries;
        return apiPlanSummaries
      }),
      flatMap((apiPlanSummary: IApiPlanSummary) => {
        return this.policyService.getPlanPolicies(this.orgId, apiPlanSummary.planId, apiPlanSummary.version)
      })
    ).subscribe((extendedPolicy: IPolicyExt) => {
      this.extractPolicy(extendedPolicy);
    });
    this.policyService.getApiPolicies(this.orgId, this.apiId, this.apiVersion.version).subscribe((extendedApiPolicy: IPolicyExt) => {
      this.apiPolicies.push(extendedApiPolicy);
    })
  }

  private extractPolicy(policyExt: IPolicyExt) {
    const planIdVersionMapped = policyExt.planId + ':' + policyExt.planVersion;
    const foundPolicies = this.planPoliciesMap.get(planIdVersionMapped);
    if (foundPolicies) {
      this.planPoliciesMap.set(planIdVersionMapped, foundPolicies.concat(policyExt))
    } else {
      this.planPoliciesMap.set(planIdVersionMapped, [policyExt]);
    }
  }
}
