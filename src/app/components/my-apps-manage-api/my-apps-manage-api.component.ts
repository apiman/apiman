import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-my-apps-manage-api',
  templateUrl: './my-apps-manage-api.component.html',
  styleUrls: ['./my-apps-manage-api.component.scss'],
})
export class MyAppsManageApiComponent implements OnInit {
  @Input() api: any;
  mockText =
    'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et';

  constructor() {}

  ngOnInit(): void {}
}
