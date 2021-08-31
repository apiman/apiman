import {Component, Input, OnInit} from '@angular/core';
import {Plan} from "../../interfaces/plan";

@Component({
  selector: 'app-plan-card',
  templateUrl: './plan-card.component.html',
  styleUrls: ['./plan-card.component.sass']
})
export class PlanCardComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }
  @Input() plan: Plan = {
    id: "",
    title: "",
    subtitle: "",
    policies: [{
      title: "",
      configuration: ""
    },{
      title: "",
      configuration: ""
    }]
  }
}

