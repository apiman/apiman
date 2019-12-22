import {Component, Input, OnInit} from '@angular/core';
import {CdkDragDrop, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {ClientMapping} from '../../../services/api-data.service';
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

  /**
   * Load clients on initialization
   */
  ngOnInit() {
    this.loadClients();
  }

  /**
   * Load available clients
   */
  loadClients() {
    this.loadingSpinnerService.startWaiting();
    const loadedAvailableClients = [];
    this.adminService.getAllClients().pipe(map(client => {
      if (this.assignedClients
        && !this.assignedClients.find((c => c.organizationId === client.organizationId && c.clientId === client.id))) {
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
      this.toasterService.pop('error', errorMessage, error.message);
      this.loadingSpinnerService.stopWaiting();
    });
  }

  /**
   * Reset the client list including assigned clients
   */
  reset() {
    this.loadClients();
    this.assignedClients = [];
  }

  /**
   * Drop event for adding a client
   * @param event The Drag&Drop event
   */
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
