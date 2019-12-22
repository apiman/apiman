import {Component, OnInit} from '@angular/core';
import {AdminService} from './services/admin.service';
import {Developer} from '../../services/api-data.service';
import {DeveloperDataCacheService} from './services/developer-data-cache.service';
import {Toast, ToasterService} from 'angular2-toaster';
import {SpinnerService} from '../../services/spinner.service';

@Component({
  selector: 'app-developer-list',
  templateUrl: './developer-list.component.html',
  styleUrls: ['./developer-list.component.scss']
})
export class DeveloperListComponent implements OnInit {

  developers: Array<Developer> = [];

  constructor(private adminService: AdminService,
              private developerDataCache: DeveloperDataCacheService,
              private toasterService: ToasterService,
              private loadingSpinnerService: SpinnerService) {
  }

  /**
   * load data on initialization
   */
  ngOnInit() {
    this.load();
  }

  /**
   * Load developer data
   */
  public load() {
    this.loadingSpinnerService.startWaiting();
    // set data from cache
    this.developers = this.developerDataCache.developers;
    if (!this.developers) {
      this.developerDataCache.load()
        .subscribe(() => {
          // set data from cache
          this.developers = this.developerDataCache.developers;
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
        this.loadingSpinnerService.stopWaiting();
      }, error => {
        const errorMessage = 'Error deleting developer';
        console.error(errorMessage, error);
        this.toasterService.pop('error', errorMessage, error.message);
        this.loadingSpinnerService.stopWaiting();
      });
  }
}
