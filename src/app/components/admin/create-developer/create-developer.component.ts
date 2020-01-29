import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {DeveloperImpl} from '../../../type-definitions/developerImpl';
import {ClientMappingComponent} from './client-mapping.component';
import {ClientMappingImpl} from '../../../type-definitions/client-mapping-impl';
import {Router} from '@angular/router';
import {KeycloakUserImpl} from '../edit-developer/keycloak-user-impl';
import {AdminService} from '../services/admin.service';
import {DeveloperDataCacheService} from '../services/developer-data-cache.service';
import {forkJoin, Subject} from 'rxjs';
import {debounceTime, map, mergeMap} from 'rxjs/operators';
import {Toast, ToasterService} from 'angular2-toaster';
import UserRepresentation from 'keycloak-admin/lib/defs/userRepresentation';
import {SpinnerService} from '../../../services/spinner.service';

@Component({
  selector: 'app-create-developer',
  templateUrl: './create-developer.component.html',
  styleUrls: ['./create-developer.component.scss']
})
export class CreateDeveloperComponent implements OnInit, OnDestroy {

  // pattern allows only strings without slashes and square brackets (taken from keycloak)
  private usernamePattern = Validators.pattern('^[^\\<\\>\\\\\\/]*$');

  public userFormGroup = new FormGroup({
    email: new FormControl('', [Validators.email]),
    firstname: new FormControl(''),
    lastname: new FormControl(''),
    password: new FormControl(''),
    username: new FormControl('', [this.usernamePattern])
  });

  @ViewChild('clientmapping', {static: false}) clientMapping: ClientMappingComponent;

  public usernameKeyUp = new Subject<string>();
  private userNameInputSubscription;

  public keycloakUsers: Array<UserRepresentation>;
  public filteredKeycloakUsers: Array<UserRepresentation>;

  constructor(private adminService: AdminService,
              private router: Router,
              private developerDataCache: DeveloperDataCacheService,
              private toasterService: ToasterService,
              private loadingSpinnerService: SpinnerService) {
  }

  /**
   * On init life circle
   */
  ngOnInit(): void {
    const getKeycloakUserTask = this.adminService.getKeycloakUsers().pipe(map(keycloakUsers => {
      this.keycloakUsers = keycloakUsers;
      this.filteredKeycloakUsers = keycloakUsers.filter((user => this.checkDeveloperNotExists(user.username)));
    }));
    const userNameInputTask = this.userNameInputSubscription = this.usernameKeyUp
      .pipe(debounceTime(300), map(username => this.filterKeycloakUser(username)));

    const tasks = [];
    if (!this.developerDataCache.developers) {
      tasks.push(this.developerDataCache.load());
    }
    tasks.push(getKeycloakUserTask);
    tasks.push(userNameInputTask);

    this.loadingSpinnerService.startWaiting();
    forkJoin(tasks).subscribe(() =>
        this.loadingSpinnerService.stopWaiting(),
      error => {
        this.toasterService.pop('error', 'Error initializing page');
      });
  }

  /**
   * On destroy life circle
   */
  ngOnDestroy(): void {
    this.userNameInputSubscription.unsubscribe();
  }

  /**
   * Filter the user auto complete list
   * @param usernameInput the inputed username
   */
  private filterKeycloakUser(usernameInput: string) {
    this.filteredKeycloakUsers = this.keycloakUsers
      .filter((u) => this.checkDeveloperNotExists(u.username) && u.username.toLowerCase().includes(usernameInput.toLowerCase()));
  }

  /**
   * Insert a developer
   */
  insertDeveloper() {
    this.loadingSpinnerService.startWaiting();

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
        this.loadingSpinnerService.stopWaiting();
        this.router.navigate(['/admin']);
      }, error => {
        this.loadingSpinnerService.stopWaiting();

        const errorMessage = 'Error creating developer';
        console.error(errorMessage, error);
        this.toasterService.pop('error', errorMessage, error.message);

        const developerIndexToDelete = this.developerDataCache.developers
          .findIndex(developer => developer.name.toLowerCase() === developerToCreate.name.toLowerCase());
        if (developerIndexToDelete !== -1) {
          this.developerDataCache.developers.splice(developerIndexToDelete, 1);
        }
      });
  }

  /**
   * Check if the developer does not exists
   * @param username the developer username
   */
  checkDeveloperNotExists(username: string) {
    return this.developerDataCache.developers
      && this.developerDataCache.developers.find((d) => d.name.toLowerCase() === username.toLowerCase()) === undefined;
  }

  /**
   * Check if email adress is used
   * @param email the email address
   */
  checkEmailAlreadyUsed(email: string) {
    // check if the keycloak user does not exists
    return this.keycloakUsers
      && this.keycloakUsers.find((u) => u.email && u.email.toLowerCase() === email.toLowerCase()) !== undefined;
  }

  /**
   * Check if the a password is required for an insert
   * @param username the keycloak username
   */
  isPasswordRequiredForInsert(username: string) {
    // check if the keycloak user does not exists
    return this.keycloakUsers
      && this.keycloakUsers.find((u) => u.username.toLowerCase() === username.toLowerCase()) === undefined;
  }
}
