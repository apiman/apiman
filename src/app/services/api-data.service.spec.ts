import { TestBed } from '@angular/core/testing';

import { ApiDataService } from './api-data.service';
import {KeycloakService} from 'keycloak-angular';
import {HttpClient} from '@angular/common/http';

describe('ApiDataService', () => {
  const httpClient = jasmine.createSpy('HttpClient');

  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useValue: httpClient},
      { provide: 'API_MGMT_UI_REST_URL', useValue: 'https://dummyURL.com/apiman' }
    ]
  }));

  it('should be created', () => {
    const service: ApiDataService = TestBed.get(ApiDataService);
    expect(service).toBeTruthy();
  });
});
