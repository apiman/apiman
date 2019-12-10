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

  constructor(private keycloak: KeycloakService, private router: Router, private route: ActivatedRoute) {
  }

  public logout() {
    const firstChild = this.route;
    sessionStorage.setItem('apiman_keycloak_token', '');
    sessionStorage.setItem('apiman_keycloak_refresh_token', '');
    this.keycloak.getKeycloakInstance().logout({
      redirectUri: location.href
    });
  }

}
