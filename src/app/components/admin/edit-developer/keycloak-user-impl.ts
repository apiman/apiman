import {KeycloakUser} from '../../../services/api-data.service';

export class KeycloakUserImpl implements KeycloakUser {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  username: string;
}
