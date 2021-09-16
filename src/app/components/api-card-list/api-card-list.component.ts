import { Component, Input, OnInit } from '@angular/core';
import {
  ApiSummaryBean,
  SearchCriteriaBean,
} from '../../services/backend/backend.service';
import { ApiService } from '../../services/api/api.service';
import { PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-api-card-list',
  templateUrl: './api-card-list.component.html',
  styleUrls: ['./api-card-list.component.scss'],
})
export class ApiCardListComponent implements OnInit {
  apis: ApiSummaryBean[] = [];
  totalSize = 0;
  searchCriteria: SearchCriteriaBean = {
    filters: [
      {
        name: 'name',
        value: '*',
        operator: 'like',
      },
    ],
    paging: {
      page: 1,
      pageSize: 8,
    },
  };

  @Input() listType = '';

  constructor(public apiService: ApiService) {}

  ngOnInit(): void {
    this.apis = [];
    if (this.listType === 'api') {
      this.getApis();
    } else if (this.listType === 'featuredApi') {
      this.getFeaturedApis();
    }
  }

  OnInput(event: any) {
    this.searchCriteria.paging.page = 1;
    this.searchCriteria.filters[0].value = '*' + event.target.value + '*';
    this.getApis();
  }

  OnPageChange(event: PageEvent) {
    this.searchCriteria.paging.page = event.pageIndex + 1;
    this.searchCriteria.paging.pageSize = event.pageSize;
    this.getApis();
  }

  getApis(): void {
    this.apiService
      .searchApis(this.searchCriteria)
      .subscribe((searchResult) => {
        this.apis = searchResult.beans;
        this.totalSize = searchResult.totalSize;
      });
  }

  getFeaturedApis(): void {
    this.apiService
      .getFeaturedApis(this.searchCriteria)
      .subscribe((searchResult) => {
        this.apis = searchResult.beans;
      });
  }
}
