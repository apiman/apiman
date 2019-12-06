import { TestBed } from '@angular/core/testing';

import { KeycloakInteractionService } from './keycloak-interaction.service';

describe('KeycloakInteractionService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: KeycloakInteractionService = TestBed.get(KeycloakInteractionService);
    expect(service).toBeTruthy();
  });
});
