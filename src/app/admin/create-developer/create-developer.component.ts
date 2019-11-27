import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {ApiDataService, Developer} from '../../api-data.service';
import {DeveloperImpl} from '../../../developerImpl';
import {DeveloperListComponent} from '../developer-list.component';
import {ClientMappingComponent} from './client-mapping.component';
import {ClientMappingImpl} from '../../../client-mapping-impl';
import {Router} from '@angular/router';
import {KeycloakUserImpl} from '../keycloak-user-impl';

@Component({
  selector: 'app-create-developer',
  templateUrl: './create-developer.component.html',
  styleUrls: ['./create-developer.component.scss']
})
export class CreateDeveloperComponent {

  public isPasswordRequiredForInsert = true;

  //pattern allows only strings with characters a-z A-Z and 0-9
  private nonSpecialCharacterPattern = Validators.pattern('[a-zA-Z0-9]+');

  public userFormGroup = new FormGroup({
    email: new FormControl('', [Validators.email]),
    firstname: new FormControl('', [this.nonSpecialCharacterPattern]),
    lastname:  new FormControl('', [this.nonSpecialCharacterPattern]),
    password:  new FormControl(''),
    username:  new FormControl('', [this.nonSpecialCharacterPattern])
  });

  @ViewChild('clientmapping', {static: false}) clientMapping: ClientMappingComponent;

  constructor(private apiDataService: ApiDataService, private router: Router) { }

  insertDeveloper() {
    const developerToCreate = new DeveloperImpl();
    developerToCreate.name = this.userFormGroup.get('username').value;
    developerToCreate.clients = [];
    this.clientMapping.assignedClients.forEach(client => {
      developerToCreate.clients.push(new ClientMappingImpl(client.clientId, client.organizationId));
    });

    const keycloakUserToCreate = new KeycloakUserImpl();
    keycloakUserToCreate.username = this.userFormGroup.get('username').value;
    keycloakUserToCreate.email = this.userFormGroup.get('email').value;
    keycloakUserToCreate.firstName = this.userFormGroup.get('firstname').value;
    keycloakUserToCreate.lastName = this.userFormGroup.get('lastname').value;
    keycloakUserToCreate.password = this.userFormGroup.get('password').value;

    this.apiDataService.createNewDeveloper(developerToCreate, keycloakUserToCreate)
      .subscribe(createdDeveloper => {
        this.userFormGroup.reset();
        this.clientMapping.reset();
        this.router.navigate(['/admin']);
      });
  }

  checkPasswordRequired(username) {
    return this.apiDataService.isPasswordRequired(username).then(isRequired => this.isPasswordRequiredForInsert = isRequired);
  }
}
