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

import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Developer} from '../../../services/api-data.service';
import {AdminService} from '../services/admin.service';
import {DeveloperDataCacheService} from '../services/developer-data-cache.service';
import {Toast, ToasterService} from 'angular2-toaster';
import {SpinnerService} from '../../../services/spinner.service';

@Component({
  selector: 'app-edit-developer',
  templateUrl: './edit-developer.component.html',
  styleUrls: ['./edit-developer.component.scss']
})
export class EditDeveloperComponent implements OnInit {

  public developerId;
  public developer: Developer;
  public assignedClients;

  constructor(private adminService: AdminService,
              private route: ActivatedRoute,
              private router: Router,
              private developerDataCache: DeveloperDataCacheService,
              private toasterService: ToasterService,
              private loadingSpinnerService: SpinnerService) {
  }

  /**
   * Load developer data
   */
  ngOnInit() {
    this.loadingSpinnerService.startWaiting();
    this.developerId = this.route.snapshot.paramMap.get('developerId');
    this.adminService.getDeveloper(this.developerId).subscribe(developer => {
      this.developer = developer;
      this.assignedClients = developer.clients;
      this.loadingSpinnerService.stopWaiting();
    }, error => {
      const errorMessage = 'Error loading developer';
      console.error(errorMessage, error);
      this.toasterService.pop('error', errorMessage, error.message);
      this.loadingSpinnerService.stopWaiting();
    });
  }

  /**
   * Update a developer
   */
  updateDeveloper() {
    this.loadingSpinnerService.startWaiting();
    if (this.developer) {
      return this.adminService.updateDeveloper(this.developer).subscribe(() => {
        // delete developer from cache
        this.developerDataCache.developers
          .splice(this.developerDataCache.developers
            .findIndex((d) => d.id === this.developer.id), 1, this.developer);
        this.loadingSpinnerService.stopWaiting();
        this.router.navigate(['/admin']);
      }, error => {
        const errorMessage = 'Error saving developer';
        console.error(errorMessage, error);
        this.toasterService.pop('error', errorMessage, error.message);
        this.loadingSpinnerService.stopWaiting();
      });
    } else {
      this.loadingSpinnerService.stopWaiting();
      this.toasterService.pop('error', 'Developer is not defined');
    }
  }

}
