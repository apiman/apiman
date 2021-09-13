import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-marketplace-api-policies',
  templateUrl: './marketplace-api-policies.component.html',
  styleUrls: ['./marketplace-api-policies.component.scss']
})
export class MarketplaceApiPoliciesComponent implements OnInit {

  rateLimitPolicy = {
    limit: '300',
    unit: 'day'
  }

  quotaLimitPolicy = {
    limit: '300',
    unit: 'day'
  }

  constructor() { }

  ngOnInit(): void {
  }

}
