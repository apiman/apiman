import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Developer} from '../../../services/api-data.service';
import {ClientMappingComponent} from '../create-developer/client-mapping.component';
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

  @ViewChild('clientmapping', {static: false}) clientMapping: ClientMappingComponent;

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
      this.clientMapping.loadClients();
    }, error => {
      const errorMessage = 'Error loading developer';
      console.error(errorMessage, error);
      const errorToast: Toast = {
        type: 'error',
        title: errorMessage,
        body: error.message ? error.message : error.error.message,
        timeout: 0,
        showCloseButton: true
      };
      this.toasterService.pop(errorToast);
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
        //delete developer from cache
        this.developerDataCache.developers
          .splice(this.developerDataCache.developers
            .findIndex((d) => d.id === this.developer.id), 1, this.developer);
        this.loadingSpinnerService.stopWaiting();
        this.router.navigate(['/admin']);
      }, error => {
        const errorMessage = 'Error saving developer';
        console.error(errorMessage, error);
        const errorToast: Toast = {
          type: 'error',
          title: errorMessage,
          body: error.message ? error.message : error.error.message,
          timeout: 0,
          showCloseButton: true
        };
        this.toasterService.pop(errorToast);
        this.loadingSpinnerService.stopWaiting();
      });
    } else {
      this.loadingSpinnerService.stopWaiting();
      this.toasterService.pop({
        type: 'error',
        title: 'Developer is not defined',
        timeout: 0,
        showCloseButton: true
      });
    }
  }

}
