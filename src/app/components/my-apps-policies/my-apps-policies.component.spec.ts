import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyAppsPoliciesComponent } from './my-apps-policies.component';

describe('MyAppsPoliciesComponent', () => {
  let component: MyAppsPoliciesComponent;
  let fixture: ComponentFixture<MyAppsPoliciesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyAppsPoliciesComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyAppsPoliciesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
