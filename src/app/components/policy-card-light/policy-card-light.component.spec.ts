import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PolicyCardLightComponent } from './policy-card-light.component';

describe('PolicyCardLightComponent', () => {
  let component: PolicyCardLightComponent;
  let fixture: ComponentFixture<PolicyCardLightComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PolicyCardLightComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PolicyCardLightComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
