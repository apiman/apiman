import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-my-apps-summary',
  templateUrl: './my-apps-summary.component.html',
  styleUrls: ['./my-apps-summary.component.scss'],
})
export class MyAppsSummaryComponent implements OnInit {
  @Input() api: any;

  constructor() {}

  ngOnInit(): void {}
}
