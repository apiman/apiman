import {Component, Input, OnInit} from '@angular/core';
import {Plan} from "../../interfaces/plan";
import { ApiService } from "../../services/api/api.service";
import {PlanService} from "../../services/plan/plan.service";
import {ApiSummaryBean} from "../../services/backend/backend.service";
import {PageEvent} from "@angular/material/paginator";

@Component({
  selector: 'app-card-list',
  templateUrl: './card-list.component.html',
  styleUrls: ['./card-list.component.sass']
})
export class CardListComponent implements OnInit {

  apis: ApiSummaryBean[] = [];
  plans: Plan[] = [];


  @Input() listType = "";
  @Input() cardType = "";
  pageEvent: void;
  inputEvent: void;

  constructor(public apiService: ApiService,
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

  OnInput(event:any){
    this.apiService.searchCriteria.paging.page = 1;
    this.apiService.searchCriteria.filters[0].value = '*' + event.target.value + '*';
    this.getApis();
  }

  OnPageChange(event: PageEvent){
    this.apiService.searchCriteria.paging.page = event.pageIndex + 1;
    this.apiService.searchCriteria.paging.pageSize = event.pageSize;
    this.getApis();
  }


  getApis(): void {
    this.apiService.searchApis();
  }

  getFeaturedApis(): void {
    this.apiService.getFeaturedApis();
  }

  // ToDo Change to backend call
  getPlans(): void {
    this.planService.getPlans()
      .subscribe(plans => this.plans = plans);
  }
}
