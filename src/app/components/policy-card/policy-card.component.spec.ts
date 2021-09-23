import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PolicyCardComponent } from './policy-card.component';

describe('PolicyCardComponent', () => {
  let component: PolicyCardComponent;
  let fixture: ComponentFixture<PolicyCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PolicyCardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PolicyCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
