import {ClientMapping} from './app/api-data.service';

export class ClientMappingImpl implements ClientMapping{
  clientId: string;
  organizationId: string;

  constructor(clientId: string, organizationId: string) {
    this.clientId = clientId;
    this.organizationId = organizationId;
  }
}
