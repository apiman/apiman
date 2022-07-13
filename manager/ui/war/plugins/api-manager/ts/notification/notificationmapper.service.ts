import {
  ApimanNotification, ClientVersionStatusEvent, ContractApprovalEvent,
  ContractCreatedEvent,
} from "../model/notifications.model";
import { _module } from "../apimanPlugin";
import { ApimanGlobals } from "../apimanGlobals";

_module.factory("NotificationMapperService", [
  "$rootScope",
  ($rootScope) => {
    return {
      mapNotification: (
        notification: ApimanNotification<any>
      ): NotificationLayoutMetadata => {
        const result: NotificationLayoutResolver = MAPPER.mapNotification(notification);

        if (result == undefined) {
          return {
            icon: 'fa-alert',
            reason: 'Notification',
            message: notification.reasonMessage,
            link: '' // TODO: use the link in the notification
          }
        } else {
          return {
            icon: result.icon,
            reason: result.reason,
            message: result.messageResolver(notification),
            link: result.linkResolver(notification),
          };
        }
      },
    };
  },
]);

class NotificationMapper {
  constructor() {}

  public mapNotification(
    notification: ApimanNotification<any>
  ): NotificationLayoutResolver {
    return this.reasonMappings.get(notification.reason);
  }

  private reasonMappings: Map<String, NotificationLayoutResolver> = new Map<
    String,
    NotificationLayoutResolver
  >([
    [
      // http://localhost:2772/api-manager/orgs/test/apis/testy/1.0/contracts
      // Key
      "apiman.client.contract.approval.request",
      // Value
      {
        icon: "fa-pencil",
        reason: "Contract approval request",
        messageResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ContractCreatedEvent> = notification;
          const event: ContractCreatedEvent = notificationWithEvent.payload;
          return `A request has been made by ${event.user.username} in org ${event.clientOrgId} to approve access to API ${event.apiId}`
        },
        linkResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ContractCreatedEvent> = notification;
          const event: ContractCreatedEvent = notificationWithEvent.payload;
          return `${ApimanGlobals.pluginName}/orgs/${event.apiOrgId}/apis/${event.apiId}/${event.apiVersion}/contracts`;
        },
      },
    ],
    [
      "apiman.client.contract.approval.granted",
      {
        icon: "fa-check",
        reason: "Your API signup request was approved",
        messageResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ContractApprovalEvent> = notification;
          const event: ContractApprovalEvent = notificationWithEvent.payload;
          return `Your request to sign up to API ${event.apiId} has been ${this.approvalToString(event)}`;
        },
        linkResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ContractApprovalEvent> = notification;
          const event: ContractApprovalEvent = notificationWithEvent.payload;
          return `${ApimanGlobals.pluginName}/orgs/${event.clientOrgId}/clients/${event.clientId}/${event.clientVersion}/contracts`;
        }
      },
    ],
    [
      "apiman.client.contract.request.user",
      {
        icon: "fa-info",
        reason: "Your API signup",
        messageResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ContractApprovalEvent> = notification;
          const event: ContractApprovalEvent = notificationWithEvent.payload;
          return `Your request to sign up to API ${event.apiId} is pending approval`;
        },
        linkResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ContractApprovalEvent> = notification;
          const event: ContractApprovalEvent = notificationWithEvent.payload;
          return `${ApimanGlobals.pluginName}/orgs/${event.clientOrgId}/clients/${event.clientId}/${event.clientVersion}/contracts`;
        }
      },
    ],
    [
      "apiman.client.status_change",
      {
        icon: "fa-info",
        reason: "Client App",
        messageResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ClientVersionStatusEvent> = notification;
          const event: ClientVersionStatusEvent = notificationWithEvent.payload;
          return `A client was published`; // TODO
        },
        linkResolver: (notification: ApimanNotification<any>): string => {
          const notificationWithEvent: ApimanNotification<ClientVersionStatusEvent> = notification;
          const event: ClientVersionStatusEvent = notificationWithEvent.payload;
          return `${ApimanGlobals.pluginName}/orgs/${event.clientOrgId}/clients/${event.clientId}/${event.clientVersion}/contracts`;
        }
      },
    ]
  ]);

  private approvalToString(event: ContractApprovalEvent): string {
    return event.approved ? "approved" : "rejected";
  }
}

const MAPPER = new NotificationMapper();

export interface NotificationLayoutMetadata {
  icon: string;
  reason: string;
  message: string;
  link: string;
}

interface NotificationLayoutResolver {
  icon: string;
  reason: string;
  messageResolver: (notification: ApimanNotification<any>) => string;
  linkResolver: (notification: ApimanNotification<any>) => string;
}
