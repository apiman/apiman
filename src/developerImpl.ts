import {ClientMapping, Developer} from './app/api-data.service';

export class DeveloperImpl implements Developer{
  clients: Array<ClientMapping>;
  id: string;
  name: string;
}
