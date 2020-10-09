/*
 * Copyright 2020 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Injectable } from '@angular/core';
import {Subject} from 'rxjs';
import {Token} from '@angular/compiler';

export interface Tokens {
  token: string;
  refreshToken: string;
}

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  private tokenSubject = new Subject<Tokens>();

  constructor() { }

  /**
   * Set token to subject
   * @param token the token
   */
  public setTokens(token: string, refreshToken: string) {
    console.log('got fresh tokens');
    this.tokenSubject.next({
      token,
      refreshToken
    });
  }

  /**
   * Get token from subject
   */
  public getTokens() {
    return this.tokenSubject.asObservable();
  }
}
