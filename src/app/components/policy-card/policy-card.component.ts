import { Component, Input, OnInit } from '@angular/core';
import {IPolicyExt} from "../../interfaces/IPolicyExt";

@Component({
  selector: 'app-policy-card',
  templateUrl: './policy-card.component.html',
  styleUrls: ['./policy-card.component.scss'],
})
export class PolicyCardComponent {
  @Input() policy!: IPolicyExt;

  constructor() {}
}
