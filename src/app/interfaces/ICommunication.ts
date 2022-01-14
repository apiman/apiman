/*
 * Copyright 2021 Scheer PAS Schweiz AG
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

import { IPolicyExt } from './IPolicy';

export interface IAction {
  type:
    | 'publishAPI'
    | 'retireAPI'
    | 'registerClient'
    | 'unregisterClient'
    | 'lockPlan';
  organizationId: string;
  entityId: string;
  entityVersion: string;
}

export interface IDeveloper {
  id: string;
  clients: IDeveloperMapping[];
}

export interface IDeveloperMapping {
  clientId: string;
  organizationId: string;
}

export interface IApi {
  organization: IOrganization;
  id: string;
  name: string;
  description: string;
  createdBy: string;
  image?: string;

  /** @format date-time */
  createdOn: string;

  /** @format int32 */
  numPublished: number;
}

export interface IApiGateway {
  gatewayId: string;
}

export interface IApiPlan {
  planId: string;
  version: string;
}

export interface IApiVersion {
  /** @format int64 */
  id: number;
  api: IApi;
  status: 'Created' | 'Ready' | 'Published' | 'Retired';
  endpoint: string;
  endpointType: 'rest' | 'soap' | 'ui';
  endpointContentType: 'json' | 'xml';
  endpointProperties: Record<string, string>;
  gateways: IApiGateway[];
  publicAPI: boolean;
  plans: IApiPlan[];
  version: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
  modifiedBy: string;

  /** @format date-time */
  modifiedOn: string;

  /** @format date-time */
  publishedOn: string;

  /** @format date-time */
  retiredOn: string;
  definitionType:
    | 'None'
    | 'SwaggerJSON'
    | 'SwaggerYAML'
    | 'WSDL'
    | 'WADL'
    | 'RAML'
    | 'External';
  parsePayload: boolean;
  disableKeysStrip: boolean;
  definitionUrl: string;
  extendedDescription: string;
  exposeInPortal: boolean;
}

export interface IOrganization {
  id: string;
  name: string;
  description: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
  modifiedBy: string;

  /** @format date-time */
  modifiedOn: string;
}

export interface IContractSummary {
  /** @format int64 */
  contractId: number;
  clientOrganizationId: string;
  clientOrganizationName: string;
  clientId: string;
  clientName: string;
  clientVersion: string;
  apiOrganizationId: string;
  apiOrganizationName: string;
  apiId: string;
  apiName: string;
  apiVersion: string;
  apiDescription: string;
  planName: string;
  planId: string;
  planVersion: string;

  /** @format date-time */
  createdOn: string;
}

export interface IUpdateDeveloper {
  clients: IDeveloperMapping[];
}

export interface IClientVersionSummary {
  organizationId: string;
  organizationName: string;
  id: string;
  name: string;
  description: string;
  status: 'Created' | 'Ready' | 'Registered' | 'Retired';
  version: string;
  apiKey: string;
}

export interface IGatewayEndpointSummary {
  endpoint: string;
}

export interface IGateway {
  id: string;
  name: string;
  description: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
  modifiedBy: string;

  /** @format date-time */
  modifiedOn: string;
  type: 'REST';
  configuration: string;
}

export interface IUpdateGateway {
  description: string;
  type: 'REST';
  configuration: string;
}

export interface IGatewaySummary {
  id: string;
  name: string;
  description: string;
  type: 'REST';
}

export interface IGatewayTestResult {
  success: boolean;
  detail: string;
}

export interface INewGateway {
  name: string;
  description: string;
  type: 'REST';
  configuration: string;
}

export interface IApiPlanSummary {
  planId: string;
  planName: string;
  planDescription: string;
  version: string;
  planPolicies: IPolicyExt[];
}

export interface IAuditEntry {
  /** @format int64 */
  id: number;
  who: string;
  organizationId: string;
  entityType: 'Organization' | 'Client' | 'Plan' | 'Api';
  entityId: string;
  entityVersion: string;

  /** @format date-time */
  createdOn: string;
  what:
    | 'Create'
    | 'Update'
    | 'Delete'
    | 'Clone'
    | 'Grant'
    | 'Revoke'
    | 'Publish'
    | 'Retire'
    | 'Register'
    | 'Unregister'
    | 'AddPolicy'
    | 'RemovePolicy'
    | 'UpdatePolicy'
    | 'ReorderPolicies'
    | 'CreateContract'
    | 'BreakContract'
    | 'Lock'
    | 'UpdateDefinition'
    | 'DeleteDefinition';
  data: string;
}

export interface ISearchResults {
  /*Don't use `object` as a type. The `object` type is currently hard to use ([see this issue](https://github.com/microsoft/TypeScript/issues/21732)).
  Consider using `Record<string, unknown>` instead, as it allows you to more easily inspect and use the keys  @typescript-eslint/ban-types*/
  beans: Record<string, unknown>;

  /** @format int32 */
  totalSize: number;
}

export interface ISearchResultsAuditEntry {
  beans: IAuditEntry[];

  /** @format int32 */
  totalSize: number;
}

export interface IClientSummary {
  organizationId: string;
  organizationName: string;
  id: string;
  name: string;
  description: string;

  /** @format int32 */
  numContracts: number;
}

export interface IApiKey {
  apiKey: string;
}

export interface IPolicy {
  /** @format int64 */
  id: number;
  type: 'Client' | 'Plan' | 'Api';
  organizationId: string;
  entityId: string;
  entityVersion: string;
  name: string;
  description: string;
  configuration: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string | number;
  modifiedBy: string;

  /** @format date-time */
  modifiedOn: string | number;
  definition: IPolicyDefinition;

  /** @format int32 */
  orderIndex: number;
}

export interface IPolicyDefinition {
  id: string;
  policyImpl: string;
  name: string;
  description: string;
  icon: string;
  templates: IPolicyDefinitionTemplate[];

  /** @format int64 */
  pluginId?: number;
  formType: 'Default' | 'JsonSchema';
  form?: string;
  deleted: boolean;
}

export interface IPolicyDefinitionTemplate {
  language: string | null;
  template: string;
}

export interface INewPolicy {
  definitionId: string;
  configuration: string;
}

export interface IUpdatePolicy {
  configuration: string;
}

export interface IPolicySummary {
  policyDefinitionId: string;

  /** @format int64 */
  id: number;
  name: string;
  description: string;
  icon: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
}

export interface IPolicyChain {
  policies: IPolicySummary[];
}

export interface IApiSummary {
  organizationId: string;
  organizationName: string;
  id: string;
  name: string;
  description: string;
  image: string;

  /** @format date-time */
  createdOn: string;
}

export interface IApiVersionSummary {
  organizationId: string;
  organizationName: string;
  id: string;
  name: string;
  description: string;
  status: 'Created' | 'Ready' | 'Published' | 'Retired';
  version: string;
  publicAPI: boolean;
}

export interface IApiVersionStatus {
  status: 'Created' | 'Ready' | 'Published' | 'Retired';
  items: IStatusItem[];
}

export interface IStatusItem {
  id: string;
  name: string;
  done: boolean;
  optional: boolean;
  remediation: string;
}

export interface IApiVersionEndpointSummary {
  managedEndpoint: string;
}

export interface INewApiDefinition {
  definitionUrl: string;
  definitionType:
    | 'None'
    | 'SwaggerJSON'
    | 'SwaggerYAML'
    | 'WSDL'
    | 'WADL'
    | 'RAML'
    | 'External';
}

export interface IPlanSummary {
  organizationId: string;
  organizationName: string;
  id: string;
  name: string;
  description: string;
}

export interface IPlanVersionSummary {
  organizationId: string;
  organizationName: string;
  id: string;
  name: string;
  description: string;
  status: 'Created' | 'Ready' | 'Locked';
  version: string;
}

export interface IGrantRoles {
  userId: string;
  roleIds: string[];
}

export interface IMember {
  userId: string;
  userName: string;
  email: string;

  /** @format date-time */
  joinedOn: string;
  roles: IMemberRole[];
}

export interface IMemberRole {
  roleId: string;
  roleName: string;
}

export interface IUsagePerClient {
  data: Record<string, number>;
}

export interface IUsagePerPlan {
  data: Record<string, number>;
}

export interface IResponseStatsDataPoint {
  label: string;

  /** @format int64 */
  total: number;

  /** @format int64 */
  failures: number;

  /** @format int64 */
  errors: number;
}

export interface IResponseStatsHistogram {
  data: IResponseStatsDataPoint[];
}

export interface IResponseStatsSummary {
  /** @format int64 */
  total: number;

  /** @format int64 */
  failures: number;

  /** @format int64 */
  errors: number;
}

export interface IResponseStatsPerClient {
  data: Record<string, IResponseStatsDataPoint>;
}

export interface IResponseStatsPerPlan {
  data: Record<string, IResponseStatsDataPoint>;
}

export interface IClientUsagePerApi {
  data: Record<string, number>;
}

export interface IUpdateClient {
  description: string;
}

export interface IUpdateApi {
  description: string;
}

export interface IUpdateApiVersion {
  endpoint: string;
  endpointType: 'rest' | 'soap' | 'ui';
  endpointContentType: 'json' | 'xml';
  endpointProperties: Record<string, string>;
  gateways: IApiGateway[];
  parsePayload: boolean;
  publicAPI: boolean;
  disableKeysStrip: boolean;
  plans: IApiPlan[];
}

export interface IUpdatePlan {
  description: string;
}

export interface IClient {
  organization: IOrganization;
  id: string;
  name: string;
  description: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
}

export interface IClientVersion {
  /** @format int64 */
  id: number;
  client: IClient;
  status:
    | 'Created'
    | 'Ready'
    | 'Registered'
    | 'Retired'
    | 'AwaitingApproval'
    | 'Unregistered';
  version: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
  modifiedBy: string;

  /** @format date-time */
  modifiedOn: string;

  /** @format date-time */
  publishedOn: string;

  /** @format date-time */
  retiredOn: string;
  apikey: string;
}

export interface IContract {
  /** @format int64 */
  id: number;
  client: IClientVersion;
  api: IApiVersion;
  plan: IPlanVersion;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
}

export interface IPlan {
  organization: IOrganization;
  id: string;
  name: string;
  description: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
}

export interface IPlanVersion {
  /** @format int64 */
  id: number;
  plan: IPlan;
  status: 'Created' | 'Ready' | 'Locked';
  version: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
  modifiedBy: string;

  /** @format date-time */
  modifiedOn: string;

  /** @format date-time */
  lockedOn: string;
}

export interface IUsageDataPoint {
  label: string;

  /** @format int64 */
  count: number;
}

export interface IUsageHistogram {
  data: IUsageDataPoint[];
}

export interface IUpdateOrganization {
  description: string;
}

export interface INewOrganization {
  name: string;
  description: string;
}

export interface INewPlan {
  name: string;
  description: string;
  initialVersion: string;
}

export interface INewPlanVersion {
  version: string;
  clone: boolean;
  cloneVersion: string;
}

export interface INewApi {
  name: string;
  description: string;
  initialVersion: string;
  endpoint: string;
  endpointType: 'rest' | 'soap' | 'ui';
  endpointContentType: 'json' | 'xml';
  publicAPI: boolean;
  parsePayload: boolean;
  disableKeysStrip: boolean;
  plans: IApiPlan[];
  definitionUrl: string;
  definitionType:
    | 'None'
    | 'SwaggerJSON'
    | 'SwaggerYAML'
    | 'WSDL'
    | 'WADL'
    | 'RAML'
    | 'External';
}

export interface INewApiVersion {
  version: string;
  clone: boolean;
  cloneVersion: string;
  endpoint: string;
  endpointType: 'rest' | 'soap' | 'ui';
  endpointContentType: 'json' | 'xml';
  publicAPI: boolean;
  parsePayload: boolean;
  disableKeysStrip: boolean;
  plans: IApiPlan[];
  definitionUrl: string;
  definitionType:
    | 'None'
    | 'SwaggerJSON'
    | 'SwaggerYAML'
    | 'WSDL'
    | 'WADL'
    | 'RAML'
    | 'External';
}

export interface INewClient {
  name: string;
  description: string;
  initialVersion: string;
}

export interface INewClientVersion {
  version: string;
  clone: boolean;
  cloneVersion: string;
  apiKey: string;
}

export interface INewContract {
  apiOrgId: string;
  apiId: string;
  apiVersion: string;
  planId: string;
}

export interface IPolicyDefinitionSummary {
  id: string;
  policyImpl: string;
  name: string;
  description: string;
  icon: string;
  formType: 'Default' | 'JsonSchema';

  /** @format int64 */
  pluginId: number;
}

export interface IPluginSummary {
  /** @format int64 */
  id: number;
  groupId: string;
  artifactId: string;
  version: string;
  classifier: string;
  type: string;
  name: string;
  description: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
}

export interface IPlugin {
  /** @format int64 */
  id: number;
  groupId: string;
  artifactId: string;
  version: string;
  classifier: string;
  type: string;
  name: string;
  description: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
  deleted: boolean;
}

export interface INewPlugin {
  groupId: string;
  artifactId: string;
  version: string;
  classifier: string;
  type: string;
  name: string;
  description: string;
  upgrade: boolean;
}

export interface IUpdatePolicyDefinition {
  name: string;
  description: string;
  icon: string;
}

export interface IRole {
  id: string;
  name: string;
  description: string;
  createdBy: string;

  /** @format date-time */
  createdOn: string;
  autoGrant: boolean;
  permissions: (
    | 'orgView'
    | 'orgEdit'
    | 'orgAdmin'
    | 'apiView'
    | 'apiEdit'
    | 'apiAdmin'
    | 'clientView'
    | 'clientEdit'
    | 'clientAdmin'
    | 'planView'
    | 'planEdit'
    | 'planAdmin'
  )[];
}

export interface IUpdateRole {
  name: string;
  description: string;
  autoGrant: boolean;
  permissions: (
    | 'orgView'
    | 'orgEdit'
    | 'orgAdmin'
    | 'apiView'
    | 'apiEdit'
    | 'apiAdmin'
    | 'clientView'
    | 'clientEdit'
    | 'clientAdmin'
    | 'planView'
    | 'planEdit'
    | 'planAdmin'
  )[];
}

export interface INewRole {
  name: string;
  description: string;
  autoGrant: boolean;
  permissions: (
    | 'orgView'
    | 'orgEdit'
    | 'orgAdmin'
    | 'apiView'
    | 'apiEdit'
    | 'apiAdmin'
    | 'clientView'
    | 'clientEdit'
    | 'clientAdmin'
    | 'planView'
    | 'planEdit'
    | 'planAdmin'
  )[];
}

export interface IOrganizationSummary {
  id: string;
  name: string;
  description: string;

  /** @format int32 */
  numClients: number;

  /** @format int32 */
  numApis: number;

  /** @format int32 */
  numMembers: number;
}

export interface ISearchResultsOrganizationSummary {
  beans: IOrganizationSummary[];

  /** @format int32 */
  totalSize: number;
}

export interface IOrderBy {
  ascending: boolean;
  name: string;
}

export interface IPaging {
  /** @format int32 */
  page: number;

  /** @format int32 */
  pageSize: number;
}

export interface ISearchCriteria {
  filters: ISearchCriteriaFilter[];
  orderBy?: IOrderBy;
  paging: IPaging;
}

export interface ISearchCriteriaFilter {
  name: string;
  value: string;
  operator: 'bool_eq' | 'eq' | 'neq' | 'gt' | 'gte' | 'lt' | 'lte' | 'like';
}

export interface ISearchResultsClientSummary {
  beans: IClientSummary[];

  /** @format int32 */
  totalSize: number;
}

export interface ISearchResultsApiSummary {
  beans: IApiSummary[];

  /** @format int32 */
  totalSize: number;
}

export interface IAvailableApi {
  id: string;
  icon: string;
  endpoint: string;
  routeEndpoint: string;
  endpointType: 'rest' | 'soap' | 'ui';
  name: string;
  description: string;
  definitionUrl: string;
  routeDefinitionUrl: string;
  definitionType:
    | 'None'
    | 'SwaggerJSON'
    | 'SwaggerYAML'
    | 'WSDL'
    | 'WADL'
    | 'RAML'
    | 'External';
  namespace: string;
  tags: string[];
  internal: boolean;
}

export interface ISearchResultsAvailableApi {
  beans: IAvailableApi[];

  /** @format int32 */
  totalSize: number;
}

export interface IApiNamespace {
  name: string;
  ownedByUser: boolean;
  current: boolean;
}

export interface ISearchResultsUserSearchResult {
  beans: IUserSearchResult[];

  /** @format int32 */
  totalSize: number;
}

export interface IUserSearchResult {
  username: string;
  fullName: string;
}

export interface ISearchResultsRole {
  beans: IRole[];

  /** @format int32 */
  totalSize: number;
}

export interface ISystemStatus {
  id: string;
  name: string;
  description: string;
  moreInfo: string;
  version: string;
  builtOn: string;
  up: boolean;
}

export interface IPermission {
  name:
    | 'orgView'
    | 'orgEdit'
    | 'orgAdmin'
    | 'apiView'
    | 'apiEdit'
    | 'apiAdmin'
    | 'clientView'
    | 'clientEdit'
    | 'clientAdmin'
    | 'planView'
    | 'planEdit'
    | 'planAdmin';
  organizationId: string;
}

export interface IUserPermissions {
  userId: string;
  permissions: IPermission[];
}

export interface ICurrentUser {
  username: string;
  fullName: string;
  email: string;

  /** @format date-time */
  joinedOn: string;
  admin: boolean;
  permissions: IPermission[];
}

export interface IUser {
  username: string;
  fullName: string;
  email: string;

  /** @format date-time */
  joinedOn: string;
  admin: boolean;
}

export interface IUpdateUser {
  fullName: string;
  email: string;
}

export interface ISearchResultsNotifications {
  beans: INotificationsDto[];

  /** @format int32 */
  totalSize: number;
}

export interface INotificationsDto {
  /** @format int64 */
  id: number;
  category:
    | 'USER_ADMINISTRATION'
    | 'API_ADMINISTRATION'
    | 'API_LIFECYCLE'
    | 'OTHER';
  reason: string;
  reasonMessage: string;
  status: 'OPEN' | 'USER_DISMISSED' | 'SYSTEM_DISMISSED';

  /** @format date-time */
  createdOn: string;

  /** @format date-time */
  modifiedOn: string;
  recipient: IUserDto;
  source: string;
  payload: INotificationPayload;
}

export interface INotificationPayload {
  headers?: IApimanEventHeaders;
  clientOrgId?: string;
  clientId?: string;
  clientVersion?: string;
  apiOrgId?: string;
  apiId?: string;
  apiVersion?: string;
  contractId?: string;
  planId?: string;
  planVersion?: string;
  approved?: boolean;
  rejectionReason?: string;
  newStatus?: string;
  previousStatus?: string;
}

export interface IApimanEventHeaders {
  id?: string;

  /** @format uri */
  source?: string;
  type?: string;
  subject?: string;

  /** @format date-time */
  time?: string;

  /** @format int64 */
  eventVersion?: number;
  otherProperties?: unknown;
}

export interface IUserDto {
  id?: string;
  username?: string;
  fullName?: string;
  email?: string;
}
