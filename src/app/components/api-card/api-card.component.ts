import {Component, Input, OnInit} from '@angular/core';
import {ApiSummaryBean} from "../../services/backend/backend.service";

@Component({
  selector: 'app-api-card',
  templateUrl: './api-card.component.html',
  styleUrls: ['./api-card.component.sass']
})
export class ApiCardComponent implements OnInit {
  tmpUrl ='https://github.com/apiman/apiman/raw/master/manager/ui/war/plugins/api-manager/img/about-logo.png';

  constructor() { }

  ngOnInit(): void {
  }
  @Input() api: ApiSummaryBean = {};
}
