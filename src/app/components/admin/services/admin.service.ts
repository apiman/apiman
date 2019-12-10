import {Inject, Injectable} from '@angular/core';
import {forkJoin, Observable} from 'rxjs';
import {map, mergeMap, share} from 'rxjs/operators';
import UserRepresentation from 'keycloak-admin/lib/defs/userRepresentation';
import {ClientSearchResult, Developer, KeycloakUser} from '../../../services/api-data.service';
import {HttpClient} from '@angular/common/http';
import {KeycloakInteractionService} from './keycloak-interaction.service';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  /**
   * Constructor of Admin Service
   * @param http Http Client
   * @param keycloak Keycloak Interaction Service
   * @param apimanUiRestUrl Apiman UI REST Url
   */
  constructor(private http: HttpClient,
              private keycloak: KeycloakInteractionService, @Inject('APIMAN_UI_REST_URL')
              private apimanUiRestUrl: string) {
  }

  /**
   * Get all available developers
   */
  public getAllDevelopers() {
    const url = this.apimanUiRestUrl + '/developers';
    return this.http.get(url) as Observable<Array<Developer>>;
  }

  /**
   * Get developer by name
   * @param name the developer name
   */
  private getDeveloperByName(name: string) {
    return this.getAllDevelopers().pipe(map(developers => developers.find(d => d.name.toLowerCase() === name.toLowerCase())));
  }

  /**
   * Get developer by developer id
   * @param developerId The developer id
   */
  public getDeveloper(developerId: string) {
    const url = this.apimanUiRestUrl + '/developers/' + developerId;
    return this.http.get(url) as Observable<Developer>;
  }

  /**
   * Check if password is required (check if user exists already in keycloak)
   * @param username The username
   */
  public isPasswordRequired(username) {
    return this.keycloak.findUser(username)
      .pipe(map(users => users.findIndex((user => user.username === username)) === -1));
  }

  /**
   * Get all keycloak users
   */
  public getKeycloakUsers() {
    return this.keycloak.getAllUsers();
  }

  /**
   * Create new developer
   * @param developer the developer to create
   */
  public createNewDeveloper(developer: Developer, keycloakUserToInsert: KeycloakUser) {
    const url = this.apimanUiRestUrl + '/developers';

    // 1. insert developer to API-Mgmt
    // response has developer object with developer id
    const insertRequests = (this.http.post(url, developer) as Observable<Developer>)
      .pipe(mergeMap(developerInserted => {
        // 2. insert user into keycloak
        return forkJoin(this.keycloak.findExistingOrCreateUser(keycloakUserToInsert)
          .pipe(mergeMap((insertedKeycloakUser) =>
            // 3. add devPortal role and add client role to keycloak user
            forkJoin(this.keycloak.addDevPortalGroupToUser(insertedKeycloakUser),
              this.keycloak.addClientRoleToUser(insertedKeycloakUser, developerInserted.id))
          )))
          .pipe(map(() => developerInserted));
      }));
    return insertRequests;
  }

  public rollbackDeveloperCreation(developerToRollback: Developer, keycloakUserToRollback: KeycloakUser) {
    const getDeveloperByName = this.getDeveloperByName(developerToRollback.name).pipe(share());

    const deleteDeveloperFromApiman = getDeveloperByName
      .pipe(mergeMap(developerToDelete => this.deleteDeveloperFromApiman(developerToDelete)));
    const deleteKeycloakClientRole = getDeveloperByName
      .pipe(mergeMap(developerToDelete => this.keycloak.deleteClientRole(developerToDelete.id)));
    const deleteKeycloakUser = this.keycloak.deleteUser(keycloakUserToRollback.username);

    return forkJoin(deleteDeveloperFromApiman, deleteKeycloakClientRole, deleteKeycloakUser);
  }

  /**
   * Update a developer
   * @param developer the developer to update
   */
  public updateDeveloper(developer: Developer) {
    const url = this.apimanUiRestUrl + '/developers/' + developer.id;
    return this.http.put(url, {
      name: developer.name,
      clients: developer.clients
    });
  }

  /**
   * Delete developer from apiman
   * @param developer the developer
   */
  private deleteDeveloperFromApiman(developer: Developer) {
    const url = this.apimanUiRestUrl + '/developers/' + developer.id;
    return this.http.delete(url);
  }

  /**
   * Delete a developer
   * @param developer the developer to update
   */
  public deleteDeveloper(developer: Developer) {
    return forkJoin(
      this.deleteDeveloperFromApiman(developer),
      this.keycloak.deleteClientRole(developer.id),
      this.keycloak.deleteUser(developer.name)
    );
  }

  /**
   * Get all available clients
   */
  public getAllClients() {
    const url = this.apimanUiRestUrl + '/search/clients';
    const searchQuery = {
      filters: [{
        name: 'name',
        value: '*',
        operator: 'like'
      }],

      paging: {
        page: '1',
        pageSize: '10000'
      }
    };
    return (this.http.post(url, searchQuery) as Observable<ClientSearchResult>)
      .pipe(mergeMap(searchResult => searchResult.beans));
  }
}
