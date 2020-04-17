import {Inject, Injectable} from '@angular/core';
import {KeycloakService} from 'keycloak-angular';
import KcAdminClient from 'keycloak-admin';
import {from} from 'rxjs';
import {map, mergeMap} from 'rxjs/operators';
import {TokenService} from '../../../services/token.service';

@Injectable({
  providedIn: 'root'
})
export class KeycloakInteractionService {

  private kcAdminClient: KcAdminClient;

  private keycloakGroupUsers = 'API-Mgmt-Devportal-Users';

  /**
   * Constructor of Keycloak Interaction Service
   * @param keycloak the keycloak service
   * @param keycloakRestUrl the keycloak rest url
   */
  constructor(private keycloak: KeycloakService,
              private tokenService: TokenService,
              @Inject('KEYCLOAK_AUTH_URL') private keycloakRestUrl: string,
              @Inject('API_MGTM_REALM') private apiMgmtRealm: string) {
    this.kcAdminClient = new KcAdminClient({
      baseUrl: this.keycloakRestUrl,
      realmName: this.apiMgmtRealm
    });
    // set initial token
    const token = keycloak.getKeycloakInstance().token;
    this.kcAdminClient.setAccessToken(token);
    console.log('set token to admin library');

    this.tokenService.getTokens()
      .subscribe(tokens => {
        this.kcAdminClient.setAccessToken(tokens.token);
        console.log('set token to admin library');
      });
  }

  /**
   * Get API-Mgmt-Devportal-Users Group
   */
  private getDevPortalUserGroup() {
    return from(this.kcAdminClient.groups.find({search: this.keycloakGroupUsers}))
      .pipe(map(groups => groups.length > 0 ? groups[0] : undefined));
  }

  /**
   * Get all keycloak users
   * Max value: https://www.keycloak.org/docs-api/9.0/rest-api/index.html#_users_resource
   */
  public getAllUsers() {
    return from(this.kcAdminClient.users.find({max: (Math.pow(2, 31) - 1)}));
  }

  /**
   * Remove Group Mapping from user
   * @param userUUID user UUID
   * @param groupUUID group UUID
   */
  private removeGroupMapping(userUUID: string, groupUUID: string) {
    return from(this.kcAdminClient.users.delFromGroup({
      id: userUUID,
      groupId: groupUUID
    }));
  }

  /**
   * Add User to API-Mgmt-Devportal-Users group
   * @param userId the user id
   */
  public addDevPortalGroupToUser(userId: string) {
    return this.getDevPortalUserGroup().pipe(mergeMap(devPortalUserGroup => this.kcAdminClient.users.addToGroup({
      id: userId,
      groupId: devPortalUserGroup.id
    })));
  }

  // TODO REMOVE
  /**
   * Remove User from API-Mgmt-Devportal-Users group
   * @param userId the user id
   */
  public removeDevPortalGroupFromUser(userId: string) {
    return this.getDevPortalUserGroup()
      .pipe(mergeMap(devPortalUserGroup => this.removeGroupMapping(userId, devPortalUserGroup.id)))
      // removeGroupMapping returns GroupRepresentation which is not needed
      // and should be skipped because of typing errors of method this.deleteUser:
      .pipe(map((groups) => {}));
  }
}
