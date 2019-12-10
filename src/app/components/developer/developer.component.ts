import {Component, OnInit} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';

@Component({
  selector: 'app-developer',
  templateUrl: './developer.component.html',
  styleUrls: ['./developer.component.scss']
})

export class DeveloperComponent {

  public developers: Array<string> = [];

  constructor(private keycloak: KeycloakService) {
    // get keycloak client roles of devportal
    const resourceAccess = this.keycloak.getKeycloakInstance().tokenParsed.resource_access['devportal'];
    if (resourceAccess && resourceAccess.roles) {
      this.developers = resourceAccess.roles;
    }
  }
}
