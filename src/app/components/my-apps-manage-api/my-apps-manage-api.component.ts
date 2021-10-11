import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-my-apps-manage-api',
  templateUrl: './my-apps-manage-api.component.html',
  styleUrls: ['./my-apps-manage-api.component.scss'],
})
export class MyAppsManageApiComponent implements OnInit {
  @Input() api: any;

  constructor() {}

  ngOnInit(): void {}
}
