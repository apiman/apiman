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
import { IAction, IOrganization } from '../../interfaces/ICommunication';
import { IContractExt } from '../../interfaces/IContractExt';
import { PolicyService } from '../../services/policy/policy.service';
import {
  catchError,
  debounceTime,
  map,
  retry,
  shareReplay,
  switchMap,
  tap
} from 'rxjs/operators';
import { EMPTY, forkJoin, Observable, of, Subject } from 'rxjs';
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
import { ClientService } from '../../services/client/client.service';
import { IClientVersionExt } from '../../interfaces/IClientVersionSummaryExt';
import { ContractService } from '../../services/contract/contract.service';
import { OrganizationService } from '../../services/org/organization.service';
import { BreakContractComponent } from '../dialogs/break-contract/break-contract.component';

@Component({
  selector: 'app-my-apps',
  templateUrl: './my-apps.component.html',
  styleUrls: ['./my-apps.component.scss']
})
export class MyAppsComponent implements OnInit {
  organizationCount = 0;
  contractsLoaded = false;
  contractsFiltered = false;
  searchTerm = '';
  searchTermNotifier = new Subject();

  organizations$: Observable<IOrganization[]> = of([]);
  allOrganizations$: Observable<IOrganization[]> = of([]);
  clients$: Observable<IClientVersionExt[]> = of([]);
  allClients$: Observable<IClientVersionExt[]> = of([]);
  contracts$: Observable<IContractExt[]> = of([]);
  allContracts$: Observable<IContractExt[]> = of([]);

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
    private permissionsService: PermissionsService,
    private clientService: ClientService,
    private contractService: ContractService,
    private orgService: OrganizationService
  ) {}

  ngOnInit(): void {
    this.setUpHero();
    this.fetchData();
    this.initSearchDebounce();
  }

  private fetchData() {
    this.spinnerService.startWaiting();

    this.clientService
      .getExtendedClientVersions()
      .pipe(
        switchMap((clients: IClientVersionExt[]) => {
          this.allClients$ = of(clients);
          this.allOrganizations$ = this.orgService
            .getOrganizationsFromClientVersions(clients)
            .pipe(shareReplay());
          return this.allOrganizations$;
        }),
        switchMap(() => this.allClients$),
        switchMap((extendedClientVersions: IClientVersionExt[]) => {
          this.allContracts$ = this.contractService
            .getExtendedContractsFromClients(extendedClientVersions)
            .pipe(shareReplay());
          return this.allContracts$;
        }),
        catchError((err) => {
          console.warn(err);
          this.stopMainRequest(true);
          return EMPTY;
        })
      )
      .subscribe({
        next: () => {
          this.fetchPolicyProbes();
        },
        complete: () => {
          this.stopMainRequest();
          this.copyData();
          this.generateTocLinks();
        }
      });
  }

  private copyData() {
    this.organizations$ = this.allOrganizations$;
    this.clients$ = this.allClients$;
    this.contracts$ = this.allContracts$;
  }

  // TODO: Remove nested subscriptions if possible
  private fetchPolicyProbes() {
    this.allContracts$
      .pipe(
        switchMap((contracts: IContractExt[]) => {
          contracts.forEach((contract: IContractExt) => {
            contract.policies.forEach((policy: IPolicyExt) => {
              this.policyService
                .getPolicyProbe(contract, policy)
                .pipe(
                  retry(2),
                  catchError((err) => {
                    console.warn(err);
                    return EMPTY;
                  })
                )
                .subscribe({
                  next: (probe: IPolicyProbe) => {
                    policy.probe = probe;
                    this.policyService.setGaugeDataForPolicy(policy);
                  },
                  complete: () => {
                    policy.probeRequestFinished = true;
                  }
                });
            });
          });
          return EMPTY;
        })
      )
      .subscribe();
  }

  private stopMainRequest(setError = false) {
    this.spinnerService.stopWaiting();
    this.contractsLoaded = true;

    this.error = setError;
  }

  public resetSearch() {
    this.searchTerm = '';
    this.organizations$ = this.allOrganizations$;
    this.clients$ = this.allClients$;
    this.contracts$ = this.allContracts$;
    this.contractsFiltered = false;
  }

  private filterData() {
    this.searchTerm = this.searchTerm.toLowerCase();
    forkJoin([this.allOrganizations$, this.allClients$, this.allContracts$])
      .pipe(
        map(([orgs, clients, contracts]) => {
          contracts = contracts.filter(
            (contract) =>
              contract.client.client.name
                .toLowerCase()
                .includes(this.searchTerm) ||
              contract.api.api.name.toLowerCase().includes(this.searchTerm)
          );
          const availableClients: Set<string> = new Set(
            contracts.map(
              (contract) =>
                contract.client.client.organization.id +
                contract.client.client.name +
                contract.client.version
            )
          );
          clients = clients.filter(
            (client) =>
              availableClients.has(
                client.client.organization.id +
                  client.client.id +
                  client.version
              ) || client.client.name.toLowerCase().includes(this.searchTerm)
          );
          const availableOrgs: Set<string> = new Set(
            clients.map((client) => client.client.organization.id)
          );
          orgs = orgs.filter((org) => availableOrgs.has(org.id));
          return [orgs, clients, contracts];
        })
      )
      .subscribe(([orgs, clients, contracts]) => {
        this.organizations$ = of(orgs as IOrganization[]);
        this.clients$ = of(clients as IClientVersionExt[]);
        this.contracts$ = of(contracts as IContractExt[]);
        this.contractsFiltered = true;
      });
  }

  private initSearchDebounce() {
    // https://m.clearbluedesign.com/how-to-simple-angular-debounce-using-rxjs-e7b86fde6167
    this.searchTermNotifier.pipe(debounceTime(300)).subscribe(() => {
      this.filterData();
    });
  }

  public getClientTooltip(extendedClientVersion: IClientVersionExt): string {
    return this.translator.instant(
      'CLIENTS.CLIENT_TOOLTIP',
      extendedClientVersion
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

  formatClientContractTitle(extendedClientVersion: IClientVersionExt): string {
    return `${extendedClientVersion.client.name} - ${extendedClientVersion.version}`;
  }

  private generateTocLinks() {
    this.tocLinks = [];
    this.organizations$
      .pipe(
        switchMap((orgs: IOrganization[]) => {
          if (orgs.length > 1) {
            orgs.forEach((org: IOrganization) => {
              this.tocLinks.push({
                name: org.name,
                destination: org.id,
                subLinks: []
              });
            });
          }

          return this.clients$;
        }),
        switchMap((extendedClientVersions: IClientVersionExt[]) => {
          extendedClientVersions.forEach(
            (extendedClientVersion: IClientVersionExt) => {
              const destination: ITocLink[] =
                this.tocLinks.find((tocLink: ITocLink) => {
                  return (
                    tocLink.name ==
                    extendedClientVersion.client.organization.name
                  );
                })?.subLinks || this.tocLinks;

              const clientTocLink: ITocLink = {
                name: `${extendedClientVersion.client.name} - ${extendedClientVersion.version}`,
                destination: this.tocService.formatClientId(
                  extendedClientVersion
                ),
                subLinks: []
              };

              destination.push(clientTocLink);
            }
          );
          return this.contracts$;
        }),
        switchMap((extendedContracts: IContractExt[]) => {
          extendedContracts.forEach((extendedContract: IContractExt) => {
            const orgTocLink: ITocLink | undefined = this.tocLinks.find(
              (tocLink: ITocLink) => {
                return (
                  tocLink.name ===
                  extendedContract.client.client.organization.name
                );
              }
            );
            const clientTocLink: ITocLink | undefined = (
              orgTocLink?.subLinks || this.tocLinks
            ).find((tocLink: ITocLink) => {
              return (
                tocLink.name ===
                `${extendedContract.client.client.name} - ${extendedContract.client.version}`
              );
            });

            clientTocLink?.subLinks.push({
              name: `${extendedContract.api.api.name} ${extendedContract.api.version} - ${extendedContract.plan.plan.name}`,
              destination:
                this.tocService.formatApiVersionPlanId(extendedContract),
              subLinks: []
            });
          });
          return EMPTY;
        })
      )
      .subscribe();
  }

  contractCount(clientVersionId: number): number {
    let count = 0;
    this.contracts$
      .pipe(
        tap((contracts: IContractExt[]) => {
          count = contracts.filter((contract) => {
            return contract.client.id === clientVersionId;
          }).length;
        })
      )
      .subscribe();
    return count;
  }

  formatApiVersionPlanTitle(contract: IContractExt): string {
    return `${contract.api.api.name} ${contract.api.version} - ${contract.plan.plan.name}`;
  }

  register(extendedClientVersion: IClientVersionExt) {
    const action: IAction = {
      type: 'registerClient',
      organizationId: extendedClientVersion.client.organization.id,
      entityId: extendedClientVersion.client.id,
      entityVersion: extendedClientVersion.version
    };
    this.clientService.registerClient(action).subscribe(() => {
      console.info(
        `Client ${action.organizationId}/${action.entityId}/${action.entityVersion} successfully registered`
      );
      extendedClientVersion.status = 'Registered';
    });
  }

  unregister(extendedClientVersion: IClientVersionExt): void {
    const dialogRef = this.dialog.open(UnregisterClientComponent, {
      data: {
        clientVersion: extendedClientVersion,
        clientName: this.formatClientContractTitle(extendedClientVersion)
      },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((clientDeleted) => {
      if (clientDeleted) {
        this.snackbarService.showPrimarySnackBar(
          this.translator.instant('CLIENTS.CLIENT_DELETED') as string
        );
        this.fetchData();
      }
    });
  }

  breakContract(contract: IContractExt): void {
    const dialogRef = this.dialog.open(BreakContractComponent, {
      data: { contract: contract },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((contractDeleted: boolean) => {
      if (contractDeleted) {
        this.snackbarService.showPrimarySnackBar(
          this.translator.instant('CLIENTS.CONTRACT_DELETED') as string
        );
        this.fetchData();
      }
    });
  }

  onSetSection($event: { contract: IContractExt; section: ISection }): void {
    this.setSection($event.contract, $event.section);
    this.cdr.detectChanges();
  }
}
