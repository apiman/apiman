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

import { Component, OnInit } from '@angular/core';
import {
  INotificationsDto,
  ISearchCriteria,
  ISearchResultsNotifications
} from '../../interfaces/ICommunication';
import { MatTableDataSource } from '@angular/material/table';
import { TranslateService } from '@ngx-translate/core';
import { NotificationService } from '../../services/notification/notification.service';
import { Observable } from 'rxjs';
import { HeroService } from '../../services/hero/hero.service';
import { SpinnerService } from '../../services/spinner/spinner.service';
import { Router } from '@angular/router';
import { ConfigService } from '../../services/config/config.service';
import { DateTime } from 'luxon';
import {
  notificationsCategory,
  notificationsSection
} from '../../models/notifications-enum';
import { SnackbarService } from '../../services/snackbar/snackbar.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit {
  notifications: INotificationsDto[];
  searchCriteria: ISearchCriteria;
  dataSource: MatTableDataSource<INotificationsDto>;
  displayedColumns: string[] = ['category', 'message', 'date', 'action'];
  sections = notificationsSection;
  activeSection = this.sections.unread;

  constructor(
    private heroService: HeroService,
    private translator: TranslateService,
    private router: Router,
    private notificationService: NotificationService,
    public loadingSpinnerService: SpinnerService,
    private snackbarService: SnackbarService,
    private configService: ConfigService
  ) {
    this.dataSource = {} as MatTableDataSource<INotificationsDto>;
    this.notifications = [] as INotificationsDto[];
    this.searchCriteria = {
      filters: [{ name: 'status', operator: 'eq', value: 'OPEN' }]
    } as ISearchCriteria;
  }

  ngOnInit(): void {
    this.heroService.setUpHero({
      title: this.translator.instant('NOTIFICATION.TITLE') as string,
      subtitle: ''
    });
    this.fetchNotifications(this.activeSection);
  }

  public mapNotificationCategory(category: string): string {
    if (
      category === notificationsCategory.apiAdministration ||
      category === notificationsCategory.userAdministration
    ) {
      return 'Administration';
    } else if (category === notificationsCategory.apiLifecycle) {
      return 'API Information';
    } else {
      return 'Other';
    }
  }

  public mapNotificationMessage(notification: INotificationsDto): string {
    let reason: string = notification.reason;
    const reasonMap: Map<string, string> = new Map<string, string>([
      [
        'apiman.client.contract.approval.granted',
        'NOTIFICATION.MESSAGE.API.SIGN_UP_APPROVED'
      ],
      [
        'apiman.client.contract.approval.rejected',
        'NOTIFICATION.MESSAGE.API.SIGN_UP_REJECTED'
      ],
      [
        'apiman.client.status_change.registered',
        'NOTIFICATION.MESSAGE.CLIENT.REGISTERED'
      ],
      [
        'apiman.client.contract.request.user',
        'NOTIFICATION.MESSAGE.API.SIGN_UP_REQUESTED'
      ],
      ['apiman.api.status_change', 'NOTIFICATION.MESSAGE.API.RETIRED']
    ]);

    if (reason === 'apiman.client.status_change') {
      if (
        notification.payload.newStatus &&
        notification.payload.newStatus === 'Registered'
      ) {
        reason = `${reason}.registered`;
      }
    }

    const i18nKey: string | undefined = reasonMap.get(reason);

    if (i18nKey) {
      return this.translator.instant(i18nKey, notification) as string;
    } else {
      return notification.reasonMessage;
    }
  }

  fetchNotifications(section: string): void {
    let notifications: Observable<ISearchResultsNotifications>;

    this.loadingSpinnerService.startWaiting();
    if (section === 'Unread') {
      notifications = this.notificationService.getUnreadNotifications();
    } else if (section === 'Read') {
      notifications = this.notificationService.getReadNotifications();
    } else {
      notifications = this.notificationService.getAllNotifications();
    }
    notifications.subscribe((searchResult: ISearchResultsNotifications) => {
      this.notifications = searchResult.beans;
      this.dataSource = new MatTableDataSource(this.notifications);
      this.loadingSpinnerService.stopWaiting();
    });
  }

  public markNotificationAsRead(notificationId: number): void {
    this.notificationService
      .markNotificationAsRead(notificationId)
      .subscribe((response) => {
        this.fetchNotifications(this.activeSection);
        this.heroService.updateNotificationCount(true);
        if (response.status !== 204) {
          this.snackbarService.showErrorSnackBar(
            `Error ${response.status}: ${response.statusText}`
          );
        }
      });
  }

  public redirect(notification: INotificationsDto): void {
    switch (notification.reason) {
      case 'apiman.client.contract.approval.request':
        this.redirectAdminApprovalRequest(notification);
        break;
      default:
        this.redirectToMyClientsPage(notification);
        break;
    }
  }

  private redirectToMyClientsPage(notification: INotificationsDto): void {
    let routerFragment = '';

    if (notification.payload.clientOrgId) {
      routerFragment = `${notification.payload.clientOrgId}`;
    }
    if (
      routerFragment &&
      notification.payload.clientId &&
      notification.payload.clientVersion
    ) {
      routerFragment += `-${notification.payload.clientId}-${notification.payload.clientVersion}`;
    }
    if (
      routerFragment &&
      notification.payload.apiId &&
      notification.payload.apiVersion
    ) {
      routerFragment += `-${notification.payload.apiId}-${notification.payload.apiVersion}`;
    }
    void this.router.navigate(['applications'], {
      fragment: routerFragment
    });
  }

  private redirectAdminApprovalRequest(notification: INotificationsDto): void {
    if (
      notification.payload &&
      notification.payload.apiOrgId &&
      notification.payload.apiId &&
      notification.payload.apiVersion
    ) {
      // TODO (fvolk): get endpoint from Manger REST API or from the notification
      // prettier-ignore
      const url = `${this.configService.getManagerUiEndpoint()}/orgs/${notification.payload.apiOrgId}/apis/${notification.payload.apiId}/${notification.payload.apiVersion}/contracts`;
      window.open(url, '_blank');
    }
  }

  public convertTimestamp(timestamp: string): string {
    return DateTime.fromISO(timestamp).toRelative({
      locale: this.translator.currentLang
    }) as string;
  }

  public convertTooltipTimestamp(timestamp: string): string {
    // format F => 8/6/2014, 1:07:04 PM
    return DateTime.fromISO(timestamp).toFormat('F', {
      locale: this.translator.currentLang
    });
  }

  public setActiveSection(section: notificationsSection): void {
    this.activeSection = section;
    if (section === notificationsSection.read) {
      this.displayedColumns = ['category', 'message', 'date'];
    } else {
      this.displayedColumns = ['category', 'message', 'date', 'action'];
    }
    this.fetchNotifications(this.activeSection);
  }
}
