import { TestBed } from '@angular/core/testing';

import { TocService } from './toc.service';

describe('TocService', () => {
  let service: TocService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TocService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
