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
  displayedColumns: string[] = ['Category', 'Message', 'Date', 'Action'];
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
        'NOTIFICATION.MESSAGE.APP.REGISTERED'
      ]
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

  public redirectToClient(notification: INotificationsDto): void {
    let routerFragment = '';

    if (notification.payload.clientId && notification.payload.clientVersion) {
      routerFragment = `${notification.payload.clientId}-${notification.payload.clientVersion}`;
    }
    if (
      routerFragment &&
      notification.payload.apiId &&
      notification.payload.apiVersion
    ) {
      routerFragment =
        routerFragment +
        `-${notification.payload.apiId}-${notification.payload.apiVersion}`;
    }
    void this.router.navigate(['applications'], {
      fragment: routerFragment
    });
  }

  public convertTimestamp(timestamp: string): string {
    return DateTime.fromISO(timestamp).toRelative({
      locale: this.configService.getLanguage()
    }) as string;
  }

  public setActiveSection(section: notificationsSection): void {
    this.activeSection = section;
    if (section === notificationsSection.read) {
      this.displayedColumns = ['Category', 'Message', 'Date'];
    } else {
      this.displayedColumns = ['Category', 'Message', 'Date', 'Action'];
    }
    this.fetchNotifications(this.activeSection);
  }
}
