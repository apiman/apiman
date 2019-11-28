import { Inject, Injectable } from '@angular/core';
import { forkJoin, Observable } from 'rxjs';
import { map, mergeMap } from 'rxjs/operators';
import UserRepresentation from 'keycloak-admin/lib/defs/userRepresentation';
import { ClientSearchResult, Developer, KeycloakUser } from '../api-data.service';
import { HttpClient } from '@angular/common/http';
import { KeycloakInteractionService } from './keycloak-interaction.service';

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
              private apimanUiRestUrl: string) {}

  /**
   * Get all available developers
   */
  public getAllDevelopers() {
    const url = this.apimanUiRestUrl + '/developers';
    return this.http.get(url) as Observable<Array<Developer>>;
  }

  /**
   * Get developer by developer id
   * @param developerId The developer id
   */
  public getDeveloper(developerId: string) {
    const url = this.apimanUiRestUrl + '/developers/' + developerId;
    return this.http.get(url) as Observable<Developer>;
  }

  public isPasswordRequired(username) {
    return this.keycloak.searchUser(username)
      .pipe(map(users => users.findIndex((user => user.username === username)) === -1));
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
            forkJoin(this.keycloak.addDevPortalRoleToUser(insertedKeycloakUser),
              this.keycloak.addClientRoleToUser(insertedKeycloakUser, developerInserted.id))
          )))
          .pipe(map(() => developerInserted));
      }));
    return insertRequests;
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
   * Delete a developer
   * @param developer the developer to update
   */
  public deleteDeveloper(developer: Developer) {
    const url = this.apimanUiRestUrl + '/developers/' + developer.id;
    const deleteDeveloperFromApiman = this.http.delete(url);
    return forkJoin(
      deleteDeveloperFromApiman,
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
      .pipe(mergeMap( searchResult => searchResult.beans));
  }
}
