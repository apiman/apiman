import {Component, OnInit, Input, Output} from '@angular/core';
import {CdkDragDrop, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {ApiDataService, ClientBean, ClientMapping} from '../../../services/api-data.service';
import {map} from 'rxjs/operators';
import {AdminService} from '../services/admin.service';
import {Toast, ToasterService} from 'angular2-toaster';
import {SpinnerService} from '../../../services/spinner.service';

@Component({
  selector: 'app-client-mapping',
  templateUrl: './client-mapping.component.html',
  styleUrls: ['./client-mapping.component.scss']
})
export class ClientMappingComponent implements OnInit {

  constructor(private adminService: AdminService,
              private toasterService: ToasterService,
              private loadingSpinnerService: SpinnerService) {
  }

  availableClients: Array<ClientMapping> = [];

  @Input('assignedClients') assignedClients: Array<ClientMapping> = [];

  ngOnInit() {
    this.loadClients();
  }

  loadClients() {
    this.loadingSpinnerService.startWaiting();
    const loadedAvailableClients = [];
    this.adminService.getAllClients().pipe(map(client => {
      if (this.assignedClients && !this.assignedClients.find((c => c.organizationId === client.organizationId && c.clientId === client.id))) {
        loadedAvailableClients.push({
          clientId: client.id,
          organizationId: client.organizationId
        });
      }
    })).subscribe(ready => {
      this.availableClients = loadedAvailableClients;
      this.loadingSpinnerService.stopWaiting();
    }, error => {
      const errorMessage = 'Error loading clients';
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

  reset() {
    this.loadClients();
    this.assignedClients = [];
  }

  drop(event: CdkDragDrop<ClientMapping[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex);
    }
  }

}
