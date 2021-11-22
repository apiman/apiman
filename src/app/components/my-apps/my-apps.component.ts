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
import { catchError, map, switchMap } from 'rxjs/operators';
import { merge, EMPTY, forkJoin } from 'rxjs';
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
  contracts: IContractExt[] = [];
  clientContractsMap!: Map<string, IContractExt[]>;
  contractsLoaded = false;

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
  }

  // Detailed explanation of request chain: https://stackoverflow.com/questions/69421293/how-to-chain-requests-correctly-with-rxjs
  private fetchContracts() {
    this.contracts = [];
    this.clientContractsMap = new Map<string, IContractExt[]>();

    this.spinnerService.startWaiting();
    this.contractsLoaded = false;
    merge(this.backend.getEditableClients(), this.backend.getViewableClients())
      .pipe(
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
          // Filter out contracts with apis which are not exposed in developer portal
          const filteredContracts: IContract[] = contracts.filter(
            (contract) => {
              return contract.api.exposeInPortal;
            }
          );
          return forkJoin(
            filteredContracts.map((contract) => {
              const orgId = contract.plan.plan.organization.id;
              return forkJoin([
                this.policyService.getPlanPolicies(
                  orgId,
                  contract.plan.plan.id,
                  contract.plan.version
                ),

                this.backend.getManagedApiEndpoint(
                  orgId,
                  contract.api.api.id,
                  contract.api.version
                ),
                this.apiService.isApiDocAvailable(contract.api)
              ]).pipe(
                map(([planPolicies, endpoint, docsAvailable]) => {
                  planPolicies.forEach((planPolicy) => {
                    this.policyService.initPolicy(planPolicy);
                  });

                  return {
                    ...contract,
                    policies: planPolicies,
                    section: 'summary',
                    managedEndpoint: endpoint.managedEndpoint,
                    docsAvailable: docsAvailable,
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
        this.generateTocLinks();
      });
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

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('APPS.TITLE') as string,
      subtitle: this.translator.instant('APPS.SUBTITLE') as string
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

  private generateTocLinks() {
    this.tocLinks = [];

    this.clientContractsMap.forEach((value: IContractExt[], key: string) => {
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
        this.translator.instant('APPS.APPLICATION_REMOVED') as string
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
      contract.client.status === 'Registered' &&
      clientAdminOrganizations.includes(contract.client.client.organization.id)
    );
  }

  onSetSection($event: { contract: IContractExt; section: ISection }): void {
    this.setSection($event.contract, $event.section);
    this.cdr.detectChanges();
  }
}
