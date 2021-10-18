import { Component, Input, OnInit } from '@angular/core';
import {IApiSummaryExt} from "../../interfaces/IApiSummaryExt";
import {ConfigService} from "../../services/config/config.service";

@Component({
  selector: 'app-api-card',
  templateUrl: './api-card.component.html',
  styleUrls: ['./api-card.component.scss'],
})
export class ApiCardComponent implements OnInit {
  @Input() api!: IApiSummaryExt;

  constructor(public configService: ConfigService) {}

  ngOnInit(): void {}
}
