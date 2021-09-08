import {Component, Input, OnInit} from '@angular/core';
import {ApiSummaryBean} from "../../services/backend/backend.service";
import {ApiService} from "../../services/api/api.service";
import {PageEvent} from "@angular/material/paginator";

@Component({
  selector: 'app-api-card-list',
  templateUrl: './api-card-list.component.html',
  styleUrls: ['./api-card-list.component.sass']
})
export class ApiCardListComponent implements OnInit {

  apis: ApiSummaryBean[] = [];

  @Input() listType = "";
  pageEvent: void;
  inputEvent: void;

  constructor(public apiService: ApiService) { }

  ngOnInit(): void {
    if (this.listType === "api") {
      this.getApis();
    } else if (this.listType === "featuredApi") {
      this.getFeaturedApis();
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
}
