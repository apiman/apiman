import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditDeveloperComponent } from './edit-developer.component';

describe('EditDeveloperComponent', () => {
  let component: EditDeveloperComponent;
  let fixture: ComponentFixture<EditDeveloperComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditDeveloperComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditDeveloperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
