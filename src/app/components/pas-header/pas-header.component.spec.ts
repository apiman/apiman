import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PasHeaderComponent } from './pas-header.component';

describe('PasHeaderComponent', () => {
  let component: PasHeaderComponent;
  let fixture: ComponentFixture<PasHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PasHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PasHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
