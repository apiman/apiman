import {Inject, Injectable} from '@angular/core';
import {forkJoin, Observable, of, Subject} from 'rxjs';
import {catchError, map, mergeMap, share} from 'rxjs/operators';
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
   * @param apiMgmtUiRestUrl Api Management UI REST Url
   */
  constructor(private http: HttpClient,
              private keycloak: KeycloakInteractionService, @Inject('API_MGMT_UI_REST_URL')
              private apiMgmtUiRestUrl: string) {
  }

  /**
   * Get all available developers
   */
  public getAllDevelopers() {
    const url = this.apiMgmtUiRestUrl + '/developers';
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
    const url = this.apiMgmtUiRestUrl + '/developers/' + developerId;
    return this.http.get(url) as Observable<Developer>;
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
    // observer to insert keycloak user
    const insertToKeycloak = this.keycloak.findExistingOrCreateUser(keycloakUserToInsert);
    // observer insert developer to API-Mgmt
    // response has developer object with developer id
    const url = this.apiMgmtUiRestUrl + '/developers';
    const insertToApiMgmt = this.http.post(url, developer) as Observable<Developer>;
    // observer to add keycloak user to developer portal group
    const addDevPortalGroupToUser = (insertedKeycloakUser) => this.keycloak.addDevPortalGroupToUser(insertedKeycloakUser.id);
    // observer to add client role to user
    const addClientRoleToUser = (insertedKeycloakUser, insertedDeveloper) => {
      const clientRoleDescription = 'role for user: ' + insertedDeveloper.name;
      return this.keycloak.addClientRoleToUser(insertedKeycloakUser.id, insertedDeveloper.id, clientRoleDescription);
    };

    // 1. insert user into keycloak
    return insertToKeycloak.pipe(mergeMap(insertedKeycloakUser => {
      // 2. insert developer to API-Mgmt
      return insertToApiMgmt.pipe(mergeMap(insertedDeveloper => {
        // 3. add devPortal group and add client role to keycloak user
        return forkJoin(addDevPortalGroupToUser(insertedKeycloakUser), addClientRoleToUser(insertedKeycloakUser, insertedDeveloper))
          .pipe(map(() => insertedDeveloper),
            catchError((err, caught) => {
              // rollback developer if keycloak settings cannot be done
              this.deleteDeveloperFromApiMgmt(insertedDeveloper.id).subscribe();
              throw err;
            }));
      }), catchError((err, caught) => {
        // rollback keycloak user if developer cannot created at API-Mgmt
        this.keycloak.deleteUser(keycloakUserToInsert.username).subscribe();
        throw err;
      }));
    }));
  }

  /**
   * Update a developer
   * @param developer the developer to update
   */
  public updateDeveloper(developer: Developer) {
    const url = this.apiMgmtUiRestUrl + '/developers/' + developer.id;
    return this.http.put(url, {
      name: developer.name,
      clients: developer.clients
    });
  }

  /**
   * Delete developer from API Mgmt
   * @param developerId the developer id
   */
  private deleteDeveloperFromApiMgmt(developerId: string) {
    const url = this.apiMgmtUiRestUrl + '/developers/' + developerId;
    return this.http.delete(url);
  }

  /**
   * Delete a developer
   * @param developer the developer to update
   */
  public deleteDeveloper(developer: Developer) {
    return forkJoin(
      this.deleteDeveloperFromApiMgmt(developer.id),
      this.keycloak.deleteClientRole(developer.id),
      this.keycloak.deleteUser(developer.name)
    );
  }

  /**
   * Get all available clients
   */
  public getAllClients() {
    const url = this.apiMgmtUiRestUrl + '/search/clients';
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
      .pipe(mergeMap(searchResult => searchResult.beans.length > 0 ? searchResult.beans : of(null)));
  }
}
