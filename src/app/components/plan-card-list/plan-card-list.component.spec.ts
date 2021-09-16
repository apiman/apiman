import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanCardListComponent } from './plan-card-list.component';

describe('PlanCardListComponent', () => {
  let component: PlanCardListComponent;
  let fixture: ComponentFixture<PlanCardListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PlanCardListComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PlanCardListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
