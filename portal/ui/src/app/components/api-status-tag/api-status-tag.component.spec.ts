import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApiStatusTagComponent } from './api-status-tag.component';

describe('ApiStatusTagComponent', () => {
  let component: ApiStatusTagComponent;
  let fixture: ComponentFixture<ApiStatusTagComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ApiStatusTagComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ApiStatusTagComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
