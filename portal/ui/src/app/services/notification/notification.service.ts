/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

import { Injectable } from '@angular/core';
import { BackendService } from '../backend/backend.service';
import {
  ISearchCriteria,
  ISearchResultsNotifications
} from '../../interfaces/ICommunication';
import { Observable } from 'rxjs';
import { HttpResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  constructor(private backendService: BackendService) {}

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
    return this.backendService.postNotifications(searchCriteria);
  }

  public headNotifications(): Observable<HttpResponse<string>> {
    return this.backendService.headNotifications();
  }

  public markNotificationAsRead(
    notificationId: number
  ): Observable<HttpResponse<string>> {
    return this.backendService.putNotifications(notificationId);
  }
}
