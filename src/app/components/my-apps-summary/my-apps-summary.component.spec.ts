import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyAppsSummaryComponent } from './my-apps-summary.component';

describe('MyAppsSummaryComponent', () => {
  let component: MyAppsSummaryComponent;
  let fixture: ComponentFixture<MyAppsSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyAppsSummaryComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyAppsSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
