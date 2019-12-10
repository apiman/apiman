import {Component, OnInit} from '@angular/core';
import {Toast, ToasterContainerComponent, ToasterService} from 'angular2-toaster';

@Component({
  selector: 'app-not-authorized',
  templateUrl: './not-authorized.component.html',
  styleUrls: ['./not-authorized.component.scss']
})
export class NotAuthorizedComponent implements OnInit {

  constructor(private toasterService: ToasterService) {
  }

  ngOnInit() {
    const toast: Toast = {
      type: 'warning',
      body: 'You are not authorized to see this page.',
      timeout: 0,
      showCloseButton: true
    };
    this.toasterService.pop(toast);
  }

}
