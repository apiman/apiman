import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {AdminComponent} from './components/admin/admin.component';
import {DeveloperComponent} from './components/developer/developer.component';
import {CreateDeveloperComponent} from './components/admin/create-developer/create-developer.component';
import {EditDeveloperComponent} from './components/admin/edit-developer/edit-developer.component';
import {DevportalGuard} from './auth/devportal.guard';
import {AdminGuard} from './auth/admin.guard';
import {NotAuthorizedComponent} from './components/not-authorized/not-authorized.component';

const routes: Routes = [
  {path: '', component: DeveloperComponent, canActivate: [DevportalGuard]},
  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [AdminGuard],
    children: []
  },
  {path: 'admin/create/developer', component: CreateDeveloperComponent, canActivate: [AdminGuard]},
  {path: 'admin/edit/developer/:developerId', component: EditDeveloperComponent, canActivate: [AdminGuard]},
  {path: 'not-authorized', component: NotAuthorizedComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
