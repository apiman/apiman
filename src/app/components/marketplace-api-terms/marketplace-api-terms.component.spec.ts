import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketplaceApiTermsComponent } from './marketplace-api-terms.component';

describe('MarketplaceApiTermsComponent', () => {
  let component: MarketplaceApiTermsComponent;
  let fixture: ComponentFixture<MarketplaceApiTermsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MarketplaceApiTermsComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketplaceApiTermsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
