import { Component, OnInit } from '@angular/core';
import {KeycloakUserService} from '../../services/keycloak-user.service';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent {

  constructor(public keycloakUser: KeycloakUserService) { }

}
