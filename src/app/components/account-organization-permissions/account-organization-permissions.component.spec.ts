import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountOrganizationPermissionsComponent } from './account-organization-permissions.component';

describe('AccountOrganizationPermissionsComponent', () => {
  let component: AccountOrganizationPermissionsComponent;
  let fixture: ComponentFixture<AccountOrganizationPermissionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AccountOrganizationPermissionsComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccountOrganizationPermissionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
