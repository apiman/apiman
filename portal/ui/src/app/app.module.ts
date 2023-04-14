/*
 * Copyright 2023 Scheer PAS Schweiz AG
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

import { APP_INITIALIZER, NgModule, SecurityContext } from '@angular/core';
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
import { ClipboardModule } from '@angular/cdk/clipboard';
import { MarketplaceApiDescriptionComponent } from './components/marketplace-api-description/marketplace-api-description.component';
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
import { KeycloakAngularModule } from 'keycloak-angular';
import { KeycloakHelperService } from './services/keycloak-helper/keycloak-helper.service';
import { MarketplaceClientAppComponent } from './components/marketplace-client-app/marketplace-client-app.component';
import { MyAppsSummaryComponent } from './components/my-apps-summary/my-apps-summary.component';
import { MyAppsUseApiComponent } from './components/my-apps-use-api/my-apps-use-api.component';
import { MyAppsPoliciesComponent } from './components/my-apps-policies/my-apps-policies.component';
import { MyAppsManageApiComponent } from './components/my-apps-manage-api/my-apps-manage-api.component';
import { PolicyCardComponent } from './components/policy-card/policy-card.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { GaugeChartComponent } from './components/charts/gauge-chart/gauge-chart.component';
import { IConfig } from './interfaces/IConfig';
import { ThemeService } from './services/theme/theme.service';
import { TocComponent } from './components/toc/toc.component';
import { PolicyCardLightComponent } from './components/policy-card-light/policy-card-light.component';
import { NoDataComponent } from './components/no-data/no-data.component';
import { UnregisterClientComponent } from './components/dialogs/unregister-client/unregister-client.component';
import { ApiDocumentationButtonsComponent } from './components/api-documentation-buttons/api-documentation-buttons.component';
import { RateQuotaPolicyShortLimitComponent } from './components/policies/rate-quota-policy-short-limit/rate-quota-policy-short-limit.component';
import { MatBadgeModule } from '@angular/material/badge';
import { NotificationsComponent } from './components/notifications/notifications.component';
import { ApiMarkdownDescriptionComponent } from './components/api-markdown-description/api-markdown-description.component';
import { ApiStatusTagComponent } from './components/api-status-tag/api-status-tag.component';
import { AccountIdmComponent } from './components/account-idm/account-idm.component';
import { AccountOrganizationPermissionsComponent } from './components/account-organization-permissions/account-organization-permissions.component';
import { ApiPublicEndpointComponent } from './components/api-public-endpoint/api-public-endpoint.component';
import { MatRadioModule } from '@angular/material/radio';
import { BreakContractComponent } from './components/dialogs/break-contract/break-contract.component';
import { SharedModule } from './shared/shared.module';

export function initializeApp(
  configService: ConfigService,
  devPortalInitializer: InitializerService,
  keycloakHelper: KeycloakHelperService,
  themeService: ThemeService
): () => Promise<boolean> {
  return () =>
    new Promise((resolve, reject) => {
      /* At first fetch the configuration file */
      void configService.readAndEvaluateConfig().then((config: IConfig) => {
        /* After you can list the promises, which depend on the config here */
        return Promise.all([
          devPortalInitializer.initLanguage(),
          keycloakHelper.initKeycloak(),
          themeService.initTheme(config.theme)
        ])
          .then(() => {
            resolve(true);
          })
          .catch((e: Error) => {
            reject(e);
          });
      });
    });
}

export function createTranslateLoader(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http, 'assets/i18n/', '.json');
}

@NgModule({
  declarations: [
    AccountComponent,
    ApiCardComponent,
    ApiCardListComponent,
    ApiDocumentationButtonsComponent,
    ApiMarkdownDescriptionComponent,
    ApiPublicEndpointComponent,
    ApiStatusTagComponent,
    AppComponent,
    BreakContractComponent,
    FooterComponent,
    GaugeChartComponent,
    HeaderComponent,
    HomeComponent,
    MarketplaceApiDescriptionComponent,
    MarketplaceApiDetailsComponent,
    MarketplaceApiTermsComponent,
    MarketplaceClientAppComponent,
    MarketplaceComponent,
    MarketplaceSignupStepperComponent,
    AccountIdmComponent,
    AccountOrganizationPermissionsComponent,
    MyAppsComponent,
    MyAppsManageApiComponent,
    MyAppsPoliciesComponent,
    MyAppsSummaryComponent,
    MyAppsUseApiComponent,
    NavigationComponent,
    NoDataComponent,
    NotificationsComponent,
    PlanCardListComponent,
    PolicyCardComponent,
    PolicyCardLightComponent,
    RateQuotaPolicyShortLimitComponent,
    TocComponent,
    UnregisterClientComponent
  ],
  imports: [
    KeycloakAngularModule,
    MaterialModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MarkdownModule.forRoot({
      sanitize: SecurityContext.NONE
    }),
    FormsModule,
    HttpClientModule,
    ClipboardModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient]
      }
    }),
    ReactiveFormsModule,
    NgxChartsModule,
    MatBadgeModule,
    MatRadioModule,
    SharedModule
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      multi: true,
      deps: [
        ConfigService,
        InitializerService,
        KeycloakHelperService,
        ThemeService
      ]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
