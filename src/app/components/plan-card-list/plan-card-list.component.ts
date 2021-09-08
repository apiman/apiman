import { Component, OnInit } from '@angular/core';
import {Plan} from "../../interfaces/plan";
import {PlanService} from "../../services/plan/plan.service";

@Component({
  selector: 'app-plan-card-list',
  templateUrl: './plan-card-list.component.html',
  styleUrls: ['./plan-card-list.component.sass']
})
export class PlanCardListComponent implements OnInit {


  plans: Plan[] = [];

  constructor(private planService: PlanService) { }

  ngOnInit(): void {
    this.getPlans();
  }

  // ToDo Change to backend call
  getPlans(): void {
    this.planService.getPlans()
      .subscribe(plans => this.plans = plans);
  }
}
