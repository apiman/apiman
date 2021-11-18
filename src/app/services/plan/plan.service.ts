import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BackendService } from '../backend/backend.service';
import { IApiPlanSummary } from '../../interfaces/ICommunication';

@Injectable({
  providedIn: 'root'
})
export class PlanService {
  constructor(private backendService: BackendService) {}

  getPlans(
    organizationId: string,
    apiId: string,
    apiVersion: string
  ): Observable<IApiPlanSummary[]> {
    return this.backendService.getApiVersionPlans(
      organizationId,
      apiId,
      apiVersion
    );
  }
}
