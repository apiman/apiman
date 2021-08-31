import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketplaceApiDescriptionComponent } from './marketplace-api-description.component';

describe('MarketplaceApiDescriptionComponent', () => {
  let component: MarketplaceApiDescriptionComponent;
  let fixture: ComponentFixture<MarketplaceApiDescriptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MarketplaceApiDescriptionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketplaceApiDescriptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
