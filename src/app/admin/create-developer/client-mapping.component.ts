import {Component, OnInit, Output} from '@angular/core';
import {CdkDragDrop, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {ApiDataService, ClientBean} from '../../api-data.service';
import {mergeAll} from 'rxjs/operators';

@Component({
  selector: 'app-client-mapping',
  templateUrl: './client-mapping.component.html',
  styleUrls: ['./client-mapping.component.scss']
})
export class ClientMappingComponent implements OnInit {

  constructor(private apiDataService: ApiDataService) { }

  availableClients: Array<ClientBean> = [];

  @Output('assignedClients') assignedClients: Array<ClientBean> = [];

  ngOnInit() {
    this.loadClients();
  }

  loadClients() {
    this.availableClients = [];
    this.apiDataService.getAllClients().subscribe(client => {
      this.availableClients.push(client);
    });
  }

  reset() {
    this.loadClients();
    this.assignedClients = [];
  }

  drop(event: CdkDragDrop<ClientBean[]>) {
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
