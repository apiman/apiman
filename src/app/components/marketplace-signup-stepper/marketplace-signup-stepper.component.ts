import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import {
  IClientSummary,
  IContract,
  INewContract,
} from '../../interfaces/ICommunication';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { ConfigService } from '../../services/config/config.service';
import { SignUpService } from '../../services/sign-up/sign-up.service';
import { ISignUpInfo } from '../../interfaces/ISignUpInfo';
import { BackendService } from '../../services/backend/backend.service';
import { MatStepper } from '@angular/material/stepper';

@Component({
  selector: 'app-marketplace-signup-stepper',
  templateUrl: './marketplace-signup-stepper.component.html',
  styleUrls: ['./marketplace-signup-stepper.component.scss'],
})
export class MarketplaceSignupStepperComponent implements OnInit {
  selectedClients = new Set<IClientSummary>();
  agreedTermsAndPrivacy: boolean | undefined;
  termsEnabled: boolean;
  infos: ISignUpInfo;
  contract: IContract | undefined;

  constructor(
    private heroService: HeroService,
    private route: ActivatedRoute,
    private translator: TranslateService,
    private snackbar: SnackbarService,
    private configService: ConfigService,
    private signUpService: SignUpService,
    private router: Router,
    private backend: BackendService
  ) {
    this.termsEnabled = this.configService.getTerms().enabled;
    this.infos = this.signUpService.getSignUpInfo();
  }

  ngOnInit(): void {
    this.setUpHero();
    this.checkNavigationAllowed();
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

  private checkNavigationAllowed(): void {
    if (!this.infos) {
      this.snackbar.showErrorSnackBar(
        this.translator.instant('WIZARD.REDIRECT')
      );
      void this.router.navigate(['home']);
    }
  }

  createContract(stepper: MatStepper): void {
    const client: IClientSummary = this.selectedClients.values().next().value;

    const contract: INewContract = {
      apiOrgId: this.infos.organizationId,
      apiId: this.infos.apiVersion.api.id,
      apiVersion: this.infos.apiVersion.version,
      planId: this.infos.plan.id,
    };

    this.backend
      .createContract(client.organizationId, client.id, '1.0', contract)
      .subscribe(
        (contract: IContract) => {
          this.snackbar.showPrimarySnackBar(
            this.translator.instant('WIZARD.SUCCESS')
          );
          this.contract = contract;
          stepper.next();
        },
        (error) => this.snackbar.showErrorSnackBar(error.message, error)
      );
  }

  finish(): void {
    void this.router.navigate(['applications']);
  }
}
