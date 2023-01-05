/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { MarketplaceApiDetailsComponent } from './components/marketplace-api-details/marketplace-api-details.component';
import { MarketplaceSignupStepperComponent } from './components/marketplace-signup-stepper/marketplace-signup-stepper.component';
import { MarketplaceComponent } from './components/marketplace/marketplace.component';
import { AccountComponent } from './components/account/account.component';
import { MyAppsComponent } from './components/my-apps/my-apps.component';
import { AuthGuard } from './guards/auth.guard';
import { NotificationsComponent } from './components/notifications/notifications.component';
import { LoginGuard } from './guards/login.guard';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'marketplace', component: MarketplaceComponent },
  {
    path: 'api-details/:orgId/:apiId',
    component: MarketplaceApiDetailsComponent
  },
  {
    path: 'api-signup',
    component: MarketplaceSignupStepperComponent,
    canActivate: [AuthGuard]
  },
  { path: 'account', component: AccountComponent, canActivate: [LoginGuard] },
  {
    path: 'applications',
    component: MyAppsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'swagger/:orgId/:apiId/:apiVersion',
    loadChildren: () =>
      import('./components/swagger/swagger.module').then((m) => m.SwaggerModule)
  },
  {
    path: 'approval',
    loadChildren: () =>
      import('./components/approval/approval.module').then(
        (m) => m.ApprovalModule
      )
  },
  {
    path: 'notifications',
    component: NotificationsComponent,
    canActivate: [AuthGuard]
  },
  { path: '**', redirectTo: '/home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { urlUpdateStrategy: 'eager' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
