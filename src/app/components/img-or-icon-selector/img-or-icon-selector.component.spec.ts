import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImgOrIconSelectorComponent } from './img-or-icon-selector.component';

describe('ImgOrIconSelectorComponent', () => {
  let component: ImgOrIconSelectorComponent;
  let fixture: ComponentFixture<ImgOrIconSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ImgOrIconSelectorComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ImgOrIconSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
