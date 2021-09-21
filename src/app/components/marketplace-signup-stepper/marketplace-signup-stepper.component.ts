import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { IClientSummary } from '../../interfaces/ICommunication';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';
import { MatStepper } from '@angular/material/stepper';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import {SignUpService} from "../../services/sign-up/sign-up.service";

@Component({
  selector: 'app-marketplace-signup-stepper',
  templateUrl: './marketplace-signup-stepper.component.html',
  styleUrls: ['./marketplace-signup-stepper.component.scss'],
  providers: [
    {
      provide: STEPPER_GLOBAL_OPTIONS,
      useValue: { showError: true },
    },
  ],
})
export class MarketplaceSignupStepperComponent implements OnInit {
  selectedClients = new Set<IClientSummary>();
  agreedTermsAndPrivacy: boolean | undefined;

  constructor(
    private heroService: HeroService,
    private route: ActivatedRoute,
    private translator: TranslateService,
    private snackbar: SnackbarService,
    private signUpService: SignUpService
  ) {}

  ngOnInit(): void {
    this.setUpHero();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('API_SIGN_UP.TITLE'),
    });
  }

  checkApplications($event: Set<IClientSummary>) {
    this.selectedClients = $event;
  }

  checkTerms($event: boolean) {
    this.agreedTermsAndPrivacy = $event;
  }

  nextStep1(stepper: MatStepper) {
    if (this.selectedClients.size == 0) {
      this.snackbar.showErrorSnackBar(
        this.translator.instant('WIZARD.APPLICATION_ERROR')
      );
    }
    stepper.next();
  }

  nextStep2() {
    if (!this.agreedTermsAndPrivacy)
      this.snackbar.showErrorSnackBar(
        this.translator.instant('WIZARD.TERMS_ERROR')
      );
  }
}
