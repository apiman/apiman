import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketplaceApiConfirmationComponent } from './marketplace-api-confirmation.component';

describe('MarketplaceApiConfirmationComponent', () => {
  let component: MarketplaceApiConfirmationComponent;
  let fixture: ComponentFixture<MarketplaceApiConfirmationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MarketplaceApiConfirmationComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketplaceApiConfirmationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
