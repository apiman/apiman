import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Developer } from '../../../services/api-data.service';
import { ClientMappingComponent } from '../create-developer/client-mapping.component';
import { AdminService } from '../services/admin.service';
import { DeveloperDataCacheService } from '../services/developer-data-cache.service';

@Component({
  selector: 'app-edit-developer',
  templateUrl: './edit-developer.component.html',
  styleUrls: ['./edit-developer.component.scss']
})
export class EditDeveloperComponent implements OnInit {

  public developerId;
  private developer: Developer;
  public assignedClients;

  @ViewChild('clientmapping', {static: false}) clientMapping: ClientMappingComponent;

  constructor(private adminService: AdminService, private route: ActivatedRoute, private router: Router, private developerDataCache: DeveloperDataCacheService) { }

  ngOnInit() {
    this.developerId = this.route.snapshot.paramMap.get('developerId');
    this.adminService.getDeveloper(this.developerId).subscribe(developer => {
      this.developer = developer;
      this.assignedClients = developer.clients;
      this.clientMapping.loadClients();
    });
  }

  updateDeveloper() {
    if (this.developer && this.clientMapping.assignedClients.length > 0) {
      return this.adminService.updateDeveloper(this.developer).subscribe(() => {
        this.developerDataCache.developers.splice(this.developerDataCache.developers.findIndex((d) => d.id === this.developer.id), 1, this.developer);
        this.router.navigate(['/admin']);
      });
    }
  }

}
