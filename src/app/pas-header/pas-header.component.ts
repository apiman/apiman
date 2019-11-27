import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-pas-header',
  templateUrl: './pas-header.component.html',
  styleUrls: ['./pas-header.component.scss']
})
export class PasHeaderComponent implements OnInit {

  public user: string = this.keycloak.getKeycloakInstance().profile.username;

  constructor(private keycloak: KeycloakService) { }

  ngOnInit() {
  }

  public logout() {
    sessionStorage.setItem('apiman_keycloak_token', '');
    sessionStorage.setItem('apiman_keycloak_refresh_token', '');
    this.keycloak.getKeycloakInstance().logout();
  }

}
