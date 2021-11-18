import { Component, Input } from '@angular/core';
import { IPolicyExt } from '../../interfaces/IPolicy';

@Component({
  selector: 'app-policy-card',
  templateUrl: './policy-card.component.html',
  styleUrls: ['./policy-card.component.scss']
})
export class PolicyCardComponent {
  @Input() policy!: IPolicyExt;

  constructor() {}
}
