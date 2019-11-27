import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ApiDataService, Developer} from '../../api-data.service';
import { ClientMappingComponent } from '../create-developer/client-mapping.component';
import {timeInterval} from 'rxjs/operators';

@Component({
  selector: 'app-edit-developer',
  templateUrl: './edit-developer.component.html',
  styleUrls: ['./edit-developer.component.scss']
})
export class EditDeveloperComponent implements OnInit {

  private developerId;
  private developer: Developer;
  public assignedClients;

  @ViewChild('clientmapping', {static: false}) clientMapping: ClientMappingComponent;

  constructor(private apiDataService: ApiDataService, private route: ActivatedRoute, private router: Router) { }

  ngOnInit() {
    this.developerId = this.route.snapshot.paramMap.get('developerId');
    this.apiDataService.getDeveloper(this.developerId).subscribe(developer => {
      this.developer = developer;
      this.assignedClients = developer.clients;
      this.clientMapping.loadClients();
    });
  }

  updateDeveloper() {
    if (this.developer && this.clientMapping.assignedClients.length > 0) {
      return this.apiDataService.updateDeveloper(this.developer).subscribe(() => {
        console.log('Update Developer done');
        setTimeout(() => this.router.navigate(['/admin']), 500);
      });
    }
  }

}
