import {OrganizationBean} from "./organization.model";

export interface ApiBean {
  organization: OrganizationBean;
  id: string;
  name: string;
  image: string;
  description: string;
  tags: KeyValueTagDto[];
  createdBy: string;
  createdOn: Date;
  numPublished: number;
}

export interface UpdateApiBean {
  description?: string;
  image?: string;
  tags?: KeyValueTagDto[];
}

export interface ApiPlanSummaryBean {
  planId: string;
  planName: string;
  planDescription: string;
  version: string;
  exposeInPortal: boolean;
  requiresApproval: boolean;
}

export interface ApiGatewayBean {
  gatewayId: string
}

export interface ApiPlanBean {
  planId: string;
  version: string;
  exposeInPortal: boolean;
  requiresApproval: boolean
}

export interface KeyValueTagDto {
  key: string,
  value?: string
}

export interface UpdateApiVersionBean {
  endpoint: string;
  endpointType: string; // enum EndpointType
  endpointContentType: string // enum EndpointContentType;
  endpointProperties: Map<string, string>;
  gateways: ApiGatewayBean[]; // Set<ApiGatewayBean>
  parsePayload: boolean;
  publicAPI: boolean;
  disableKeysStrip: boolean;
  plans: ApiPlanBean[];
  extendedDescription: string;
  exposeInPortal: boolean
}

export interface ApiVersionBean {
  id: number;
  api: ApiBean;
  status: string; // Enum ApiStatus
  endpoint: string;
  endpointType: string; //enum EndpointType
  endpointContentType: string; // enum EndpointContentType
  endpointProperties: Map<string, string>;
  gateways: ApiGatewayBean[];
  publicAPI: boolean;
  //apiDefinition: ApiDefinitionBean
  plans: ApiPlanBean[];
  version: string;
  createdBy: string;
  createdOn: string;
  modifiedBy: string;
  modifiedOn: string;
  publishedOn: string;
  retiredOn: string;
  definitionType: string // ApiDefinitionType
  parsePayload: boolean;
  disableKeysStrip: boolean;
  definitionUrl: string;
  exposeInPortal: boolean;
  extendedDescription: string
}
