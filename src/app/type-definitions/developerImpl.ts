import {ClientMapping, Developer} from '../services/api-data.service';

/**
 * Represents a Developer
 */
export class DeveloperImpl implements Developer {
  clients: Array<ClientMapping>;
  id: string;
  name: string;
}
