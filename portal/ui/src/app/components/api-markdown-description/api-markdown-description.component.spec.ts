import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApiMarkdownDescriptionComponent } from './api-markdown-description.component';

describe('ApiMarkdownDescriptionComponent', () => {
  let component: ApiMarkdownDescriptionComponent;
  let fixture: ComponentFixture<ApiMarkdownDescriptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ApiMarkdownDescriptionComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ApiMarkdownDescriptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
