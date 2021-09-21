import { Component, Input, OnInit } from '@angular/core';
import {IContractExt} from '../../interfaces/IContractExt';

@Component({
  selector: 'app-my-apps-summary',
  templateUrl: './my-apps-summary.component.html',
  styleUrls: ['./my-apps-summary.component.scss'],
})
export class MyAppsSummaryComponent implements OnInit {
  @Input() contract?: IContractExt;

  constructor() {}

  ngOnInit(): void {}
}
