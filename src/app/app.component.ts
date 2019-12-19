import {Component, ViewEncapsulation} from '@angular/core';
import {SpinnerService} from './services/spinner.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  constructor(public loadingSpinnerService: SpinnerService) {
  }

}
