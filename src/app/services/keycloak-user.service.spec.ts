import { TestBed } from '@angular/core/testing';

import { KeycloakUserService } from './keycloak-user.service';

describe('KeycloakUserService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: KeycloakUserService = TestBed.get(KeycloakUserService);
    expect(service).toBeTruthy();
  });
});
