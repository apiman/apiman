import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyAppsComponent } from './my-apps.component';

describe('MyAppsComponent', () => {
  let component: MyAppsComponent;
  let fixture: ComponentFixture<MyAppsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyAppsComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyAppsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
