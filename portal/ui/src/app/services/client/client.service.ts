/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

import { Injectable } from '@angular/core';
import { catchError, defaultIfEmpty, map, switchMap } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { EMPTY, forkJoin, iif, Observable, of, throwError } from 'rxjs';
import { BackendService } from '../backend/backend.service';
import {
  IAction,
  IClientSummary,
  IClientVersion,
  IClientVersionSummary,
  IPermission
} from '../../interfaces/ICommunication';
import { PermissionsService } from '../permissions/permissions.service';
import { IClientVersionExt } from '../../interfaces/IClientVersionSummaryExt';
import { SnackbarService } from '../snackbar/snackbar.service';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  constructor(
    private backend: BackendService,
    private permissionsService: PermissionsService,
    private snackbarService: SnackbarService,
    private translator: TranslateService
  ) {}

  /**
   * This method registers a client, if we got an InvalidContractStatusException which can happen
   * if one API needs approval we will do nothing as this is okay
   * @param action - the action with the client to register
   */
  public registerClient(action: IAction): Observable<void> {
    return this.backend.sendAction(action).pipe(
      catchError((err: HttpErrorResponse) => {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
        if (err.error.type === 'InvalidContractStatusException') {
          // Ignore this error because this is still valid
          return EMPTY;
        } else {
          return throwError(() => err);
        }
      })
    );
  }

  private getClientAdminOrgs() {
    return this.permissionsService.getAllowedOrganizations({
      name: 'clientAdmin'
    } as IPermission);
  }

  public isRegisterable(clientVersion: IClientVersion): boolean {
    const clientAdminOrganizations = this.getClientAdminOrgs();
    return (
      (clientVersion.status === 'Ready' ||
        clientVersion.status === 'Unregistered' ||
        clientVersion.status === 'Retired') &&
      clientAdminOrganizations.includes(clientVersion.client.organization.id)
    );
  }

  public isDeleteAllowed(clientVersion: IClientVersion): boolean {
    const clientAdminOrganizations =
      this.permissionsService.getAllowedOrganizations({
        name: 'clientAdmin'
      } as IPermission);
    return clientAdminOrganizations.includes(
      clientVersion.client.organization.id
    );
  }

  public getClientSummaries(): Observable<IClientSummary[]> {
    return forkJoin([
      this.backend.getEditableClients(),
      this.backend.getViewableClients()
    ]).pipe(
      map((nestedClientSummaries: IClientSummary[][]) => {
        const clientSummaries = nestedClientSummaries.flat();
        return this.getUniqueClients(clientSummaries);
      }),
      defaultIfEmpty([])
    );
  }

  private getUniqueClients(
    clientSummaries: IClientSummary[]
  ): IClientSummary[] {
    return [
      ...new Map(
        clientSummaries.map((clientSummary: IClientSummary) => [
          clientSummary.organizationId + clientSummary.id,
          clientSummary
        ])
      ).values()
    ];
  }

  public getClientVersionSummaries(
    clientSummaries: IClientSummary[]
  ): Observable<IClientVersionSummary[]> {
    return forkJoin(
      clientSummaries.map((clientSummary: IClientSummary) => {
        return this.backend.getClientVersionSummaries(
          clientSummary.organizationId,
          clientSummary.id
        );
      })
    ).pipe(
      defaultIfEmpty([]),
      map((nestedClientVersionSummaries: IClientVersionSummary[][]) => {
        return nestedClientVersionSummaries.flat();
      })
    );
  }

  public getClientVersions(): Observable<IClientVersion[]> {
    return this.getClientSummaries().pipe(
      switchMap((clientSummaries: IClientSummary[]) => {
        return this.getClientVersionSummaries(clientSummaries);
      }),
      switchMap((clientVersionSummaries: IClientVersionSummary[]) => {
        return forkJoin(
          clientVersionSummaries.map(
            (clientVersionSummary: IClientVersionSummary) => {
              return this.backend.getClientVersion(
                clientVersionSummary.organizationId,
                clientVersionSummary.id,
                clientVersionSummary.version
              );
            }
          )
        ).pipe(defaultIfEmpty([]));
      })
    );
  }

  public getExtendedClientVersions(): Observable<IClientVersionExt[]> {
    return this.getClientVersions().pipe(
      map((clientVersions: IClientVersion[]) => {
        return this.extendClientVersions(clientVersions).sort((a, b) => {
          return a.client.name
            .toLowerCase()
            .localeCompare(b.client.name.toLowerCase());
        });
      })
    );
  }

  public extendClientVersions(
    clientVersions: IClientVersion[]
  ): IClientVersionExt[] {
    return clientVersions.map((clientVersion: IClientVersion) => {
      return {
        ...clientVersion,
        deletable: this.isDeleteAllowed(clientVersion),
        registerable: this.isRegisterable(clientVersion)
      } as IClientVersionExt;
    });
  }

  /**
   * This method deletes and optionally unregisters a client.
   * If the client is in the "Registered" or in the "AwaitingApproval" state, we unregister the client before deletion.
   */
  public deleteClient(
    extendedClientVersion: IClientVersionExt | IClientVersion
  ): Observable<void> {
    const action: IAction = {
      type: 'unregisterClient',
      organizationId: extendedClientVersion.client.organization.id,
      entityId: extendedClientVersion.client.id,
      entityVersion: extendedClientVersion.version
    };

    return iif(
      () =>
        extendedClientVersion.status === 'Registered' ||
        extendedClientVersion.status === 'AwaitingApproval',
      this.backend.sendAction(action),
      of(void 0)
    ).pipe(
      switchMap(() =>
        this.backend.deleteClient(action.organizationId, action.entityId)
      ),
      catchError((err) => {
        console.error('Deleting client failed: ', err);
        this.snackbarService.showErrorSnackBar(
          this.translator.instant('CLIENTS.DELETE_CLIENT_FAILED') as string
        );
        return EMPTY;
      })
    );
  }
}
