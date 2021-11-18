import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyAppsManageApiComponent } from './my-apps-manage-api.component';

describe('MyAppsManageApiComponent', () => {
  let component: MyAppsManageApiComponent;
  let fixture: ComponentFixture<MyAppsManageApiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyAppsManageApiComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyAppsManageApiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
