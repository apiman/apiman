import { Component, Input, OnInit } from '@angular/core';
import { Plan } from '../../interfaces/plan';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-plan-card',
  templateUrl: './plan-card.component.html',
  styleUrls: ['./plan-card.component.scss'],
})
export class PlanCardComponent implements OnInit {
  constructor(private route: ActivatedRoute) {}
  orgId = '';
  apiId = '';

  ngOnInit(): void {
    this.orgId = this.route.snapshot.paramMap.get('orgId')!;
    this.apiId = this.route.snapshot.paramMap.get('apiId')!;
  }
  @Input() plan: Plan = {
    id: '',
    title: '',
    subtitle: '',
    policies: [
      {
        title: '',
        configuration: '',
      },
      {
        title: '',
        configuration: '',
      },
    ],
  };
}
