import {Injectable} from '@angular/core';
import {Developer} from '../../../services/api-data.service';
import {AdminService} from './admin.service';

@Injectable({
  providedIn: 'root'
})
export class DeveloperDataCacheService {

  public developers: Array<Developer>;

  constructor(private adminService: AdminService) {
    if (!this.developers) {
      this.adminService.getAllDevelopers().subscribe((developers) => {
        // set data to cache
        this.developers = developers;
        console.log('set developer list cache', developers);
      });
    }
  }
}
