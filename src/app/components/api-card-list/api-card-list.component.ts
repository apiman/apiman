import { Component, OnInit } from '@angular/core';
import { Api } from '../../interfaces/api'
import { ApiService } from "../../services/api/api.service";

@Component({
  selector: 'app-api-card-list',
  templateUrl: './api-card-list.component.html',
  styleUrls: ['./api-card-list.component.sass']
})
export class ApiCardListComponent implements OnInit {

  apis: Api[] = [];

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.getApis();
  }

  getApis(): void {
    this.apiService.getApis()
      .subscribe(apis => this.apis = apis);
  }
}
