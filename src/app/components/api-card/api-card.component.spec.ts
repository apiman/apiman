import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApiCardComponent } from './api-card.component';

describe('ApiCardComponent', () => {
  let component: ApiCardComponent;
  let fixture: ComponentFixture<ApiCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ApiCardComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ApiCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
