import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountIdmComponent } from './account-idm.component';

describe('AccountIdmComponent', () => {
  let component: AccountIdmComponent;
  let fixture: ComponentFixture<AccountIdmComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AccountIdmComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccountIdmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
