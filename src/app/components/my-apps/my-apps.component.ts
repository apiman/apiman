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

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { BackendService } from '../../services/backend/backend.service';
import { ISection } from '../../interfaces/ISection';
import {
  IApiStatus,
  IClientStatus,
  IStatusColors,
  statusColorMap
} from '../../interfaces/IStatus';
import {
  IClientSummary,
  IClientVersionSummary,
  IContract,
  IContractSummary,
  IPermission
} from '../../interfaces/ICommunication';
import { IContractExt } from '../../interfaces/IContractExt';
import { PolicyService } from '../../services/policy/policy.service';
import { catchError, debounceTime, map, switchMap, tap } from 'rxjs/operators';
import { EMPTY, forkJoin, Observable, of, Subject } from 'rxjs';
import { flatArray } from '../../shared/utility';
import { SpinnerService } from '../../services/spinner/spinner.service';
import { ApiService } from '../../services/api/api.service';
import { ITocLink } from '../../interfaces/ITocLink';
import { TocService } from '../../services/toc/toc.service';
import { MatDialog } from '@angular/material/dialog';
import { UnregisterClientComponent } from '../dialogs/unregister-client/unregister-client.component';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { ConfigService } from '../../services/config/config.service';
import { IPolicyExt, IPolicyProbe } from '../../interfaces/IPolicy';
import { PermissionsService } from '../../services/permissions/permissions.service';

@Component({
  selector: 'app-my-apps',
  templateUrl: './my-apps.component.html',
  styleUrls: ['./my-apps.component.scss']
})
export class MyAppsComponent implements OnInit {
  organizationCount = 0;
  contracts: IContractExt[] = [];
  clientContractsMap!: Map<string, IContractExt[]>;
  filteredClientContractsMap: Map<string, IContractExt[]> = new Map<
    string,
    IContractExt[]
  >();
  clientContractsMap$!: Observable<Map<string, IContractExt[]>>;
  contractsLoaded = false;
  contractsFiltered = false;
  searchTerm = '';
  searchTermNotifier = new Subject();

  tocLinks: ITocLink[] = [];

  error = false;

  constructor(
    private spinnerService: SpinnerService,
    private heroService: HeroService,
    private translator: TranslateService,
    private backend: BackendService,
    private policyService: PolicyService,
    private apiService: ApiService,
    public tocService: TocService,
    private dialog: MatDialog,
    private snackbarService: SnackbarService,
    public configService: ConfigService,
    private cdr: ChangeDetectorRef,
    private permissionsService: PermissionsService
  ) {}

  ngOnInit(): void {
    this.setUpHero();
    this.fetchContracts();
    this.initSearchDebounce();
  }

  // Detailed explanation of request chain: https://stackoverflow.com/questions/69421293/how-to-chain-requests-correctly-with-rxjs
  private fetchContracts() {
    this.contracts = [];
    this.clientContractsMap = new Map<string, IContractExt[]>();

    this.spinnerService.startWaiting();
    this.contractsLoaded = false;
    forkJoin([
      this.backend.getEditableClients(),
      this.backend.getViewableClients()
    ])
      .pipe(
        switchMap((clientSummaries: IClientSummary[][]) => {
          return of(MyAppsComponent.getUniqueClients(clientSummaries));
        }),
        tap((clientSummaries: IClientSummary[]) => {
          this.organizationCount = new Set(
            clientSummaries.map((clientSummary) => clientSummary.organizationId)
          ).size;
        }),
        switchMap((clientSummaries: IClientSummary[]) => {
          if (clientSummaries.length === 0) {
            this.stopMainRequest();
          }
          return forkJoin(
            clientSummaries.map((clientSummary) => {
              return this.backend.getClientVersions(
                clientSummary.organizationId,
                clientSummary.id
              );
            })
          );
        }),
        switchMap((nestedClientVersionSummaries: IClientVersionSummary[][]) => {
          const clientVersionSummaries: IClientVersionSummary[] = flatArray(
            nestedClientVersionSummaries
          ) as IClientVersionSummary[];
          return forkJoin(
            clientVersionSummaries.map((clientVersionSummary) => {
              return this.backend.getContracts(
                clientVersionSummary.organizationId,
                clientVersionSummary.id,
                clientVersionSummary.version
              );
            })
          );
        }),
        switchMap((nestedContractSummaries: IContractSummary[][]) => {
          const contractSummaries: IContractSummary[] = flatArray(
            nestedContractSummaries
          ) as IContractSummary[];
          if (contractSummaries.length === 0) {
            this.stopMainRequest();
          }
          return forkJoin(
            contractSummaries.map((contractSummary) => {
              return this.backend.getContract(
                contractSummary.clientOrganizationId,
                contractSummary.clientId,
                contractSummary.clientVersion,
                contractSummary.contractId
              );
            })
          );
        }),
        switchMap((contracts: IContract[]) => {
          return forkJoin(
            contracts.map((contract) => {
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
                map(([planPolicies, endpoint]) => {
                  planPolicies.forEach((planPolicy) => {
                    this.policyService.extendPolicy(planPolicy);
                  });

                  return {
                    ...contract,
                    policies: planPolicies,
                    section: 'summary',
                    managedEndpoint: endpoint.managedEndpoint,
                    docsAvailable: this.apiService.isApiDocAvailable(
                      contract.api
                    ),
                    deletable: this.isDeleteAllowed(contract)
                  } as IContractExt;
                })
              );
            })
          );
        }),
        catchError((err) => {
          console.warn(err);
          this.stopMainRequest(true);
          return EMPTY;
        })
      )
      .subscribe((contracts) => {
        this.stopMainRequest();
        this.extractContracts(contracts);
        this.fetchPolicyProbes();
        this.generateTocLinks(this.clientContractsMap);
        this.clientContractsMap$ = of(this.clientContractsMap);
      });
  }

  private static getUniqueClients(clientSummaries: IClientSummary[][]) {
    const clients: IClientSummary[] = flatArray(
      clientSummaries
    ) as IClientSummary[];

    return [
      ...new Map(
        clients.map((clientSummary) => [
          clientSummary.organizationId + clientSummary.id,
          clientSummary
        ])
      ).values()
    ];
  }

  private fetchPolicyProbes() {
    this.contracts.forEach((contract: IContractExt) => {
      contract.policies.forEach((policy: IPolicyExt) => {
        this.policyService
          .getPolicyProbe(contract, policy)
          .pipe(
            catchError((err) => {
              console.warn(err);
              return EMPTY;
            })
          )
          .subscribe((probes: IPolicyProbe[]) => {
            policy.probe = probes[0];
            this.policyService.setGaugeDataForPolicy(policy);
          });
      });
    });
  }

  private stopMainRequest(setError = false) {
    this.spinnerService.stopWaiting();
    this.contractsLoaded = true;

    this.error = setError;
  }

  /**
   * Contracts will be stored in a map with schema {client.name:contract.version,contract-object}
   * @param contracts
   * @private
   */
  private extractContracts(contracts: IContractExt[]) {
    this.contracts = this.contracts.concat(contracts);

    this.contracts.forEach((contract: IContractExt) => {
      const clientNameVersionMapped = (
        contract.client.client.name +
        ':' +
        contract.client.version
      ).toLowerCase();
      const foundContracts = this.clientContractsMap.get(
        clientNameVersionMapped
      );

      if (!foundContracts) {
        this.clientContractsMap.set(clientNameVersionMapped, [contract]);
        return;
      }

      const contractIfPartOfArray = foundContracts.find((c: IContractExt) => {
        return c.id === contract.id;
      });

      if (contractIfPartOfArray) {
        return;
      }

      this.clientContractsMap.set(
        clientNameVersionMapped,
        foundContracts.concat(contract)
      );
    });

    // Sort Clients by name
    this.clientContractsMap = new Map(
      [...this.clientContractsMap.entries()].sort()
    );
  }

  filterContracts(searchTerm: string) {
    this.searchTerm = searchTerm;
    this.copyContracts();

    this.contractsFiltered = true;
    searchTerm = searchTerm.toLocaleLowerCase();
    this.filteredClientContractsMap.forEach((contracts, key) => {
      let removeClient = true;
      contracts.forEach((contract) => {
        if (
          contract.client.client.name
            .toLocaleLowerCase()
            .includes(searchTerm) ||
          contract.api.api.name.toLocaleLowerCase().includes(searchTerm)
        ) {
          removeClient = false;
        }
      });
      if (removeClient) {
        this.filteredClientContractsMap.delete(key);
      }
    });

    this.clientContractsMap$ = of(this.filteredClientContractsMap);
    this.generateTocLinks(this.filteredClientContractsMap);
  }

  private copyContracts() {
    this.filteredClientContractsMap = new Map(
      JSON.parse(
        JSON.stringify(Array.from(this.clientContractsMap))
      ) as Iterable<readonly [string, IContractExt[]]>
    );
  }

  private initSearchDebounce() {
    // https://m.clearbluedesign.com/how-to-simple-angular-debounce-using-rxjs-e7b86fde6167
    this.searchTermNotifier.pipe(debounceTime(300)).subscribe(() => {
      this.filterContracts(this.searchTerm);
    });
  }

  public getClientTooltip(contract: IContractExt): string {
    return this.translator.instant(
      'CLIENTS.CLIENT_TOOLTIP',
      contract
    ) as string;
  }

  public getApiTooltip(contract: IContractExt): string {
    return this.translator.instant('CLIENTS.API_TOOLTIP', contract) as string;
  }
  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('CLIENTS.TITLE') as string,
      subtitle: this.translator.instant('CLIENTS.SUBTITLE') as string
    });
  }

  setSection(contract: IContractExt, sectionName: ISection): void {
    contract.section =
      contract.section === sectionName ? contract.section : sectionName;
    this.cdr.detectChanges();
  }

  getColorForLabel(
    status: IApiStatus | IClientStatus
  ): IStatusColors | undefined {
    return statusColorMap.get(status);
  }

  formatClientContractTitle(key: string): string {
    return (
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      this.clientContractsMap.get(key)![0].client.client.name +
      ' - ' +
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      this.clientContractsMap.get(key)![0].client.version
    );
  }

  private generateTocLinks(clientContractsMap: Map<string, IContractExt[]>) {
    this.tocLinks = [];

    clientContractsMap.forEach((value: IContractExt[], key: string) => {
      this.tocLinks.push({
        name: this.formatClientContractTitle(key),
        destination: this.tocService.formatClientId(value[0]),
        subLinks: this.generateTocSubLinks(value)
      });
    });
  }

  private generateTocSubLinks(contracts: IContractExt[]): ITocLink[] {
    const subLinks: ITocLink[] = [];

    contracts.forEach((contract: IContractExt) => {
      subLinks.push({
        name: this.formatApiVersionPlanTitle(contract),
        destination: this.tocService.formatApiVersionPlanId(contract)
      });
    });

    return subLinks;
  }

  formatApiVersionPlanTitle(contract: IContractExt): string {
    return `${contract.api.api.name} ${contract.api.version} - ${contract.plan.plan.name}`;
  }

  unregister(contract: IContractExt, clientNameVersion: string): void {
    const dialogRef = this.dialog.open(UnregisterClientComponent, {
      autoFocus: false
    });
    dialogRef.componentInstance.contract = contract;
    dialogRef.componentInstance.clientNameVersion = {
      value: this.formatClientContractTitle(clientNameVersion)
    };

    dialogRef.componentInstance.unregisterEmitter.subscribe(() => {
      this.snackbarService.showPrimarySnackBar(
        this.translator.instant('CLIENTS.CLIENT_REMOVED') as string
      );
      this.fetchContracts();

      dialogRef.close();
    });
  }

  private isDeleteAllowed(contract: IContract): boolean {
    const clientAdminOrganizations =
      this.permissionsService.getAllowedOrganizations({
        name: 'clientAdmin'
      } as IPermission);
    return (
      contract.client.status !== 'Retired' &&
      clientAdminOrganizations.includes(contract.client.client.organization.id)
    );
  }

  onSetSection($event: { contract: IContractExt; section: ISection }): void {
    this.setSection($event.contract, $event.section);
    this.cdr.detectChanges();
  }
}
