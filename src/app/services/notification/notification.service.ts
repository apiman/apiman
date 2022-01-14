import { Injectable } from '@angular/core';
import { BackendService } from '../backend/backend.service';
import {
  ISearchCriteria,
  ISearchResultsNotifications
} from '../../interfaces/ICommunication';
import { KeycloakHelperService } from '../keycloak-helper/keycloak-helper.service';
import { Observable } from 'rxjs';
import { HttpResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  constructor(
    private backendService: BackendService,
    private keycloakHelper: KeycloakHelperService
  ) {}

  public getUnreadNotifications(): Observable<ISearchResultsNotifications> {
    const searchCriteria: ISearchCriteria = {
      filters: [{ name: 'status', operator: 'eq', value: 'OPEN' }]
    } as ISearchCriteria;
    return this.getNotifications(searchCriteria);
  }

  public getReadNotifications(): Observable<ISearchResultsNotifications> {
    const searchCriteria: ISearchCriteria = {
      filters: [{ name: 'status', operator: 'eq', value: 'USER_DISMISSED' }]
    } as ISearchCriteria;
    return this.getNotifications(searchCriteria);
  }

  public getAllNotifications(): Observable<ISearchResultsNotifications> {
    const searchCriteria: ISearchCriteria = {} as ISearchCriteria;
    return this.getNotifications(searchCriteria);
  }

  public getNotifications(
    searchCriteria: ISearchCriteria
  ): Observable<ISearchResultsNotifications> {
    return this.backendService.postNotifications(
      this.keycloakHelper.getUsername(),
      searchCriteria
    );
  }

  public headNotifications(): Observable<HttpResponse<string>> {
    return this.backendService.headNotifications(
      this.keycloakHelper.getUsername()
    );
  }

  public markNotificationAsRead(
    notificationId: number
  ): Observable<HttpResponse<string>> {
    return this.backendService.putNotifications(
      this.keycloakHelper.getUsername(),
      notificationId
    );
  }
}
