import {Component, Input, OnInit} from '@angular/core';
import { Api } from '../../interfaces/api'
import { ApiService } from "../../services/api/api.service";

@Component({
  selector: 'app-card-list',
  templateUrl: './card-list.component.html',
  styleUrls: ['./card-list.component.sass']
})
export class CardListComponent implements OnInit {

  apis: Api[] = [];

  @Input() listType = "";

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    if (this.listType === "api") {
      this.getApis();
    } else if (this.listType === "featuredApi") {
      this.getFeaturedApis();
    } else if (this.listType === "plan") {
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
    console.log(this.apis);
  }

  getPlans(): void {
    // To-Do
    // add request for plans (either apiService or planService)
  }
}
