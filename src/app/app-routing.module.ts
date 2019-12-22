import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {AdminComponent} from './components/admin/admin.component';
import {DeveloperComponent} from './components/developer/developer.component';
import {CreateDeveloperComponent} from './components/admin/create-developer/create-developer.component';
import {EditDeveloperComponent} from './components/admin/edit-developer/edit-developer.component';
import {DevportalGuard} from './auth/devportal.guard';
import {AdminGuard} from './auth/admin.guard';
import {NotAuthorizedComponent} from './components/not-authorized/not-authorized.component';
import {SwaggerComponent} from './components/swagger/swagger.component';
import {AboutComponent} from './about/about.component';

const routes: Routes = [
  {path: '', component: DeveloperComponent, canActivate: [DevportalGuard]},
  {path: 'about', component: AboutComponent},
  {
    path: 'admin',
    canActivate: [AdminGuard],
    children: [
      {
        path: '',
        component: AdminComponent
      },
      {path: 'create/developer', component: CreateDeveloperComponent},
      {path: 'edit/developer/:developerId', component: EditDeveloperComponent}
    ]
  },
  {path: 'not-authorized', component: NotAuthorizedComponent},
  {
    path: 'swagger/developer/:developerId/organizations/:orgId/apis/:apiId/versions/:version',
    component: SwaggerComponent,
    canActivate: [DevportalGuard]
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
