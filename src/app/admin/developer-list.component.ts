import { Component, OnInit } from '@angular/core';
import { AdminService } from './admin.service';
import { Developer } from '../api-data.service';
import { DeveloperDataCacheService } from './developer-data-cache.service';

@Component({
  selector: 'app-developer-list',
  templateUrl: './developer-list.component.html',
  styleUrls: ['./developer-list.component.scss']
})
export class DeveloperListComponent implements OnInit {

  developers: Array<Developer> = [];

  constructor(private adminService: AdminService, private developerDataCache: DeveloperDataCacheService) { }

  ngOnInit() {
    this.load();
  }

  public load() {
    // set data from cache
    this.developers = this.developerDataCache.developers;
    if (!this.developers) {
      this.adminService.getAllDevelopers().subscribe((developers) => {
        // set data to cache
        this.developerDataCache.developers = developers;
        // set data from cache
        this.developers = this.developerDataCache.developers;
      });
    }
  }

  deleteDeveloper(developer: Developer) {
    this.adminService.deleteDeveloper(developer).subscribe(response => this.developers.splice(this.developers.indexOf(developer), 1));
  }
}
