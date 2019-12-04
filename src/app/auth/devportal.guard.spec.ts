import { TestBed, async, inject } from '@angular/core/testing';

import { DevportalGuard } from './devportal.guard';

describe('DevportalGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DevportalGuard]
    });
  });

  it('should ...', inject([DevportalGuard], (guard: DevportalGuard) => {
    expect(guard).toBeTruthy();
  }));
});
