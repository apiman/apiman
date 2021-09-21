import {Component, Input, OnInit} from '@angular/core';
import { Plan } from '../../interfaces/plan';
import { PlanService } from '../../services/plan/plan.service';
import { ActivatedRoute, Router } from "@angular/router";
import { SignUpService } from "../../services/sign-up/sign-up.service";
import {IApiVersion} from "../../interfaces/ICommunication";

@Component({
  selector: 'app-plan-card-list',
  templateUrl: './plan-card-list.component.html',
  styleUrls: ['./plan-card-list.component.scss'],
})
export class PlanCardListComponent implements OnInit {
  @Input() apiVersion!: IApiVersion;
  plans: Plan[] = [];

  constructor(private route: ActivatedRoute,
              private planService: PlanService,
              private signUpService: SignUpService,
              private router: Router
  ) {}
  orgId = '';
  apiId = '';

  ngOnInit(): void {
    this.orgId = this.route.snapshot.paramMap.get('orgId')!;
    this.apiId = this.route.snapshot.paramMap.get('apiId')!;
    this.getPlans();
  }

  // ToDo Change to backend call
  getPlans(): void {
    this.planService.getPlans().subscribe((plans) => (this.plans = plans));
  }

  onSignUp(plan: Plan) {
    this.signUpService.setSignUpInfo(this.apiVersion, plan, this.orgId);
    this.router.navigate(['/api-signup', this.orgId, this.apiId]);
  }
}
