import {Inject, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {forkJoin, from, iif, merge, Observable, of} from 'rxjs';
import {combineLatest, concatAll, defaultIfEmpty, filter, find, map, mergeAll, mergeMap, share, single, switchMap} from 'rxjs/operators';
import { KeycloakService } from 'keycloak-angular';
import KcAdminClient from 'keycloak-admin';
import {emit} from 'cluster';
import has = Reflect.has;
import {RoleMappingPayload} from 'keycloak-admin/lib/defs/roleRepresentation';
import {RequiredActionAlias} from 'keycloak-admin/lib/defs/requiredActionProviderRepresentation';
import UserConsentRepresentation from 'keycloak-admin/lib/defs/userConsentRepresentation';
import CredentialRepresentation from 'keycloak-admin/lib/defs/credentialRepresentation';
import FederatedIdentityRepresentation from 'keycloak-admin/lib/defs/federatedIdentityRepresentation';
import UserRepresentation from 'keycloak-admin/lib/defs/userRepresentation';

/**
 * Api Version
 */
export interface ApiVersion {
  id: number;
  api: Api;
  status: 'Created' | 'Ready' | 'Published' | 'Retired';
  endpoint: string;
  endpointType: 'rest' | 'soap';
  endpointContentType: 'json' | 'xml';
  endpointProperties: object;
  gateways: Array<Gateway>;
  publicAPI: boolean;
  plans: Array<Plan>;
  version: string;
  createdBy: string;
  createdOn: number;
  modifiedBy: string;
  modifiedOn: number;
  publishedOn: number;
  retiredOn: string;
  definitionType: 'None' | 'SwaggerJSON' | 'SwaggerYAML' | 'WSDL' | 'WADL' | 'RAML' | 'External';
  parsePayload: boolean;
  definitionUrl: string;
}

/**
 * Plan
 */
export interface Plan {
  version: string;
  planId: string;
}

/**
 * Gateway
 */
export interface Gateway {
  gatewayId: string;
}

/**
 * Api definition
 */
export interface Api {
  organization: Organization;
  id: string;
  name: string;
  description: string;
  createdBy: string;
  createdOn: number;
  numPublished: number;
}

/**
 * Organization
 */
export interface Organization {
  id: string;
  name: string;
  description: string;
  createdBy: string;
  createdOn: number;
  modifiedBy: string;
  modifiedOn: number;
}

/**
 * Client
 */
export interface Client {
  organizationId: string;
  organizationName: string;
  id: string;
  name: string;
  description: string;
  status: 'Created' | 'Ready' | 'Registered' | 'Retired';
  version: string;
  apiKey: string;
}

/**
 * Contract
 */
export interface Contract {
  contractId: number;
  clientOrganizationId: string;
  clientOrganizationName: string;
  clientId: string;
  clientName: string;
  clientVersion: string;
  apiOrganizationId: string;
  apiOrganizationName: string;
  apiId: string;
  apiName: string;
  apiVersion: string;
  apiDescription: string;
  planName: string;
  planId: string;
  planVersion: string;
  createdOn: string;
}

export interface ClientMapping {
  clientId: string;
  organizationId: string;
}

export interface Developer {
  id: string;
  name: string;
  clients: Array<ClientMapping>;
}

export interface ClientBean {
  name: string;
  id: string;
  description: string;
  createdOn: string;
  organizationName: string;
  organizationId: string;
}

export interface ClientSearchResult {
  beans: Array<ClientBean>;
  totalSize: number;
}

/**
 * Gateway Details
 */
export interface GatewayDetails {
  name: string;
  id: string;
  type: 'REST';
  description: string;
}

/**
 * Gateway Endpoint
 */
export interface GatewayEndpoint {
  endpoint: string;
}

export interface KeycloakUser {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})

/**
 * A service which executes the REST calls to Apiman UI REST Interface
 */
export class ApiDataService {

  private kcAdminClient: KcAdminClient;
  private getDevPortalClientUUID: Observable<string>;

  /**
   * Contructor
   * @param http The http client
   * @param apimanUiRestUrl The apiman UI REST url
   */
  constructor(private http: HttpClient, private keycloak: KeycloakService, @Inject('APIMAN_UI_REST_URL') private apimanUiRestUrl: string, @Inject('KEYCLOAK_AUTH_URL') private apimanKeycloakRestUrl: string) {
    this.kcAdminClient = new KcAdminClient({
      baseUrl: this.apimanKeycloakRestUrl,
      realmName: 'Apiman'
    });
    this.kcAdminClient.setAccessToken(keycloak.getKeycloakInstance().token);
    this.getDevPortalClientUUID = from(this.kcAdminClient.clients.find())
      .pipe(mergeAll())
      .pipe(single(client => client.clientId === 'apiman-devportal'), map(client => client.id))
      .pipe(share());


    // const getAllDeveloperKeyCloakClientRoles = this.getDevPortalClientId.pipe(mergeMap(clientId =>
    //   this.kcAdminClient.clients.listRoles({
    //     id: clientId
    //   })));
  }

  /**
   * Get developer clients by developer id
   * @param developerId The developer Id
   */
  public getDeveloperClients(developerId: string) {
    const url = this.apimanUiRestUrl + '/developers/' + developerId + '/clients';
    return this.http.get(url) as Observable<Array<Client>>;
  }

  /**
   * Get developer contracts by developer id
   * @param developerId The developer Id
   */
  public getDeveloperContracts(developerId: string) {
    const url = this.apimanUiRestUrl + '/developers/' + developerId + '/contracts';
    return this.http.get(url) as Observable<Array<Contract>>;
  }

  /**
   * Get developer apis by developer id
   * @param developerId The developer Id
   */
  public getDeveloperApis(developerId: string) {
    const url = this.apimanUiRestUrl + '/developers/' + developerId + '/apis';
    return this.http.get(url) as Observable<Array<ApiVersion>>;
  }

  /**
   * Get available api gateways
   */
  public getGateways() {
    const url = this.apimanUiRestUrl + '/gateways';
    return this.http.get(url) as Observable<Array<GatewayDetails>>;
  }

  /**
   * Get gateway endpoint by gateway id
   * @param gatewayId The gateway id
   */
  public getGatewayEndpoint(gatewayId) {
    const url = this.apimanUiRestUrl + '/gateways/' + gatewayId + '/endpoint';
    return this.http.get(url) as Observable<GatewayEndpoint>;
  }

  /**
   * Get all available developers
   */
  public getAllDevelopers() {
    const url = this.apimanUiRestUrl + '/developers';
    return this.http.get(url) as Observable<Array<Developer>>;
  }

  private searchKeycloakUser = (keycloakUsername) => this.kcAdminClient.users.find({username: keycloakUsername});

  public isPasswordRequired(username) {
    return this.searchKeycloakUser(username).then(users => {
      return users.findIndex((user => user.username === username)) === -1;
    }).catch(error => {
      console.error(error);
      return Promise.resolve(true);
    });
  }

  /**
   * Create new developer
   * @param developer the developer to create
   */
  public createNewDeveloper(developer: Developer, keycloakUser: KeycloakUser) {
    const url = this.apimanUiRestUrl + '/developers';

    //1. insert developer
    //response has developer object with developer id
    const insertDeveloperToApiman = (this.http.post(url, developer) as Observable<Developer>)
      .pipe(map(developerInserted => developerInserted.id))
      .toPromise();

    // const searchKeycloakUser = (keycloakUsername) => this.kcAdminClient.users.find({username: keycloakUsername});

    const addUserToKeycloak = (userToInsert) => this.kcAdminClient.users.create({
      firstName: userToInsert.firstName,
      lastName: userToInsert.lastName,
      email: userToInsert.email,
      username: userToInsert.username,
      enabled: true,
      attributes: {generatedFromDevPortal: ['true']}
    });

    const setPasswordForUser = (userId, password) => password && password.length !== 0 ? this.kcAdminClient.users.resetPassword({
      id: userId,
      credential: {
        temporary: true,
        type: 'password',
        value: password,
      },
    }) : Promise.reject('no password set');

    const createKeycloakRoleRequest = (developerId) => this.getDevPortalClientUUID.pipe(map(clientUUID => this.kcAdminClient.clients.createRole({
      id: clientUUID,
      name: developerId
    }))).toPromise();

    const getRoleUUID = (clientUUID, roleName) => this.kcAdminClient.clients.findRole({
      id: clientUUID,
      roleName
    }).then(roleObject => roleObject.id);

    const addClientRoleMappingToUser = (userKeycloakUUID, clientUUID, roleUUID, roleName) => this.kcAdminClient.users.addClientRoleMappings({
      id: userKeycloakUUID,
      clientUniqueId: clientUUID,
      roles: [{
        id: roleUUID,
        name: roleName
      }]
    });

    return from(insertDeveloperToApiman.then(apimanDeveloperId =>
      this.searchKeycloakUser(keycloakUser.username).then(foundUsers => {
        let chain: Promise<string> = null;
        if (foundUsers.length === 0) {
          chain = addUserToKeycloak(keycloakUser)
            .then(insertedUser => setPasswordForUser(insertedUser.id, keycloakUser.password)
              .catch(reason => reason === 'no password set')
              .then(() => Promise.resolve(insertedUser.id)));
        } else {
          chain = Promise.resolve(foundUsers[0].id);
        }
        return chain.then((insertedKeycloakUserId) => createKeycloakRoleRequest(apimanDeveloperId)
          .then((roleObject) => this.getDevPortalClientUUID.toPromise().then(clientUUID =>
              getRoleUUID(clientUUID, apimanDeveloperId)
                .then(roleUUID => addClientRoleMappingToUser(insertedKeycloakUserId, clientUUID, roleUUID, apimanDeveloperId))
            )
          ));
      })
    ));
  }

  /**
   * Update a developer
   * @param developer the developer to update
   */
  public updateDeveloper(developer: Developer) {
    const url = this.apimanUiRestUrl + '/developers';
    return this.http.put(url, developer);
  }

  private isUserGeneratedFromDevPortal(user: UserRepresentation) {
    return user.attributes && user.attributes.generatedFromDevPortal && user.attributes.generatedFromDevPortal.length > 0 && user.attributes.generatedFromDevPortal[0] === 'true';
  }

  /**
   * Delete a developer
   * @param developer the developer to update
   */
  public deleteDeveloper(developer: Developer) {
    const url = this.apimanUiRestUrl + '/developers/' + developer.id;
    const deleteDeveloperFromApiman = this.http.delete(url);

    const deleteKeycloakClientRole = from(this.getDevPortalClientUUID.pipe(mergeMap(clientId => this.kcAdminClient.clients.delRole({
      id: clientId,
      roleName: developer.id
    }))));

    const deleteKeycloakUser = from(this.kcAdminClient.users.find({
      username: developer.name
    }).then(users => {
      if (users.length > 0) {
        const userToDelete = users[0];
        //delete user only if he was generated from dev portal
        if (this.isUserGeneratedFromDevPortal(userToDelete)) {
          return this.kcAdminClient.users.del({
            id: userToDelete.id
          });
        }
      }
      return Promise.resolve();
    }));
    return forkJoin(deleteDeveloperFromApiman, deleteKeycloakClientRole, deleteKeycloakUser);
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
