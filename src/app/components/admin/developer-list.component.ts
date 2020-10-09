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
import {AdminService} from './services/admin.service';
import {Developer} from '../../services/api-data.service';
import {DeveloperDataCacheService} from './services/developer-data-cache.service';
import {Toast, ToasterService} from 'angular2-toaster';
import {SpinnerService} from '../../services/spinner.service';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';

@Component({
  selector: 'app-developer-list',
  templateUrl: './developer-list.component.html',
  styleUrls: ['./developer-list.component.scss']
})

export class DeveloperListComponent implements OnInit {

  developers: Array<Developer> = [];
  displayedColumns: string[] = ['id', 'clients', 'options'];
  dataSource = new MatTableDataSource<Developer>(this.developers);

  constructor(private adminService: AdminService,
              private developerDataCache: DeveloperDataCacheService,
              private toasterService: ToasterService,
              private loadingSpinnerService: SpinnerService) {
  }

  // Fix sorting if table has *ngIf attribute
  // https://github.com/angular/components/issues/15008#issuecomment-516386055
  @ViewChild(MatSort, {static: false}) set content(sort: MatSort) {
    this.dataSource.sort = sort;
  }

  /**
   * load data on initialization
   */
  ngOnInit() {
    this.load();
  }

  refreshTable() {
    // Reload data table as recommend here in step 2:
    // https://github.com/angular/components/issues/15972#issuecomment-490235603
    this.dataSource.data = this.developers;
  }

  /**
   * Load developer data
   */
  public load() {
    this.loadingSpinnerService.startWaiting();
    // set data from cache
    this.developers = this.developerDataCache.developers;
    this.refreshTable();
    if (!this.developers) {
      this.developerDataCache.load()
        .subscribe(() => {
          // set data from cache
          this.developers = this.developerDataCache.developers;
          this.refreshTable();
          this.loadingSpinnerService.stopWaiting();
        }, (httpErrorResponse) => {
              const errorMessage = 'Error loading developer list';
              console.error(errorMessage, httpErrorResponse);
              this.toasterService.pop('error', errorMessage, httpErrorResponse.message);
              this.loadingSpinnerService.stopWaiting();
            });
    } else {
      this.loadingSpinnerService.stopWaiting();
    }
  }

  /**
   * Delete a developer
   * @param developer the developer
   */
  deleteDeveloper(developer: Developer) {
    this.loadingSpinnerService.startWaiting();
    this.adminService.deleteDeveloper(developer)
      .subscribe(response => {
        this.developers.splice(this.developers.indexOf(developer), 1);
        this.refreshTable();
        this.loadingSpinnerService.stopWaiting();
      }, error => {
        const errorMessage = 'Error deleting developer';
        console.error(errorMessage, error);
        this.toasterService.pop('error', errorMessage, error.message);
        this.loadingSpinnerService.stopWaiting();
      });
  }
}
