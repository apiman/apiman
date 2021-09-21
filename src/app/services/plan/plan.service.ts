import { Injectable } from '@angular/core';
import { Plan } from '../../interfaces/plan';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PlanService {
  constructor() {}

  // ToDo Change to backend call
  getPlans(): Observable<Plan[]> {
    return of([
      {
        id: '1',
        title: 'Sandbox',
        subtitle: 'Try this Api in a sandbox',
        policies: [
          {
            title: 'Rate Limiting',
            configuration: '500 Requests per day',
          },
          {
            title: 'Quota',
            configuration: '500 MB per day',
          },
        ],
      },
      {
        id: '2',
        title: 'Small Business',
        subtitle: 'For small business just getting started',
        policies: [
          {
            title: 'Rate Limiting',
            configuration: '5000 Requests per day',
          },
          {
            title: 'Quota',
            configuration: '2 GB per day',
          },
        ],
      },
      {
        id: '3',
        title: 'Corporate Plan',
        subtitle: 'For heavy usage business partners',
        policies: [
          {
            title: 'Rate Limiting',
            configuration: '1000 Requests per minute',
          },
          {
            title: 'Quota',
            configuration: '10 GB per hour',
          },
        ],
      },
    ]);
  }
}
