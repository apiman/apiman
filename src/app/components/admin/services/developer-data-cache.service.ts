import {Injectable} from '@angular/core';
import {Developer} from '../../../services/api-data.service';
import {AdminService} from './admin.service';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class DeveloperDataCacheService {

  public developers: Array<Developer>;

  constructor(private adminService: AdminService) {
    if (!this.developers) {
      this.load().subscribe();
    }
  }

  public load() {
    return this.adminService.getAllDevelopers()
      .pipe(map(developers => {
        // set data to cache
        this.developers = developers;
        console.log('set developer list cache', developers);
      }));
  }
}
