import {Component, Input, OnInit, ViewChild, OnDestroy} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {DeveloperImpl} from '../../../type-definitions/developerImpl';
import {DeveloperListComponent} from '../developer-list.component';
import {ClientMappingComponent} from './client-mapping.component';
import {ClientMappingImpl} from '../../../type-definitions/client-mapping-impl';
import {Router} from '@angular/router';
import {KeycloakUserImpl} from '../edit-developer/keycloak-user-impl';
import {AdminService} from '../services/admin.service';
import {DeveloperDataCacheService} from '../services/developer-data-cache.service';
import {Observable, Subject} from 'rxjs';
import {debounceTime, map, mergeMap, startWith} from 'rxjs/operators';
import {Toast, ToasterService} from 'angular2-toaster';
import UserRepresentation from 'keycloak-admin/lib/defs/userRepresentation';

@Component({
  selector: 'app-create-developer',
  templateUrl: './create-developer.component.html',
  styleUrls: ['./create-developer.component.scss']
})
export class CreateDeveloperComponent {

  // pattern allows only strings with characters a-z A-Z and 0-9
  private nonSpecialCharacterPattern = Validators.pattern('[a-zA-Z0-9]+');

  public userFormGroup = new FormGroup({
    email: new FormControl('', [Validators.email]),
    firstname: new FormControl('', [this.nonSpecialCharacterPattern]),
    lastname: new FormControl('', [this.nonSpecialCharacterPattern]),
    password: new FormControl(''),
    username: new FormControl('', [this.nonSpecialCharacterPattern])
  });

  @ViewChild('clientmapping', {static: false}) clientMapping: ClientMappingComponent;

  public usernameKeyUp = new Subject<KeyboardEvent>();
  private userNameInputSubscription;

  public keycloakUsers: Array<UserRepresentation>;
  public filteredKeycloakUsers: Array<UserRepresentation>;

  constructor(private adminService: AdminService, private router: Router, private developerDataCache: DeveloperDataCacheService, private toasterService: ToasterService) {
  }

  ngOnInit(): void {
    this.adminService.getKeycloakUsers().subscribe(keycloakUsers => {
      this.keycloakUsers = keycloakUsers;
      this.filteredKeycloakUsers = keycloakUsers.filter((user => this.checkDeveloperNotExists(user.username)));
    });

    this.userNameInputSubscription = this.usernameKeyUp
      .pipe(debounceTime(300), map(username => this.filterKeycloakUser(username)))
      .subscribe();
  }

  ngOnDestroy(): void {
    this.userNameInputSubscription.unsubscribe();
  }

  private filterKeycloakUser(value) {
    this.filteredKeycloakUsers = this.keycloakUsers.filter((u) => this.checkDeveloperNotExists(u.username) && u.username.toLowerCase().includes(value.toLowerCase()));
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
        console.log('pushed developer to cache', createdDeveloper);
        this.router.navigate(['/admin']);
      }, error => {
        this.adminService.rollbackDeveloperCreation(developerToCreate, keycloakUserToCreate)
          .subscribe(rollbackResponse => console.log('Rollback executed', rollbackResponse));
        this.developerDataCache.developers.splice(this.developerDataCache.developers
          .findIndex(developer => developer.name.toLowerCase() === developerToCreate.name.toLowerCase()), 1);

        console.error('Error creating developer', error);
        const errorToast: Toast = {
          type: 'error',
          body: error.message ? error.message : error.error.message,
          timeout: 30000,
          showCloseButton: true
        };
        this.toasterService.pop(errorToast);
      });
  }

  checkDeveloperNotExists(username: string) {
    return this.developerDataCache.developers
      && this.developerDataCache.developers.find((d) => d.name.toLowerCase() === username.toLowerCase()) === undefined;
  }

  checkKeycloakUserNotExists(username) {
    return this.keycloakUsers
      && this.keycloakUsers.find((u) => u.username.toLowerCase() === username.toLowerCase()) === undefined;
  }

  isPasswordRequiredForInsert(username) {
    return this.checkKeycloakUserNotExists(username);
  }

}
