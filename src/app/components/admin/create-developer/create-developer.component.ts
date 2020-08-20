import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {DeveloperImpl} from '../../../type-definitions/developerImpl';
import {ClientMappingComponent} from './client-mapping.component';
import {ClientMappingImpl} from '../../../type-definitions/client-mapping-impl';
import {Router} from '@angular/router';
import {AdminService} from '../services/admin.service';
import {DeveloperDataCacheService} from '../services/developer-data-cache.service';
import {forkJoin, Subject} from 'rxjs';
import {debounceTime, map} from 'rxjs/operators';
import {ToasterService} from 'angular2-toaster';
import UserRepresentation from 'keycloak-admin/lib/defs/userRepresentation';
import {SpinnerService} from '../../../services/spinner.service';

@Component({
  selector: 'app-create-developer',
  templateUrl: './create-developer.component.html',
  styleUrls: ['./create-developer.component.scss']
})
export class CreateDeveloperComponent implements OnInit, OnDestroy {

  public userFormGroup = new FormGroup({
    username: new FormControl('')
  });

  @ViewChild('clientmapping', {static: false}) clientMapping: ClientMappingComponent;

  public usernameKeyUp = new Subject<string>();
  public keycloakUsers: Array<UserRepresentation>;
  public filteredKeycloakUsers: Array<UserRepresentation>;
  private userNameInputSubscription;

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
   * Insert a developer
   */
  insertDeveloper() {
    this.loadingSpinnerService.startWaiting();

    const keycloakUser: UserRepresentation = this.filteredKeycloakUsers
      .find(user => user.username === this.userFormGroup.get('username').value);

    const developerToCreate = new DeveloperImpl();
    developerToCreate.id = keycloakUser.username;
    developerToCreate.clients = [];
    this.clientMapping.assignedClients.forEach(client => {
      developerToCreate.clients.push(new ClientMappingImpl(client.clientId, client.organizationId));
    });

    this.adminService.createNewDeveloper(developerToCreate)
      .subscribe(createdDeveloper => {
        this.userFormGroup.reset();
        this.clientMapping.reset();
        this.developerDataCache.developers.push(createdDeveloper);
        console.log('Pushed developer to cache', createdDeveloper);
        this.loadingSpinnerService.stopWaiting();
        this.router.navigate(['/admin']);
      }, error => {
        this.loadingSpinnerService.stopWaiting();

        const errorMessage = 'Error creating developer';
        console.error(errorMessage, error);
        this.toasterService.pop('error', errorMessage, error.message);

        const developerIndexToDelete = this.developerDataCache.developers
          .findIndex(developer => developer.id.toLowerCase() === developerToCreate.id.toLowerCase());
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
    if (this.developerDataCache.developers && username && username.length !== 0) {
        return this.developerDataCache.developers.find((d) => d.id.toLowerCase() === username.toLowerCase()) === undefined;
    } else {
      return true;
    }
  }

  /**
   * Check if the user exists in keycloak
   * @param username the developer username
   */
  checkUserExistsInKeycloak(username: string) {
    if (this.keycloakUsers && username && username.length !== 0) {
      return this.keycloakUsers.find((u) => u.username.toLowerCase() === username) !== undefined;
    } else {
      return false;
    }
  }

  /**
   * Filter the user auto complete list
   * @param usernameInput the entered username
   */
  private filterKeycloakUser(usernameInput: string) {
    if (usernameInput && usernameInput.length !== 0) {
      this.filteredKeycloakUsers = this.keycloakUsers
        .filter((u) => this.checkDeveloperNotExists(u.username) && u.username.toLowerCase().includes(usernameInput.toLowerCase()));
    }
  }
}
