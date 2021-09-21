import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MarkdownModule } from 'ngx-markdown';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { ApiCardComponent } from './components/api-card/api-card.component';
import { MarketplaceApiDetailsComponent } from './components/marketplace-api-details/marketplace-api-details.component';
import { HomeComponent } from './components/home/home.component';
import { MarketplaceApiTermsComponent } from './components/marketplace-api-terms/marketplace-api-terms.component';
import { MarketplaceSignupStepperComponent } from './components/marketplace-signup-stepper/marketplace-signup-stepper.component';
import { MarketplaceApiPoliciesComponent } from './components/marketplace-api-policies/marketplace-api-policies.component';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { MarketplaceApiDescriptionComponent } from './components/marketplace-api-description/marketplace-api-description.component';
import { PlanCardComponent } from './components/plan-card/plan-card.component';
import { MarketplaceComponent } from './components/marketplace/marketplace.component';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { InitializerService } from './services/initializer/initializer.service';
import { MaterialModule } from './material.module';
import { ConfigService } from './services/config/config.service';
import { NavigationComponent } from './components/navigation/navigation.component';
import { ApiCardListComponent } from './components/api-card-list/api-card-list.component';
import { PlanCardListComponent } from './components/plan-card-list/plan-card-list.component';
import { AccountComponent } from './components/account/account.component';
import { MyAppsComponent } from './components/my-apps/my-apps.component';
import { ThemeService } from './services/theme/theme.service';
import { KeycloakAngularModule } from 'keycloak-angular';
import { KeycloakHelperService } from './services/keycloak-helper/keycloak-helper.service';
import { SwaggerComponent } from './components/swagger/swagger.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MarketplaceClientAppComponent } from './components/marketplace-client-app/marketplace-client-app.component';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MyAppsSummaryComponent } from './components/my-apps-summary/my-apps-summary.component';
import { MyAppsUseApiComponent } from './components/my-apps-use-api/my-apps-use-api.component';
import { MyAppsPoliciesComponent } from './components/my-apps-policies/my-apps-policies.component';
import { MyAppsManageApiComponent } from './components/my-apps-manage-api/my-apps-manage-api.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';

export function initializeApp(
  configService: ConfigService,
  devPortalInitializer: InitializerService
): () => Promise<void> {
  configService.readAndEvaluateConfig();

  /* Define promises needed for the app initialization */
  const initLanguagePromise: Promise<void> = new Promise((resolve, reject) => {
    devPortalInitializer
      .initLanguage(configService.getLanguage())
      .then(() => {
        resolve();
      })
      .catch(() => {
        reject();
      });
  });

  return (): Promise<void> => {
    return new Promise((resolve, reject) => {
      /* Insert defined promises */
      Promise.all([initLanguagePromise])
        .then(() => {
          resolve();
        })
        .catch((e) => {
          reject(e);
        });
    });
  };
}

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './../assets/i18n/', '.json');
}

function initializeKeycloak(keycloakHelper: KeycloakHelperService) {
  return async () => await keycloakHelper.initKeycloak();
}

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    ApiCardComponent,
    MarketplaceApiDetailsComponent,
    MarketplaceApiTermsComponent,
    MarketplaceSignupStepperComponent,
    HomeComponent,
    MarketplaceApiPoliciesComponent,
    MarketplaceApiDescriptionComponent,
    PlanCardComponent,
    MarketplaceComponent,
    NavigationComponent,
    ApiCardListComponent,
    PlanCardListComponent,
    AccountComponent,
    MyAppsComponent,
    SwaggerComponent,
    MarketplaceClientAppComponent,
    MyAppsSummaryComponent,
    MyAppsUseApiComponent,
    MyAppsPoliciesComponent,
    MyAppsManageApiComponent,
  ],
  imports: [
    KeycloakAngularModule,
    MaterialModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MarkdownModule.forRoot(),
    FormsModule,
    HttpClientModule,
    ClipboardModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient],
      },
    }),
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    ReactiveFormsModule,
    MatSortModule,
    MatSnackBarModule,
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      multi: true,
      deps: [ConfigService, InitializerService, ThemeService],
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakHelperService],
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
