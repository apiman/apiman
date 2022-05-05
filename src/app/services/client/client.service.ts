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
import { catchError } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { EMPTY, Observable, throwError } from 'rxjs';
import { BackendService } from '../backend/backend.service';
import { IAction } from '../../interfaces/ICommunication';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  constructor(private backend: BackendService) {}

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
}
