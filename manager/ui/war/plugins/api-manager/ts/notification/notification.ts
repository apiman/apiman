import { _module } from "../apimanPlugin";
import { PagingBean, SearchResultsBean } from "../rpc";
import {
  ApimanNotification,
  NotificationCriteriaBean,
} from "../model/notifications.model";
import { NotificationLayoutMetadata } from "./notificationmapper.service";

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
    $scope.notifications = null;
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
        paging: {
          page: pageIdx,
          pageSize: pageSize,
        } as PagingBean,
      };

      NotificationService.getNotificationsForUser(username, notificationCriteria).then(
          (searchResult: SearchResultsBean<ApimanNotification<any>>) => {
            Logger.info("Notifications: {0}", searchResult);
            $scope.notifications = searchResult;
          },
          (failure) => {
            Logger.error(failure);
          }
      )
    };

    // Load notifs
    $scope.getNotificationsWithPagination(1, $scope.pageSize);

    // // Load in the notifications
    // NotificationService.getNotificationsForUser(
    //   Configuration.user.username,
    //   notificationCriteria
    // ).then(
    //   (searchResult: SearchResultsBean<ApimanNotification<any>>) => {
    //     // TODO: probably should remove this log stmt for perf reasons
    //     Logger.info("Notifications: {0}", searchResult);
    //     $scope.notifications = searchResult;
    //   },
    //   (failure) => {
    //     // TODO: do something useful...
    //     Logger.error(failure);
    //   }
    // );

    $scope.getNotificationMeta = function (notification: ApimanNotification<any>): NotificationLayoutMetadata {
      return NotificationMapperService.mapNotification(notification);
    };

    $scope.goToEventLink = function (notification: ApimanNotification<any>): void {
      const link = NotificationMapperService.mapNotification(notification).link;
      $location.path(link);
      //$scope.$apply();
    };

    $scope.markRead = function (notification: ApimanNotification<any>): void {
      const notificationIds: number[] = [ notification.id ];
      Logger.info("Notification to mark read: {0}", notification);
      NotificationService.markNotificationRead(notificationIds).then(
          succeeded => {
            $scope.notifications.forEach((notification: ApimanNotification<any>, index) => {
              if (notification.id === notification.id) {
                this.documents.splice(index, 1);
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
