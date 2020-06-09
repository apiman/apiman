import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientMappingComponent } from './client-mapping.component';

describe('ClientMappingComponent', () => {
  let component: ClientMappingComponent;
  let fixture: ComponentFixture<ClientMappingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ClientMappingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ClientMappingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
