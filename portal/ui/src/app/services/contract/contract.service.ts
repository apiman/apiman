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

import { Injectable } from '@angular/core';
import { BackendService } from '../backend/backend.service';
import { EMPTY, forkJoin, Observable, switchMap } from 'rxjs';
import {
  IClientVersion,
  IContract,
  IContractSummary
} from '../../interfaces/ICommunication';
import { catchError, defaultIfEmpty, map } from 'rxjs/operators';
import { IContractExt } from '../../interfaces/IContractExt';
import { ApiService } from '../api/api.service';
import { PolicyService } from '../policy/policy.service';
import { SnackbarService } from '../snackbar/snackbar.service';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class ContractService {
  constructor(
    private backendService: BackendService,
    private apiService: ApiService,
    private policyService: PolicyService,
    private snackbarService: SnackbarService,
    private translator: TranslateService
  ) {}

  public getContractSummaries(
    clientVersions: IClientVersion | IClientVersion[]
  ): Observable<IContractSummary[]> {
    const value = !Array.isArray(clientVersions)
      ? [clientVersions]
      : clientVersions;
    return forkJoin(
      value.map((clientVersion: IClientVersion) => {
        return this.backendService.getContractSummaries(
          clientVersion.client.organization.id,
          clientVersion.client.id,
          clientVersion.version
        );
      })
    ).pipe(
      map((nestedContractSummaries: IContractSummary[][]) => {
        return nestedContractSummaries.flat();
      })
    );
  }

  public getContracts(
    contractSummaries: IContractSummary | IContractSummary[]
  ): Observable<IContract[]> {
    const value = !Array.isArray(contractSummaries)
      ? [contractSummaries]
      : contractSummaries;
    return forkJoin(
      value.map((contractSummary: IContractSummary) => {
        return this.backendService.getContract(
          contractSummary.clientOrganizationId,
          contractSummary.clientId,
          contractSummary.clientVersion,
          contractSummary.contractId
        );
      })
    ).pipe(defaultIfEmpty([]));
  }

  public getExtendedContracts(
    contracts: IContract[]
  ): Observable<IContractExt[]> {
    return forkJoin(
      contracts.map((contract: IContract) => {
        const orgId = contract.plan.plan.organization.id;
        return forkJoin([
          this.policyService.getPlanPolicies(
            orgId,
            contract.plan.plan.id,
            contract.plan.version
          ),

          this.apiService.getManagedApiEndpoint(
            orgId,
            contract.api.api.id,
            contract.api.version
          )
        ]).pipe(
          defaultIfEmpty([]),
          map(([planPolicies, endpoint]) => {
            planPolicies.forEach((planPolicy) => {
              this.policyService.extendPolicy(planPolicy);
            });

            return {
              ...contract,
              policies: planPolicies,
              section: 'summary',
              managedEndpoint: endpoint.managedEndpoint,
              docsAvailable: this.apiService.isApiDocAvailable(contract.api)
            } as IContractExt;
          })
        );
      })
    ).pipe(
      map((contracts: IContractExt[]) => {
        return contracts.sort((a, b) => {
          return a.api.api.name
            .toLowerCase()
            .localeCompare(b.api.api.name.toLowerCase());
        });
      }),
      defaultIfEmpty([])
    );
  }

  public getExtendedContractsFromClients(
    clients: IClientVersion[]
  ): Observable<IContractExt[]> {
    return this.getContractSummaries(clients).pipe(
      switchMap((contractSummaries: IContractSummary[]) => {
        return this.getContracts(contractSummaries);
      }),
      switchMap((contracts: IContract[]) => {
        return this.getExtendedContracts(contracts);
      })
    );
  }

  public breakContract(contract: IContractExt | IContract): Observable<void> {
    return this.backendService
      .breakContract(
        contract.client.client.organization.id,
        contract.client.client.id,
        contract.client.version,
        contract.id
      )
      .pipe(
        catchError((err) => {
          console.error('Deleting contract failed: ', err);
          this.snackbarService.showErrorSnackBar(
            this.translator.instant('CLIENTS.DELETE_CONTRACT_FAILED') as string
          );
          return EMPTY;
        })
      );
  }
}
