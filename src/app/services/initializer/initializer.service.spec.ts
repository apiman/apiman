import { TestBed } from '@angular/core/testing';

import { InitializerService } from './initializer.service';

describe('DevPortalInitializerService', () => {
  let service: InitializerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InitializerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
