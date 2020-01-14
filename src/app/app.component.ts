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
