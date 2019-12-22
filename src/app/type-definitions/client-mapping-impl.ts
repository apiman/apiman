import {ClientMapping} from '../services/api-data.service';

/**
 * Represents a Client Mapping
 */
export class ClientMappingImpl implements ClientMapping{
  clientId: string;
  organizationId: string;

  constructor(clientId: string, organizationId: string) {
    this.clientId = clientId;
    this.organizationId = organizationId;
  }
}
