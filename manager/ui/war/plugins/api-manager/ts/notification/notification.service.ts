import { _module } from "../apimanPlugin";
import {
  ApimanNotification,
  NotificationAction,
  NotificationCriteriaBean,
  NotificationStatus,
} from "../model/notifications.model";
import { SearchResultsBean } from "../rpc";

_module.service("NotificationService", [
  "$http",
  "Configuration",
  "$q",
  function ($http, Configuration, $q) {
    return {
      markNotificationRead: function (notificationIds: number[]): Promise<any> {
        const userId: string = Configuration.user.username;
        const notificationAction: NotificationAction = {
          notificationIds: notificationIds,
          status: NotificationStatus.USER_DISMISSED,
        };

        return $http({
          method: "PUT",
          url: `${Configuration.api.endpoint}/users/${userId}/notifications`,
          data: notificationAction,
        });
      },

      getNotificationCount: (userId: string): Promise<number> => {
        return $http({
          method: "HEAD",
          url: `${Configuration.api.endpoint}/users/${userId}/notifications`,
        }).then(
          (success) => $q.resolve(success.headers("Total-Count")),
          (failure) => $q.reject(failure)
        );
      },

      getNotificationsForUser: (
        userId: string,
        criteria: NotificationCriteriaBean
      ): Promise<SearchResultsBean<ApimanNotification<any>>> => {
        return $http({
          method: "POST",
          url: `${Configuration.api.endpoint}/users/${userId}/notifications`,
          data: criteria,
        }).then(
          (success) => $q.resolve(success.data),
          (failure) => $q.reject(failure)
        );
      },
    };
  },
]);