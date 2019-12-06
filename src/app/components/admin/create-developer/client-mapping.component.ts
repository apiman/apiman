import { Component, OnInit, Input, Output } from '@angular/core';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ApiDataService, ClientBean, ClientMapping } from '../../../services/api-data.service';
import { map } from 'rxjs/operators';
import { AdminService } from '../services/admin.service';

@Component({
  selector: 'app-client-mapping',
  templateUrl: './client-mapping.component.html',
  styleUrls: ['./client-mapping.component.scss']
})
export class ClientMappingComponent implements OnInit {

  constructor(private adminService: AdminService) { }

  availableClients: Array<ClientMapping> = [];

  @Input('assignedClients') assignedClients: Array<ClientMapping> = [];

  ngOnInit() {
    this.loadClients();
  }

  loadClients() {
    const loadedAvailableClients = [];
    this.adminService.getAllClients().pipe(map(client => {
      if (this.assignedClients && !this.assignedClients.find((c => c.organizationId === client.organizationId && c.clientId === client.id))) {
        loadedAvailableClients.push({
          clientId: client.id,
          organizationId: client.organizationId
        });
      }
    })).subscribe(ready => this.availableClients = loadedAvailableClients);
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
