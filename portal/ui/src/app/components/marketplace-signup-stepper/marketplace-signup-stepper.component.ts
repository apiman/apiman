/*
 * Copyright 2022 Scheer PAS Schweiz AG
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

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import {
  IAction,
  IClientSummary,
  IContract,
  IContractSummary,
  INewContract,
  IPolicy
} from '../../interfaces/ICommunication';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { ConfigService } from '../../services/config/config.service';
import { SignUpService } from '../../services/sign-up/sign-up.service';
import { ISignUpInfo } from '../../interfaces/ISignUpInfo';
import { BackendService } from '../../services/backend/backend.service';
import { MatStepper } from '@angular/material/stepper';
import { IContractExt } from '../../interfaces/IContractExt';
import { map, switchMap } from 'rxjs/operators';
import { TocService } from '../../services/toc/toc.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClientService } from '../../services/client/client.service';

@Component({
  selector: 'app-marketplace-signup-stepper',
  templateUrl: './marketplace-signup-stepper.component.html',
  styleUrls: ['./marketplace-signup-stepper.component.scss']
})
export class MarketplaceSignupStepperComponent implements OnInit {
  // stepper completed checks, must be true to go to next step
  agreedTermsAndPrivacy: boolean | undefined;

  selectedClient: IClientSummary | undefined;
  termsEnabled: boolean;
  newContractDetails: ISignUpInfo;
  contract: IContractExt;
  policies: IPolicy[] = [];

  constructor(
    private heroService: HeroService,
    private translator: TranslateService,
    private snackbar: SnackbarService,
    private configService: ConfigService,
    private signUpService: SignUpService,
    private router: Router,
    private backend: BackendService,
    private tocService: TocService,
    private clientService: ClientService
  ) {
    this.termsEnabled = this.configService.getTerms().enabled;
    this.newContractDetails = this.signUpService.getSignUpInfo();
    this.contract = {} as IContractExt;
  }

  ngOnInit(): void {
    this.checkNavigationAllowed();
    this.setUpHero();
  }

  private setUpHero() {
    const translation: string = this.translator.instant(
      'COMMON.SIGNUP'
    ) as string;

    this.heroService.setUpHero({
      title: this.newContractDetails.apiVersion.api.name,
      subtitle: `${this.newContractDetails.plan.planName} ${translation}`
    });
  }

  checkApplications($event: IClientSummary): void {
    this.selectedClient = $event;
  }

  checkTerms($event: boolean): void {
    this.agreedTermsAndPrivacy = $event;
  }

  nextAfterClientSelect(stepper: MatStepper): void {
    if (!this.selectedClient) {
      this.printUserError('WIZARD.CLIENT_ERROR');
    } else {
      this.checkIfContractAlreadyExists(stepper, this.selectedClient);
    }
  }

  private checkIfContractAlreadyExists(
    stepper: MatStepper,
    client: IClientSummary
  ) {
    console.info(`Using client ${client.organizationId}/${client.name}`);

    this.backend
      .getContractSummaries(client.organizationId, client.id, '1.0')
      .subscribe({
        next: (contractSummaries: IContractSummary[]) => {
          if (
            contractSummaries.some((summary: IContractSummary) => {
              return (
                summary.apiId === this.newContractDetails.apiVersion.api.id &&
                summary.apiOrganizationId ===
                  this.newContractDetails.apiVersion.api.organization.id &&
                summary.apiVersion ===
                  this.newContractDetails.apiVersion.version
              );
            })
          ) {
            this.printUserError('WIZARD.CONTRACT_EXISTS');
          } else {
            this.goToNextStep(stepper);
          }
        },

        error: (error: HttpErrorResponse) => {
          this.snackbar.showErrorSnackBar(error.message, error);
        }
      });
  }

  nextAfterTermsAgreed(): void {
    if (!this.agreedTermsAndPrivacy) {
      this.printUserError('WIZARD.TERMS_ERROR');
    }
  }

  private checkNavigationAllowed(): void {
    if (
      !this.newContractDetails ||
      !this.newContractDetails.apiVersion ||
      !this.newContractDetails.plan
    ) {
      this.printUserError('WIZARD.REDIRECT');
      void this.router.navigate(['home']);
    }
  }

  createContractAndRegisterClient(stepper: MatStepper): void {
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const client: IClientSummary = this.selectedClient!;

    const contract: INewContract = {
      apiOrgId: this.newContractDetails.apiVersion.api.organization.id,
      apiId: this.newContractDetails.apiVersion.api.id,
      apiVersion: this.newContractDetails.apiVersion.version,
      planId: this.newContractDetails.plan.planId
    };

    this.backend
      .createContract(client.organizationId, client.id, '1.0', contract)
      .pipe(
        switchMap((contract) => {
          this.snackbar.showPrimarySnackBar(
            this.translator.instant('WIZARD.SUCCESS') as string
          );
          return this.extendContract(contract);
        }),
        switchMap((contract: IContractExt) => {
          this.contract = contract;
          return this.registerClient();
        })
      )
      .subscribe({
        error: (error: HttpErrorResponse) =>
          this.snackbar.showErrorSnackBar(error.message, error),
        complete: () => {
          if ('AwaitingApproval' === this.contract.client.status) {
            void this.router.navigate(['approval']);
          } else {
            this.goToNextStep(stepper);
          }
        }
      });
  }

  private extendContract(contract: IContract) {
    return this.backend
      .getManagedApiEndpoint(
        contract.api.api.organization.id,
        contract.api.api.id,
        contract.api.version
      )
      .pipe(
        map((endpoint) => {
          return {
            ...contract,
            managedEndpoint: endpoint.managedEndpoint
          } as IContractExt;
        })
      );
  }

  private registerClient(): Observable<void> {
    const action: IAction = {
      type: 'registerClient',
      entityVersion: '1.0',
      organizationId: this.contract.client.client.organization.id,
      entityId: this.contract.client.client.id
    };
    return this.clientService.registerClient(action);
  }

  finish(): void {
    void this.router.navigate(['applications'], {
      fragment: this.tocService.formatApiVersionPlanId(this.contract)
    });
  }

  private printUserError(key: string): void {
    this.snackbar.showErrorSnackBar(this.translator.instant(key) as string);
  }

  private goToNextStep(stepper: MatStepper) {
    // Set completed step directly to allow going to the next step
    // See: https://github.com/angular/components/pull/15403

    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    stepper.steps.get(stepper.selectedIndex)!.completed = true;
    stepper.next();
  }
}
