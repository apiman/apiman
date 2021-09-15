import { TestBed } from '@angular/core/testing';

import { KeycloakHelperService } from './keycloak-helper.service';

describe('KeycloakHelperService', () => {
  let service: KeycloakHelperService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KeycloakHelperService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
