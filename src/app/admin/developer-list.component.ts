import { Component, OnInit } from '@angular/core';
import {ApiDataService, ClientMapping, Developer} from '../api-data.service';
import {mergeAll, mergeMap} from 'rxjs/operators';

@Component({
  selector: 'app-developer-list',
  templateUrl: './developer-list.component.html',
  styleUrls: ['./developer-list.component.scss']
})
export class DeveloperListComponent implements OnInit {

  developers: Array<Developer> = [];

  constructor(private apiDataService: ApiDataService) { }

  ngOnInit() {
    this.load();
  }

  public load() {
    console.log('start loading developers');
    this.developers = [];
    this.apiDataService.getAllDevelopers().subscribe((developers) => {
      console.log('Developer', developers);
      this.developers = developers;
    });
  }

  deleteDeveloper(developer: Developer) {
    this.apiDataService.deleteDeveloper(developer).subscribe(response => this.developers.splice(this.developers.indexOf(developer), 1));
  }
}
