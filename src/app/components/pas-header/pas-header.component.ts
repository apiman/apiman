import {Component, OnInit} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-pas-header',
  templateUrl: './pas-header.component.html',
  styleUrls: ['./pas-header.component.scss']
})
export class PasHeaderComponent {

  public user: string = this.keycloak.getKeycloakInstance().profile.username;

  constructor(private keycloak: KeycloakService) {
  }

  /**
   * Logout a user and clear the session tokens
   */
  public logout() {
    sessionStorage.clear();
    this.keycloak.getKeycloakInstance().logout({
      redirectUri: location.href
    });
  }

}
