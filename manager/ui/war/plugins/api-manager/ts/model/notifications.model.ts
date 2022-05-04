import {OrderByBean, PagingBean, SearchCriteriaFilterBean} from "../rpc";

/**
 *  Notifications & events model
 */
export interface NotificationCriteriaBean {
  /**
   * Allowed values = {
   *    "id",
   *    "category",
   *    "reason",
   *    "reasonMessage",
   *    "status",
   *    "createdOn",
   *    "modifiedOn",
   *    "source"
   * }
   */
  filters?: SearchCriteriaFilterBean[];
  orderBy?: OrderByBean;
  paging?: PagingBean;
}

export interface ApimanEventHeaders {
  id: string;
  source: string
  type: string;
  subject: string;
  time: Date;
  eventVersion: number;
  otherProperties: Map<string, any>;
}

export interface IVersionedApimanEvent {
  headers: ApimanEventHeaders;
}

export enum NotificationCategory {
  USER_ADMINISTRATION = 'USER_ADMINISTRATION',
  API_ADMINISTRATION = 'API_ADMINISTRATION',
  API_LIFECYCLE = 'API_LIFECYCLE',
  OTHER = 'OTHER'
}

export enum NotificationStatus {
  OPEN = 'OPEN',
  USER_DISMISSED = 'USER_DISMISSED',
  SYSTEM_DISMISSED = 'SYSTEM_DISMISSED'
}

export enum ClientStatus {
  Created = "Created",
  AwaitingApproval = "AwaitingApproval",
  Ready = "Ready",
  Registered = "Registered",
  Retired = "Retired"
}

export interface ApimanNotification<P extends IVersionedApimanEvent> {
  id: number;
  category: NotificationCategory;
  reason: string;
  reasonMessage: string;
  status: NotificationStatus;
  createdOn: Date;
  modifiedOn: Date;
  source: string;
  payload: P;
}

interface UserDto {
  id: string;
  username: string;
  fullName: string;
  email: string;
}

export interface ContractCreatedEvent extends IVersionedApimanEvent {
  headers: ApimanEventHeaders;
  user: UserDto;
  clientOrgId: string;
  clientId: string;
  clientVersion: string;
  apiOrgId: string;
  apiId: string;
  apiVersion: string;
  contractId: string;
  planId: string;
  planVersion: string;
  approvalRequired: boolean;
}

export interface ContractApprovalEvent extends IVersionedApimanEvent {
  headers: ApimanEventHeaders;
  approver: UserDto;
  clientOrgId: string;
  clientId: string;
  clientVersion: string;
  apiOrgId: string;
  apiId: string;
  apiVersion: string;
  contractId: string;
  planId: string;
  planVersion: string;
  approved: boolean;
  rejectionReason: string;
}

export interface ClientVersionStatusEvent extends IVersionedApimanEvent {
  headers: ApimanEventHeaders;
  clientOrgId: string;
  clientId: string;
  clientVersion: string;
  previousStatus: ClientStatus;
  newStatus: ClientStatus;
}

export interface NotificationAction {
  markAll?: boolean,
  notificationIds: number[],
  status: NotificationStatus
}


