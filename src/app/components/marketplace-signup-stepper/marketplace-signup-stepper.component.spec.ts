import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketplaceSignupStepperComponent } from './marketplace-signup-stepper.component';

describe('MarketplaceSignupStepperComponent', () => {
  let component: MarketplaceSignupStepperComponent;
  let fixture: ComponentFixture<MarketplaceSignupStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MarketplaceSignupStepperComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketplaceSignupStepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
