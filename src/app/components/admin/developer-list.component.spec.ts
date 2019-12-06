import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeveloperListComponent } from './developer-list.component';

describe('DeveloperListComponent', () => {
  let component: DeveloperListComponent;
  let fixture: ComponentFixture<DeveloperListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeveloperListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeveloperListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
