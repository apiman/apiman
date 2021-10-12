import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import {
  IClientSummary,
  INewContract,
  IPolicy,
} from '../../interfaces/ICommunication';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { ConfigService } from '../../services/config/config.service';
import { SignUpService } from '../../services/sign-up/sign-up.service';
import { ISignUpInfo } from '../../interfaces/ISignUpInfo';
import { BackendService } from '../../services/backend/backend.service';
import { MatStepper } from '@angular/material/stepper';
import { IContractExt } from '../../interfaces/IContractExt';
import { map, switchMap } from 'rxjs/operators';
import { TocService} from '../../services/toc/toc.service';

@Component({
  selector: 'app-marketplace-signup-stepper',
  templateUrl: './marketplace-signup-stepper.component.html',
  styleUrls: ['./marketplace-signup-stepper.component.scss'],
})
export class MarketplaceSignupStepperComponent implements OnInit {
  selectedClients = new Set<IClientSummary>();
  agreedTermsAndPrivacy: boolean | undefined;
  termsEnabled: boolean;
  newContractDetails: ISignUpInfo;
  contract: IContractExt | undefined;
  policies: IPolicy[] = [];

  constructor(
    private heroService: HeroService,
    private route: ActivatedRoute,
    private translator: TranslateService,
    private snackbar: SnackbarService,
    private configService: ConfigService,
    private signUpService: SignUpService,
    private router: Router,
    private backend: BackendService,
    private tocService: TocService
  ) {
    this.termsEnabled = this.configService.getTerms().enabled;
    this.newContractDetails = this.signUpService.getSignUpInfo();
  }

  ngOnInit(): void {
    this.checkNavigationAllowed();
    this.setUpHero();
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.newContractDetails.apiVersion.api.name,
      subtitle: this.newContractDetails.plan.planName + ' ' + this.translator.instant('COMMON.SIGNUP'),
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
    if (
      !this.newContractDetails ||
      !this.newContractDetails.organizationId ||
      !this.newContractDetails.apiVersion ||
      !this.newContractDetails.plan
    ) {
      this.snackbar.showErrorSnackBar(
        this.translator.instant('WIZARD.REDIRECT')
      );
      void this.router.navigate(['home']);
    }
  }

  createContract(stepper: MatStepper): void {
    const client: IClientSummary = this.selectedClients.values().next().value;

    const contract: INewContract = {
      apiOrgId: this.newContractDetails.organizationId,
      apiId: this.newContractDetails.apiVersion.api.id,
      apiVersion: this.newContractDetails.apiVersion.version,
      planId: this.newContractDetails.plan.planId,
    };

    this.backend
      .createContract(client.organizationId, client.id, '1.0', contract).pipe(
        switchMap(contract => {
          return this.backend.getManagedApiEndpoint(
            contract.api.api.organization.id,
            contract.api.api.id,
            contract.api.version).pipe(
              map(endpoint => {
                return {
                  ...contract,
                  managedEndpoint: endpoint.managedEndpoint
                } as IContractExt;
              })
          )
        })
      )
      .subscribe(
        (contract: IContractExt) => {
          this.snackbar.showPrimarySnackBar(
            this.translator.instant('WIZARD.SUCCESS')
          );
          this.contract = contract;
          if ('AwaitingApproval' === this.contract.client.status) {
            void this.router.navigate(['approval']);
          } else {
            stepper.next();
          }
        },
        (error) => this.snackbar.showErrorSnackBar(error.message, error)
      );
  }

  finish(): void {
    void this.router.navigate(['applications'], {fragment: this.tocService.formatClientId(this.contract!)});
  }
}
