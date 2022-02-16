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

import { Component, EventEmitter } from '@angular/core';
import { IContractExt } from '../../../interfaces/IContractExt';
import { IAction } from '../../../interfaces/ICommunication';
import { BackendService } from '../../../services/backend/backend.service';
import { catchError, switchMap } from 'rxjs/operators';
import { EMPTY, iif, of } from 'rxjs';
import { SnackbarService } from '../../../services/snackbar/snackbar.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-unregister-client',
  templateUrl: './unregister-client.component.html',
  styleUrls: ['./unregister-client.component.scss']
})
export class UnregisterClientComponent {
  contract!: IContractExt;
  clientNameVersion = { value: '' };

  unregisterEmitter = new EventEmitter();

  constructor(
    private backend: BackendService,
    private snackbarService: SnackbarService,
    private translator: TranslateService
  ) {}

  onUnregister(): void {
    const action: IAction = {
      type: 'unregisterClient',
      organizationId: this.contract.client.client.organization.id,
      entityId: this.contract.client.client.id,
      entityVersion: this.contract.client.version
    };

    this.backend
      .breakAllContracts(
        action.organizationId,
        action.entityId,
        action.entityVersion
      )
      .pipe(
        switchMap(() =>
          iif(
            () => this.contract.client.status === 'Registered',
            this.backend.sendAction(action),
            of(void 0)
          )
        ),
        switchMap(() =>
          this.backend.deleteClient(action.organizationId, action.entityId)
        ),
        catchError((err) => {
          console.error('Deleting client failed: ', err);
          this.snackbarService.showErrorSnackBar(
            this.translator.instant('CLIENTS.REMOVE_CLIENT_FAILED') as string
          );
          return EMPTY;
        })
      )
      .subscribe(() => {
        this.unregisterEmitter.emit();
      });
  }
}
