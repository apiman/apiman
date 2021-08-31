import {Component, Input, OnInit} from '@angular/core';
import { Api } from '../../interfaces/api'
import {Plan} from "../../interfaces/plan";
import { ApiService } from "../../services/api/api.service";
import {PlanService} from "../../services/plan/plan.service";

@Component({
  selector: 'app-card-list',
  templateUrl: './card-list.component.html',
  styleUrls: ['./card-list.component.sass']
})
export class CardListComponent implements OnInit {

  apis: Api[] = [];
  plans: Plan[] = [];

  @Input() listType = "";
  @Input() cardType = "";

  constructor(private apiService: ApiService,
              private planService: PlanService) { }

  ngOnInit(): void {
    if (this.listType === "api") {
      this.getApis();
    } else if (this.listType === "featuredApi") {
      this.getFeaturedApis();
    } else {
      this.getPlans();
    }
  }

  getApis(): void {
    this.apiService.getApis()
      .subscribe(apis => this.apis = apis);
  }

  getFeaturedApis(): void {
    this.apiService.getFeaturedApis()
      .subscribe(apis => this.apis = apis);
  }

  getPlans(): void {
    this.planService.getPlans()
      .subscribe(plans => this.plans = plans);
  }
}
