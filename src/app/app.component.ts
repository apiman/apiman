import {Component, ViewEncapsulation} from '@angular/core';
import {SpinnerService} from './services/spinner.service';
import {ToasterConfig} from 'angular2-toaster';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  constructor(public loadingSpinnerService: SpinnerService) {
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
