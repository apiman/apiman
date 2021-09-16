import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApiCardListComponent } from './api-card-list.component';

describe('ApiCardListComponent', () => {
  let component: ApiCardListComponent;
  let fixture: ComponentFixture<ApiCardListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ApiCardListComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ApiCardListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
