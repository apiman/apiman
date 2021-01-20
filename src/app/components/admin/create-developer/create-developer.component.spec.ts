/*
 * Copyright 2020 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateDeveloperComponent } from './create-developer.component';
import {AdminService} from '../services/admin.service';
import {Router} from '@angular/router';
import {DeveloperDataCacheService} from '../services/developer-data-cache.service';
import {ToasterService} from 'angular2-toaster';
import {SpinnerService} from '../../../services/spinner.service';
import {ReactiveFormsModule} from '@angular/forms';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {ClientMappingComponent} from './client-mapping.component';
import {MatIconModule} from '@angular/material/icon';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {from} from 'rxjs';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('CreateDeveloperComponent', () => {
  let component: CreateDeveloperComponent;
  let fixture: ComponentFixture<CreateDeveloperComponent>;

  const keycloakUsers =     [{
    id: 'cf79aa9e-6443-4d1b-99cb-a743d335b2b7',
    username: 'admin',
    enabled: false,
    totp: false,
    emailVerified: false,
    firstName: 'apiman',
    lastName: 'admin',
    email: 'admin@example.org',
    disableableCredentialTypes: [],
    requiredActions: [],
    notBefore: 1547455742,
    access: {
      manageGroupMembership: true,
      view: true,
      mapRoles: true,
      impersonate: false,
      manage: true
    }
  }];

  const adminService = jasmine.createSpyObj('adminService', ['getDevPortalUsers', 'getAllClients']);
  adminService.getDevPortalUsers.and.returnValue(from(
    keycloakUsers
  ));
  adminService.getAllClients.and.returnValue(from(
    [
      {
        organizationId: 'Production',
        organizationName: 'Production',
        id: 'Support-Customer',
        name: 'Support-Customer',
        description: null,
        numContracts: 0
      },
      {
        organizationId: 'Testing',
        organizationName: 'Testing',
        id: 'SupportCustomer',
        name: 'SupportCustomer',
        description: null,
        numContracts: 0
      },
      {
        organizationId: 'Test',
        organizationName: 'Test',
        id: 'GitHub',
        name: 'GitHub',
        description: null,
        numContracts: 0
      }
    ]
  ));

  const router = jasmine.createSpy('router');
  const developerDataCache = jasmine.createSpyObj('developerDataCache', ['load', 'developers']);

  const toasterService = jasmine.createSpyObj('toasterService', ['pop']);
  const loadingSpinnerService = jasmine.createSpyObj('loadingSpinnerService', ['startWaiting', 'stopWaiting']);

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateDeveloperComponent, ClientMappingComponent ],
      providers: [
        { provide: AdminService, useValue: adminService },
        { provide: Router, useValue: router },
        { provide: DeveloperDataCacheService, useValue: developerDataCache },
        { provide: ToasterService, useValue: toasterService },
        { provide: SpinnerService, useValue: loadingSpinnerService }
      ],
      imports: [
        ReactiveFormsModule,
        MatAutocompleteModule,
        MatInputModule,
        MatIconModule,
        MatCardModule,
        DragDropModule,
        BrowserAnimationsModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateDeveloperComponent);
    component = fixture.componentInstance;
    component.ngOnInit();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('checkDeveloperNotExistsListIsUndefined', () => {
    developerDataCache.developers = undefined;
    expect(component.checkDeveloperNotExists('admin')).toBeTruthy();
  });

  it('checkDeveloperNotExists', () => {
    developerDataCache.developers = [{id: 'alice', name: 'Alice', clients: [{clientId: 'Support-Customer', organizationId: 'Production'}]}];

    expect(developerDataCache.developers).not.toBe(null);
    expect(developerDataCache.developers).not.toBe(undefined);
    // expects true for undefined input to hide the notification in the UI
    expect(component.checkDeveloperNotExists(undefined)).toBeTruthy();
    expect(component.checkDeveloperNotExists('Alice')).toBeFalsy();
    expect(component.checkDeveloperNotExists('Bob')).toBeTruthy();
  });

  it('checkUserExistsInKeycloakListIsUndefined', () => {
    component.keycloakUsers = undefined;
    expect(component.checkUserExistsInKeycloak('admin')).toBeFalsy();
  });

  it('checkUserExistsInKeycloak', () => {
    component.keycloakUsers = keycloakUsers;
    expect(component.checkUserExistsInKeycloak(undefined)).toBeFalsy();
    expect(component.checkUserExistsInKeycloak(null)).toBeFalsy();
    expect(component.checkUserExistsInKeycloak('alice')).toBeFalsy();
    expect(component.checkUserExistsInKeycloak('admin')).toBeTruthy();
  });
});
