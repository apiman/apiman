import { Injectable } from '@angular/core';
import { Plan } from '../../interfaces/plan';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PlanService {

  constructor() { }

  // ToDo Change to backend call
  getPlans(): Observable<Plan[]> {
    return of([{
      id: "1",
      title: "Sandbox",
      subtitle: "Try this Api in a sandbox",
      policies: [{
        title: "Rate Limiting",
        configuration: "500 Requests per day"
      },{
        title: "Quota",
        configuration: "500 MB per Day"
      }]
    }])
  }
}
