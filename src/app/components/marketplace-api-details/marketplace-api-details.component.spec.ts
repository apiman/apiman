import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketplaceApiDetailsComponent } from './marketplace-api-details.component';

describe('MarketplaceApiDetailsComponent', () => {
  let component: MarketplaceApiDetailsComponent;
  let fixture: ComponentFixture<MarketplaceApiDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MarketplaceApiDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketplaceApiDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
