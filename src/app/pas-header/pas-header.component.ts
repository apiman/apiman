import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-pas-header',
  templateUrl: './pas-header.component.html',
  styleUrls: ['./pas-header.component.scss']
})
export class PasHeaderComponent implements OnInit {

  constructor(private keycloak: KeycloakService) { }

  ngOnInit() {
  }

  public logout() {
    localStorage.setItem('apiman_keycloak_token', '');
    localStorage.setItem('apiman_keycloak_refresh_token', '');
    this.keycloak.getKeycloakInstance().logout();
  }

}
