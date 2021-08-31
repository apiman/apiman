import {Component, Input, OnInit} from '@angular/core';
import {Api} from "../../interfaces/api";

@Component({
  selector: 'app-api-card',
  templateUrl: './api-card.component.html',
  styleUrls: ['./api-card.component.sass']
})
export class ApiCardComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }
  @Input() api: Api = {
    id: "",
    title: "",
    shortDescription: "",
    longDescription: "",
    featuredApi: false,
    icon: ""
  };
}
