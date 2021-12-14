/*
 * Copyright 2021 Scheer PAS Schweiz AG
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
import { ActivatedRoute, Router } from '@angular/router';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import {
  IClientSummary,
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

@Component({
  selector: 'app-marketplace-signup-stepper',
  templateUrl: './marketplace-signup-stepper.component.html',
  styleUrls: ['./marketplace-signup-stepper.component.scss']
})
export class MarketplaceSignupStepperComponent implements OnInit {
  selectedClients = new Set<IClientSummary>();
  agreedTermsAndPrivacy: boolean | undefined;
  termsEnabled: boolean;
  newContractDetails: ISignUpInfo;
  contract: IContractExt;
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

  checkApplications($event: Set<IClientSummary>): void {
    this.selectedClients = $event;
  }

  checkTerms($event: boolean): void {
    this.agreedTermsAndPrivacy = $event;
  }

  nextAfterClientSelect(stepper: MatStepper): void {
    if (this.selectedClients.size == 0) {
      this.printUserError('WIZARD.APPLICATION_ERROR');
    } else {
      this.checkIfContractAlreadyExists(stepper);
    }
  }

  private checkIfContractAlreadyExists(stepper: MatStepper) {
    const client: IClientSummary = this.selectedClients.values().next()
      .value as IClientSummary;
    this.backend
      .getContracts(client.organizationId, client.id, '1.0')
      .subscribe(
        (contractSummaries: IContractSummary[]) => {
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
            stepper.next();
          }
        },
        (error: HttpErrorResponse) => {
          this.snackbar.showErrorSnackBar(error.message, error);
        }
      );
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

  createContract(stepper: MatStepper): void {
    const client: IClientSummary = this.selectedClients.values().next()
      .value as IClientSummary;

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
        })
      )
      .subscribe(
        (contract: IContractExt) => {
          this.snackbar.showPrimarySnackBar(
            this.translator.instant('WIZARD.SUCCESS') as string
          );
          this.contract = contract;
          if ('AwaitingApproval' === this.contract.client.status) {
            void this.router.navigate(['approval']);
          } else {
            stepper.next();
          }
        },
        (error: HttpErrorResponse) =>
          this.snackbar.showErrorSnackBar(error.message, error)
      );
  }

  finish(): void {
    this.backend
      .sendAction({
        type: 'registerClient',
        entityVersion: '1.0',
        organizationId: this.contract.client.client.organization.id,
        entityId: this.contract.client.client.id
      })
      .subscribe(
        () => {
          // void response
        },
        (error: HttpErrorResponse) => {
          this.snackbar.showErrorSnackBar(error.message, error);
        }
      );
    void this.router.navigate(['applications'], {
      fragment: this.tocService.formatClientId(this.contract)
    });
  }

  private printUserError(key: string): void {
    this.snackbar.showErrorSnackBar(this.translator.instant(key) as string);
  }
}
