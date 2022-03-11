import { Component, Input, OnInit } from '@angular/core';
import { apiStatus } from '../../models/api-status-enum';

@Component({
  selector: 'app-api-status-tag',
  templateUrl: './api-status-tag.component.html',
  styleUrls: ['./api-status-tag.component.scss']
})
export class ApiStatusTagComponent implements OnInit {
  @Input() apiStatus = '';
  @Input() primary = false;
  icon = '';
  class = '';
  constructor() {}

  ngOnInit() {
    switch (this.apiStatus) {
      case apiStatus.published:
        this.icon = 'check_circle';
        this.class = 'green-icon';
        break;
      case apiStatus.retired:
        this.icon = 'remove_circle';
        this.class = 'dark-warn-icon';
        break;
      case apiStatus.ready:
        this.icon = 'schedule';
        this.class = 'dark-accent-icon';
        break;
    }
  }
}
