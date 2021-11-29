/*
 * Copyright 2021 Scheer PAS Schweiz AG
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

@Injectable({
  providedIn: 'root'
})
export class SpinnerService {
  public waiting = false;

  constructor() {}

  /**
   * Start waiting and show spinner
   */
  public startWaiting(): void {
    this.setWaiting(true);
  }

  /**
   * Stop waiting and hide spinner
   */
  public stopWaiting(): void {
    this.setWaiting(false);
  }

  /**
   * Because of change detection we need a timeout, otherwise console would show several errors
   * @param waiting
   */
  setWaiting(waiting: boolean): void {
    setTimeout(() => {
      this.waiting = waiting;
    });
  }
}
