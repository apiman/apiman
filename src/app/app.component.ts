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

import {Component} from '@angular/core';
import {SpinnerService} from './services/spinner.service';
import {ToasterConfig, ToasterService} from 'angular2-toaster';
import {NavigationEnd, Router} from '@angular/router';
import {filter} from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  /**
   * Clear Toast messages on navigation end event
   * @param loadingSpinnerService the loading spinner service
   * @param router the router
   * @param toasterService the toaster service
   */
  constructor(public loadingSpinnerService: SpinnerService, private router: Router, private toasterService: ToasterService) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => this.toasterService.clear());
  }

  /**
   * default toast message options
   */
  public toasterConfig: ToasterConfig = new ToasterConfig({
    timeout: 0,
    showCloseButton: true,
    tapToDismiss: false
  });

}
