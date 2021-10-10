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
    const notificationCriteria: NotificationCriteriaBean = {
      paging: {
        page: 1,
        pageSize: 10,
      } as PagingBean,
    };

    PageLifecycle.loadPage(
      "Notifications",
      undefined,
      undefined,
      $scope,
      function () {
        PageLifecycle.setPageTitle("notification.dash", "your notifications");
      }
    );

    // Load in the notifications
    NotificationService.getNotificationsForUser(
      Configuration.user.username,
      notificationCriteria
    ).then(
      (searchResult: SearchResultsBean<ApimanNotification<any>>) => {
        // TODO: probably should remove this log stmt for perf reasons
        Logger.info("Notifications: {0}", searchResult);
        $scope.notifications = searchResult;
      },
      (failure) => {
        // TODO: do something useful...
        Logger.error(failure);
      }
    );

    $scope.getNotificationMeta = (
      notification: ApimanNotification<any>
    ): NotificationLayoutMetadata => {
      return NotificationMapperService.mapNotification(notification);
    };

    $scope.goToEventLink = (notification: ApimanNotification<any>): void => {
      const link = NotificationMapperService.mapNotification(notification).link;
      $location.path(link);
      //$scope.$apply();
    };

    $scope.markNotificationRead = (
      notification: ApimanNotification<any>
    ): void => {
      const notificationId: number = notification.id;
      NotificationService.markNotificationRead(Array(notificationId));
    };
  }
]);
