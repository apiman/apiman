import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyAppsUseApiComponent } from './my-apps-use-api.component';

describe('MyAppsUseApiComponent', () => {
  let component: MyAppsUseApiComponent;
  let fixture: ComponentFixture<MyAppsUseApiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyAppsUseApiComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyAppsUseApiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
