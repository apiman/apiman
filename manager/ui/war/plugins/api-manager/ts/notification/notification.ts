import { _module } from "../apimanPlugin";
import { PagingBean, SearchResultsBean } from "../rpc";
import {
  ApimanNotification,
  NotificationCriteriaBean,
} from "../model/notifications.model";
import { NotificationLayoutMetadata } from "./notificationmapper.service";
import * as dayjs from 'dayjs';

_module.controller("Apiman.NotificationController", [
  "$location",
  "$scope",
  "$rootScope",
  "Logger",
  "Configuration",
  "NotificationService",
  "NotificationMapperService",
  "PageLifecycle",
  function (
    $location,
    $scope,
    $rootScope,
    Logger,
    Configuration,
    NotificationService,
    NotificationMapperService,
    PageLifecycle
  ) {
    $rootScope.pageState = "loaded";
    $scope.notificationSearchResult = null;
    $scope.pageSize = 15;

    PageLifecycle.loadPage(
      "Notifications",
      undefined,
      undefined,
      $scope,
      function () {
        PageLifecycle.setPageTitle("notification.dash", "your notifications");
      }
    );

    $scope.getNotificationsWithPagination = (pageIdx: number, pageSize: number): void => {
      const username: string = Configuration.user.username;
      const notificationCriteria: NotificationCriteriaBean = {
        filters: [
          {
            name: 'status',
            operator: 'eq',
            value: 'OPEN',
          }
        ],
        paging: {
          page: pageIdx,
          pageSize: pageSize,
        } as PagingBean,
      };

      NotificationService.getNotificationsForUser(username, notificationCriteria).then(
          (searchResult: SearchResultsBean<ApimanNotification<any>>) => {
            Logger.info("Notifications: {0}", searchResult);
            $scope.notificationSearchResult = searchResult;
          },
          (failure) => {
            Logger.error(failure);
          }
      )
    };

    // Load notifications when we start the page.
    $scope.getNotificationsWithPagination(1, $scope.pageSize);

    $scope.getNotificationMeta = function (notification: ApimanNotification<any>): NotificationLayoutMetadata {
      return NotificationMapperService.mapNotification(notification);
    };

    $scope.goToEventLink = function (notification: ApimanNotification<any>): void {
      const link = NotificationMapperService.mapNotification(notification).link;
      $location.path(link);
      //$scope.$apply();
    };

    $scope.humanRelativeDate = function (humanRelativeDate): string {
      // @ts-ignore
      return dayjs().to(dayjs(humanRelativeDate));
    }

    $scope.deleteNotification = (notification: ApimanNotification<any>): void => {
      const notificationIds: number[] = [ notification.id ];
      Logger.info("Notification to mark read: {0}", notification);
      NotificationService.deleteNotification(notificationIds).then(
          succeeded => {
            // Delete the notification out of the results array. For some reason this is annoyingly verbose in JS.
            let notificationArray = $scope.notificationSearchResult.beans;
            notificationArray.forEach((candidateNotification: ApimanNotification<any>, index) => {
              if (notification.id === candidateNotification.id) {
                notificationArray.splice(index, 1);
              }
            });
          },
          failed => {
            // TODO: handle gracefully
          }
      );
    };

    $scope.markRead = function (notification: ApimanNotification<any>): void {
      const notificationIds: number[] = [ notification.id ];
      Logger.info("Notification to mark read: {0}", notification);
      NotificationService.markNotificationRead(notificationIds).then(
          succeeded => {
            // Delete the notification out of the results array. For some reason this is annoyingly verbose in JS.
            let notificationArray = $scope.notificationSearchResult.beans;
            notificationArray.forEach((candidateNotification: ApimanNotification<any>, index) => {
              if (notification.id === candidateNotification.id) {
                notificationArray.splice(index, 1);
              }
            });
          },
          failed => {
            // TODO: handle gracefully
          }
      );
    };
  }
]);
