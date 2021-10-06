import {Component, Input, OnInit} from '@angular/core';
import { PlanService } from '../../services/plan/plan.service';
import { ActivatedRoute, Router } from "@angular/router";
import { SignUpService } from "../../services/sign-up/sign-up.service";
import {IApiPlanSummary} from "../../interfaces/ICommunication";
import {IPolicyExt} from "../../interfaces/IPolicyExt";
import {PolicyService} from "../../services/policy/policy.service";
import {switchMap} from "rxjs/operators";
import {forkJoin} from "rxjs";
import {flatArray} from "../../shared/utility";
import {IApiVersionExt} from "../../interfaces/IApiVersionExt";
import {KeycloakService} from "keycloak-angular";

@Component({
  selector: 'app-plan-card-list',
  templateUrl: './plan-card-list.component.html',
  styleUrls: ['./plan-card-list.component.scss'],
})
export class PlanCardListComponent implements OnInit {
  @Input() apiVersion!: IApiVersionExt;
  plans: IApiPlanSummary[] = [];

  constructor(private route: ActivatedRoute,
              private planService: PlanService,
              private policyService: PolicyService,
              private signUpService: SignUpService,
              private router: Router,
              private keycloak: KeycloakService
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
    this.checkIfUserIsLoggedIn();

    const policies: IPolicyExt[] = [];
    const planIdVersionMapped = plan.planId + ':' + plan.version;
    const foundPlanPolicies = this.planPoliciesMap.get(planIdVersionMapped);

    if (foundPlanPolicies) {
      policies.push(...foundPlanPolicies);
    }
    if (this.apiPolicies.length > 0) {
      policies.concat(this.apiPolicies);
    }

    this.signUpService.setSignUpInfo(this.orgId, this.apiVersion, plan, policies, this.apiVersion.docsAvailable);
    this.router.navigate(['/api-signup', this.orgId, this.apiId]);
  }

  private fetchPlansAndPolicies(): void {
    this.planService.getPlans(this.orgId, this.apiId, this.apiVersion.version).pipe(
      switchMap((apiPlanSummaries: IApiPlanSummary[]) => {
        this.plans = apiPlanSummaries;
        return forkJoin(apiPlanSummaries.map(apiPlanSummary => {
          return this.policyService.getPlanPolicies(this.orgId, apiPlanSummary.planId, apiPlanSummary.version)
        }))
      })
    ).subscribe((nestedExtendedPolicies: IPolicyExt[][]) => {
      const extendedPolicies: IPolicyExt[] = flatArray(nestedExtendedPolicies);
      this.extractPolicies(extendedPolicies);
    });
    this.policyService.getApiPolicies(this.orgId, this.apiId, this.apiVersion.version).subscribe((extendedApiPolicies: IPolicyExt[]) => {
      this.apiPolicies = this.apiPolicies.concat(extendedApiPolicies);
    })
  }

  private extractPolicies(extendedPolicies: IPolicyExt[]) {
    extendedPolicies.forEach(extendedPolicy => {
      const planIdVersionMapped = extendedPolicy.planId + ':' + extendedPolicy.planVersion;
      const foundPolicies = this.planPoliciesMap.get(planIdVersionMapped);
      if (foundPolicies) {
        this.planPoliciesMap.set(planIdVersionMapped, foundPolicies.concat(extendedPolicy))
      } else {
        this.planPoliciesMap.set(planIdVersionMapped, [extendedPolicy]);
      }
    })
  }

  /**
   * Checks if the user is logged in and redirects to login in notGlau
   * @private
   */
  private checkIfUserIsLoggedIn() {
    this.keycloak.isLoggedIn().then((loggedIn) => {
      if (!loggedIn) {
        this.keycloak.login();
      }
    });
  }
}
