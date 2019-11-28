import { Component, Input, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { DeveloperImpl } from '../../../developerImpl';
import { DeveloperListComponent } from '../developer-list.component';
import { ClientMappingComponent } from './client-mapping.component';
import { ClientMappingImpl } from '../../../client-mapping-impl';
import { Router } from '@angular/router';
import { KeycloakUserImpl } from '../keycloak-user-impl';
import { AdminService } from '../admin.service';
import { DeveloperDataCacheService } from '../developer-data-cache.service';
import { Subject } from 'rxjs';
import { debounceTime, map, mergeMap } from 'rxjs/operators';

@Component({
  selector: 'app-create-developer',
  templateUrl: './create-developer.component.html',
  styleUrls: ['./create-developer.component.scss']
})
export class CreateDeveloperComponent {

  public isPasswordRequiredForInsert = true;

  // pattern allows only strings with characters a-z A-Z and 0-9
  private nonSpecialCharacterPattern = Validators.pattern('[a-zA-Z0-9]+');

  public userFormGroup = new FormGroup({
    email: new FormControl('', [Validators.email]),
    firstname: new FormControl('', [this.nonSpecialCharacterPattern]),
    lastname:  new FormControl('', [this.nonSpecialCharacterPattern]),
    password:  new FormControl(''),
    username:  new FormControl('', [this.nonSpecialCharacterPattern])
  });

  @ViewChild('clientmapping', {static: false}) clientMapping: ClientMappingComponent;

  public usernameKeyUp = new Subject<KeyboardEvent>();
  private userNameInputSubscription;

  constructor(private adminService: AdminService, private router: Router, private developerDataCache: DeveloperDataCacheService) {  }

  ngOnInit(): void {
    this.userNameInputSubscription = this.usernameKeyUp
      .pipe(
        debounceTime(300),
        mergeMap(username => this.adminService.isPasswordRequired(username)
          .pipe(map(isRequired => this.isPasswordRequiredForInsert = isRequired))
        )).subscribe();
  }

  ngOnDestroy(): void {
    this.userNameInputSubscription.unsubscribe();
  }

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

    this.adminService.createNewDeveloper(developerToCreate, keycloakUserToCreate)
      .subscribe(createdDeveloper => {
        this.userFormGroup.reset();
        this.clientMapping.reset();
        this.developerDataCache.developers.push(createdDeveloper);
        this.router.navigate(['/admin']);
      });
  }

  checkDeveloperNotExists(userName: string) {
    return this.developerDataCache.developers
      && this.developerDataCache.developers.find((d) => d.name.toLowerCase() === userName.toLowerCase()) === undefined;
  }

}
