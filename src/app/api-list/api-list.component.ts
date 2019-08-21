import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-api-list',
  templateUrl: './api-list.component.html',
  styleUrls: ['./api-list.component.scss']
})
export class ApiListComponent implements OnInit {

  apiData: Array<object> = [{
    name: 'Petstore',
    endpoint: 'https://petstore.swagger.io/v2/',
    public: true
  },
  {
    name: 'Analytic API',
    endpoint: 'http://pc0854.scheer.systems:3040/analytics/',
    public: false
  }];

  displayedColumns: string[] = ['public', 'name', 'endpoint'];

  constructor() { }

  ngOnInit() {
  }

}
