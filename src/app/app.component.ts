import { Component } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  public developers: Array<string> = [];

  constructor(private keycloak: KeycloakService) {
  // get keycloak client roles of devportal
    const resourceAccess = this.keycloak.getKeycloakInstance().tokenParsed.resource_access['apiman-devportal'];
    if (resourceAccess && resourceAccess.roles) {
      this.developers = resourceAccess.roles;
    }
  }
}
