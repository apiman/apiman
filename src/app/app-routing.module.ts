import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AdminComponent } from './admin/admin.component';
import {DeveloperComponent} from './developer/developer.component';
import {CreateDeveloperComponent} from './admin/create-developer/create-developer.component';

const routes: Routes = [
  { path: '', component: DeveloperComponent },
  { path: 'admin', component: AdminComponent },
  { path: 'admin/create/developer', component: CreateDeveloperComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
