import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketplaceClientAppComponent } from './marketplace-client-app.component';

describe('MarketplaceClientAppComponent', () => {
  let component: MarketplaceClientAppComponent;
  let fixture: ComponentFixture<MarketplaceClientAppComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MarketplaceClientAppComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketplaceClientAppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
