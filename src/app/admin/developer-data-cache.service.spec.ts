import { TestBed } from '@angular/core/testing';

import { DeveloperDataCacheService } from './developer-data-cache.service';

describe('DeveloperDataCacheService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: DeveloperDataCacheService = TestBed.get(DeveloperDataCacheService);
    expect(service).toBeTruthy();
  });
});
