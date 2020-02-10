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
  private developer: Developer;
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
