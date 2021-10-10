import {ApimanNotification, ContractCreatedEvent} from "../model/notifications.model";
import { _module } from "../apimanPlugin";
import {ApimanGlobals} from "../apimanGlobals";

_module.factory("NotificationMapperService", [
  "$rootScope",
  ($rootScope) => {
    return {
      mapNotification: (notification: ApimanNotification<any>): NotificationLayoutMetadata => {
        const result = MAPPER.mapNotification(notification);
        return {
          icon: result.icon,
          reason: result.reason,
          link: result.linkResolver(notification)
        };
      },
    };
  },
]);

class NotificationMapper {

  constructor() {
  }

  public mapNotification(notification: ApimanNotification<any>): NotificationLayoutResolver {
    return this.reasonMappings.get(notification.reason);
  }

  private reasonMappings: Map<String, NotificationLayoutResolver> = new Map<String, NotificationLayoutResolver>([
    [ // http://localhost:2772/api-manager/orgs/test/apis/testy/1.0/contracts
      // Key
      "apiman.client.contract.approval.request",
      // Value
      {
        icon: "fa-pencil",
        reason: "Contract approval request",
        linkResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ContractCreatedEvent> = notification;
          const event: ContractCreatedEvent = notificationWithEvent.payload;
          return `${ApimanGlobals.pluginName}/orgs/${event.apiOrgId}/apis/${event.apiId}/${event.apiVersion}/contracts`;
        },
      },
    ],
  ]);
}

const MAPPER = new NotificationMapper();

export interface NotificationLayoutMetadata {
  icon: string;
  reason: string;
  link: string;
}

interface NotificationLayoutResolver {
  icon: string;
  reason: string;
  linkResolver: (notification: ApimanNotification<any>) => string;
}