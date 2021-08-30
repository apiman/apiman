import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatGridListModule} from "@angular/material/grid-list";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatListModule} from "@angular/material/list";
import {MarkdownModule} from "ngx-markdown";
import {MatIconModule} from "@angular/material/icon";
import {MatStepperModule} from "@angular/material/stepper";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatInputModule} from "@angular/material/input";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {FormsModule} from "@angular/forms";

import { AppComponent } from './app.component';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { ApiCardComponent } from './components/api-card/api-card.component';
import { CardListComponent } from './components/card-list/card-list.component';
import { MarketplaceApiDetailsComponent } from './components/marketplace-api-details/marketplace-api-details.component';
import { HomeComponent } from './components/home/home.component';
import { MarketplaceApiTermsComponent } from "./components/marketplace-api-terms/marketplace-api-terms.component";
import { MarketplaceSignupStepperComponent } from "./components/marketplace-signup-stepper/marketplace-signup-stepper.component";

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    ApiCardComponent,
    CardListComponent,
    MarketplaceApiDetailsComponent,
    MarketplaceApiTermsComponent,
    MarketplaceSignupStepperComponent,
    HomeComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatCardModule,
    MatGridListModule,
    MatStepperModule,
    MatExpansionModule,
    MatIconModule,
    MatListModule,
    MarkdownModule.forRoot(),
    FormsModule,
    MatCheckboxModule,
    MatInputModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
