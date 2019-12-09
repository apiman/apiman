import { Inject, Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import KcAdminClient from 'keycloak-admin';
import { from, Observable, of } from 'rxjs';
import { map, mergeAll, mergeMap, share, single } from 'rxjs/operators';
import UserRepresentation from 'keycloak-admin/lib/defs/userRepresentation';

@Injectable({
  providedIn: 'root'
})
export class KeycloakInteractionService {

  private kcAdminClient: KcAdminClient;

  /**
   * Constructor of Keycloak Interaction Service
   * @param keycloak the keycloak service
   * @param apimanKeycloakRestUrl the apiman keycloak rest url
   */
  constructor(private keycloak: KeycloakService, @Inject('KEYCLOAK_AUTH_URL') private apimanKeycloakRestUrl: string) {
    this.kcAdminClient = new KcAdminClient({
      baseUrl: this.apimanKeycloakRestUrl,
      realmName: 'Apiman'
    });
    this.kcAdminClient.setAccessToken(keycloak.getKeycloakInstance().token);
  }

  /**
   * Get UUID of devportal client
   */
  private getDevPortalClientUUID() {
    return from(this.kcAdminClient.clients.find())
      .pipe(mergeAll())
      .pipe(single(client => client.clientId === 'devportal'), map(client => client.id))
      .pipe(share()); // fire only once and cache the result for other subscribers
  }

  /**
   * Get the devportaluser user role
   */
  private getDevPortalUserRole() {
    return from(this.kcAdminClient.roles.findOneByName({
      name: 'devportaluser'
    }));
  }

  /**
   * Get API-Mgmt-Devportal-Users Group
   */
  private getDevPortalUserGroup() {
    return from(this.kcAdminClient.groups.find({search: 'API-Mgmt-Devportal-Users'}))
      .pipe(map(groups => groups.length > 0 ? groups[0] : undefined));
  }

  /**
   * Get all keycloak users
   */
  public getAllUsers() {
    return from(this.kcAdminClient.users.find());
  }

  /**
   * Searchs a user from keycloak
   * @param username the keycloak username
   */
  public findUser(username) {
    return from(this.kcAdminClient.users.find({username}));
  }

  /**
   * Create user
   * @param userToCreate the user to create
   */
  private createUser(userToCreate) {
    return from(this.kcAdminClient.users.create({
      firstName: userToCreate.firstName,
      lastName: userToCreate.lastName,
      email: userToCreate.email,
      username: userToCreate.username,
      enabled: true,
      attributes: {generatedFromDevPortal: ['true']}
    }));
  }

  /**
   * Delete user by id
   * @param userId the user id
   */
  private deleteUserById(userId) {
    return from(this.kcAdminClient.users.del({
      id: userId
    }));
  }

  /**
   * Set user password
   * @param userId the user id
   * @param password the user password
   */
  public setUserPassword(userId, password) {
    return from(this.kcAdminClient.users.resetPassword({
        id: userId,
        credential: {
          temporary: true,
          type: 'password',
          value: password,
        },
      }));
  }

  /**
   * Create client role
   * @param clientRoleName the client role name
   */
  public createClientRole(clientRoleName) {
    return this.getDevPortalClientUUID()
      .pipe(map(clientUUID => this.kcAdminClient.clients.createRole({
        id: clientUUID,
        name: clientRoleName
      })));
  }

  /**
   * Delete client role
   * @param clientRoleName the client role name
   */
  public deleteClientRole(clientRoleName) {
    return this.getDevPortalClientUUID()
      .pipe(mergeMap(clientId => this.kcAdminClient.clients.delRole({id: clientId, roleName: clientRoleName})));
  }

  /**
   * Get client role UUID
   * @param clientUUID the client UUID
   * @param clientRoleName the client role name
   */
  public getClientRoleUUID(clientUUID, clientRoleName) {
    return from(this.kcAdminClient.clients.findRole({
      id: clientUUID,
      roleName: clientRoleName
    })).pipe(map(roleObject => roleObject.id));
  }

  /**
   * Add client role mapping to user
   * @param userUUID user UUID
   * @param clientUUID client UUID
   * @param roleUUID role UUID
   * @param roleName role name
   */
  public addClientRoleMappingToUser(userUUID, clientUUID, roleUUID, roleName) {
    return from(this.kcAdminClient.users.addClientRoleMappings({
      id: userUUID,
      clientUniqueId: clientUUID,
      roles: [{
        id: roleUUID,
        name: roleName
      }]
    }));
  }

  /**
   * Add devportal role from user
   * @param user the user
   */
  public addDevPortalRoleToUser(user) {
    return this.getDevPortalUserRole().pipe(mergeMap(devPortalUserRole => this.kcAdminClient.users.addRealmRoleMappings({
        id: user.id,
        roles: [{
          id: devPortalUserRole.id,
          name: devPortalUserRole.name
        }]
      })));
  }

  /**
   * Remove Role Mapping from user
   * @param userUUID user UUID
   * @param roleUUID role UUID
   * @param roleName role name
   */
  private removeRoleMapping(userUUID, roleUUID, roleName) {
    return from(this.kcAdminClient.users.delRealmRoleMappings({
      id: userUUID,
      roles: [{
        id: roleUUID,
        name: roleName
      }]
    }));
  }

  /**
   * Remove Group Mapping from user
   * @param userUUID user UUID
   * @param groupUUID group UUID
   */
  private removeGroupMapping(userUUID, groupUUID) {
    return from(this.kcAdminClient.users.delFromGroup({
      id: userUUID,
      groupId: groupUUID
    }));
  }

  /**
   * Remove devportal role from user
   * @param user the user
   */
  public removeDevPortalRoleFromUser(user) {
    return this.getDevPortalUserRole()
      .pipe(mergeMap(devPortalUserRole => this.removeRoleMapping(user.id, devPortalUserRole.id, devPortalUserRole.name)));
  }

  /**
   * Add User to API-Mgmt-Devportal-Users group
   * @param user the user
   */
  public addDevPortalGroupToUser(user) {
    return this.getDevPortalUserGroup().pipe(mergeMap(devPortalUserGroup => this.kcAdminClient.users.addToGroup({
      id: user.id,
      groupId: devPortalUserGroup.id
    })));
  }

  /**
   * Remove User from API-Mgmt-Devportal-Users group
   * @param user the user
   */
  public removeDevPortalGroupFromUser(user) {
    return this.getDevPortalUserGroup()
      .pipe(mergeMap(devPortalUserGroup => this.removeGroupMapping(user.id, devPortalUserGroup.id)))
      // removeGroupMapping returns GroupRepresentation which is not needed
      // and should be skipped because of typing errors of method this.deleteUser:
      .pipe(map((groups) => {}));
  }

  /**
   * Add client role to user
   * @param user the user
   * @param clientRoleName the client role name
   */
  public addClientRoleToUser(user, clientRoleName) {
    return this.createClientRole(clientRoleName)
      .pipe(mergeMap((roleObject) => this.getDevPortalClientUUID()))
      .pipe(mergeMap(clientUUID =>
        this.getClientRoleUUID(clientUUID, clientRoleName)
        .pipe(mergeMap(clientRoleUUID => this.addClientRoleMappingToUser(user.id, clientUUID, clientRoleUUID, clientRoleName))))
      );
  }

  /**
   * Find existing or create new user
   * @param user the user
   */
  public findExistingOrCreateUser(user): Observable<UserRepresentation> {
    return this.findUser(user.username).pipe(mergeMap(foundUsers => {
      let observer = null;
      if (foundUsers.length === 0) {
        observer = this.createUser(user).pipe(mergeMap((insertedUser => {
          return this.setUserPassword(insertedUser.id, user.password).pipe(mergeMap(() => of(insertedUser)));
        })));
      } else {
        observer = of(foundUsers[0]);
      }
      return observer;
    }));
  }

  /**
   * Determines if the user was generated from devportal admin
   * @param user Keycloak user
   */
  private isUserGeneratedFromDevPortal(user: UserRepresentation): boolean {
    return user.attributes
      && user.attributes.generatedFromDevPortal
      && user.attributes.generatedFromDevPortal.length > 0
      && user.attributes.generatedFromDevPortal[0] === 'true';
  }

  /**
   * Delete dev portal user if generated from dev portal if not remove only devportaluser role from user
   * @param username Username of Developer
   */
  public deleteUser(username): Observable<void> {
    return this.findUser(username).pipe(mergeMap(users => {
      if (users.length > 0) {
        const userToDelete = users[0];
        // delete user only if he was generated from dev portal
        if (this.isUserGeneratedFromDevPortal(userToDelete)) {
          return this.deleteUserById(userToDelete.id);
        } else {
          // remove only devportaluser role from user if he was not generated from dev portal
          return this.removeDevPortalGroupFromUser(userToDelete);
        }
      }
    }));
  }
}
