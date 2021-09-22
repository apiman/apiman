import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { IClientSummary } from '../../interfaces/ICommunication';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { ConfigService } from '../../services/config/config.service';

@Component({
  selector: 'app-marketplace-signup-stepper',
  templateUrl: './marketplace-signup-stepper.component.html',
  styleUrls: ['./marketplace-signup-stepper.component.scss'],
})
export class MarketplaceSignupStepperComponent implements OnInit {
  selectedClients = new Set<IClientSummary>();
  agreedTermsAndPrivacy: boolean | undefined;
  termsEnabled: boolean;

  constructor(
    private heroService: HeroService,
    private route: ActivatedRoute,
    private translator: TranslateService,
    private snackbar: SnackbarService,
    private configService: ConfigService
  ) {
    this.termsEnabled = this.configService.getTerms().enabled;
  }

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

  nextAfterClientSelect() {
    if (this.selectedClients.size == 0) {
      this.snackbar.showErrorSnackBar(
        this.translator.instant('WIZARD.APPLICATION_ERROR')
      );
    }
  }

  nextAfterTermsAgreed() {
    if (!this.agreedTermsAndPrivacy)
      this.snackbar.showErrorSnackBar(
        this.translator.instant('WIZARD.TERMS_ERROR')
      );
  }
}
