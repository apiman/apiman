import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-my-apps-policies',
  templateUrl: './my-apps-policies.component.html',
  styleUrls: ['./my-apps-policies.component.scss'],
})
export class MyAppsPoliciesComponent implements OnInit {
  @Input() api: any;

  constructor() {}

  ngOnInit(): void {}
}
