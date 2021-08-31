import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketplaceApiPoliciesComponent } from './marketplace-api-policies.component';

describe('MarketplaceApiPoliciesComponent', () => {
  let component: MarketplaceApiPoliciesComponent;
  let fixture: ComponentFixture<MarketplaceApiPoliciesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MarketplaceApiPoliciesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketplaceApiPoliciesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
