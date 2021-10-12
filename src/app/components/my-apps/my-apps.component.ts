import { Component, OnInit } from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import { BackendService } from '../../services/backend/backend.service';
import { ISection } from '../../interfaces/ISection';
import {
  IApiStatus,
  IClientStatus,
  statusColorMap,
} from '../../interfaces/IStatus';
import {
  IClientSummary, IClientVersionSummary,
  IContract,
  IContractSummary, IPermission, IUserPermissions,
} from '../../interfaces/ICommunication';
import { IContractExt } from '../../interfaces/IContractExt';
import {PolicyService} from "../../services/policy/policy.service";
import {map, switchMap} from "rxjs/operators";
import {forkJoin} from "rxjs";
import {flatArray} from "../../shared/utility";
import {SpinnerService} from "../../services/spinner/spinner.service";
import {ApiService} from "../../services/api/api.service";
import {ITocLink} from "../../interfaces/ITocLink";
import {TocService} from "../../services/toc.service";
import {MatDialog} from "@angular/material/dialog";
import {UnregisterClientComponent} from "../dialogs/unregister-client/unregister-client.component";
import {SnackbarService} from "../../services/snackbar/snackbar.service";

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

  noDataFound = false;

  clientAdminProfile = false;

  constructor(
    private spinnerService: SpinnerService,
    private heroService: HeroService,
    private translator: TranslateService,
    private backend: BackendService,
    private policyService: PolicyService,
    private apiService: ApiService,
    public tocService: TocService,
    private dialog: MatDialog,
    private snackbarService: SnackbarService
  ) {
  }

  ngOnInit(): void {
    this.setUpHero();
    this.fetchProfiles();
    this.fetchContracts();
  }

  // Detailed explanation of request chain: https://stackoverflow.com/questions/69421293/how-to-chain-requests-correctly-with-rxjs
  private fetchContracts() {
    this.contracts = [];
    this.clientContractsMap = new Map<string, IContractExt[]>();

    this.spinnerService.startWaiting();
    this.contractsLoaded = false;

    this.backend
      .getEditableClients()
      .pipe(
        switchMap((clientSummaries: IClientSummary[]) => {
          if (clientSummaries.length === 0) {
            this.spinnerService.stopWaiting();
            this.noDataFound = true;
            this.contractsLoaded = true;
          }

          return forkJoin(clientSummaries.map(clientSummary => {
            return this.backend.getClientVersions(
              clientSummary.organizationId,
              clientSummary.id
            );
          }))
        }),
        switchMap((nestedClientVersionSummaries: IClientVersionSummary[][])=> {
          const clientVersionSummaries: IClientVersionSummary[] = flatArray(nestedClientVersionSummaries)
          return forkJoin(clientVersionSummaries.map(clientVersionSummary => {
              return this.backend.getContracts(
                clientVersionSummary.organizationId,
                clientVersionSummary.id,
                clientVersionSummary.version
              );
            })
          );
        }),
        switchMap((nestedContractSummaries: IContractSummary[][]) => {
          const contractSummaries: IContractSummary[] = flatArray(nestedContractSummaries)
          return forkJoin(contractSummaries.map(contractSummary => {
            return this.backend.getContract(
              contractSummary.clientOrganizationId,
              contractSummary.clientId,
              contractSummary.clientVersion,
              contractSummary.contractId
            );
          }))
        }),
        switchMap((contracts: IContract[]) => {
          return forkJoin(contracts.map(contract => {
            const orgId = contract.plan.plan.organization.id;
            return forkJoin([
              this.policyService.getPlanPolicies(orgId, contract.plan.plan.id, contract.plan.version),
              this.policyService.getApiPolicies(orgId, contract.api.api.id, contract.api.version),
              this.backend.getManagedApiEndpoint(orgId, contract.api.api.id, contract.api.version),
              this.apiService.isApiDocAvailable(contract.api)
            ]).pipe(
              map(([planPolicies, apiPolicies, endpoint, docsAvailable]) => {
                return {
                  ...contract,
                  policies: planPolicies.concat(apiPolicies),
                  section: 'summary',
                  managedEndpoint: endpoint.managedEndpoint,
                  docsAvailable: docsAvailable
                } as IContractExt;
              })
            )
          }))
        })
      ).subscribe((contracts) => {
        this.spinnerService.stopWaiting();
        this.contractsLoaded = true;
        this.extractContracts(contracts);
        this.generateTocLinks();
      });
  }

  /**
   * Contracts will be stored in a map with schema {client.name:contract.version,contract-object}
   * @param contracts
   * @private
   */
  private extractContracts(contracts: IContractExt[]) {
    this.contracts = this.contracts.concat(contracts);

    this.contracts.forEach((contract: IContractExt) => {
      const clientNameVersionMapped = (contract.client.client.name + ':' + contract.client.version).toLowerCase();
      const foundContracts = this.clientContractsMap.get(clientNameVersionMapped);

      if (foundContracts) {
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
      } else {
        this.clientContractsMap.set(clientNameVersionMapped, [contract]);
      }
    });

    // Sort Clients by name
    this.clientContractsMap = new Map([...this.clientContractsMap.entries()].sort());
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('APPS.TITLE'),
      subtitle: this.translator.instant('APPS.SUBTITLE')
    });
  }

  setSection(api: any, sectionName: ISection) {
    api.section = api.section === sectionName ? api.section : sectionName;
  }

  getColorForLabel(status: IApiStatus | IClientStatus) {
    return statusColorMap.get(status);
  }

  formatClientContractTitle(key: string): string {
    return this.clientContractsMap.get(key)![0].client.client.name + ' - ' + this.clientContractsMap.get(key)![0].client.version;
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

  private generateTocSubLinks(contracts: IContractExt[]): ITocLink[]{
    let subLinks: ITocLink[] = [];

    contracts.forEach((contract: IContractExt) => {
      subLinks.push({
        name: this.formatApiVersionPlanTitle(contract),
        destination: this.tocService.formatApiVersionPlanId(contract)
      })
    });

    return subLinks;
  }

  formatApiVersionPlanTitle(contract: IContractExt) {
    return `${contract.api.api.name} ${contract.api.version} - ${contract.plan.plan.name}`;
  }

  unregister(contract: IContractExt, clientNameVersion: string) {
    const dialogRef = this.dialog.open(UnregisterClientComponent, {autoFocus: false});
    dialogRef.componentInstance.contract = contract;
    dialogRef.componentInstance.clientNameVersion = {value: this.formatClientContractTitle(clientNameVersion)}

    dialogRef.componentInstance.unregisterEmitter.subscribe(() => {
      this.snackbarService.showPrimarySnackBar(this.translator.instant('APPS.CLIENT_REMOVED'))
      this.fetchContracts();

      dialogRef.close();
    });
  }

  private fetchProfiles() {
    this.backend.getPermissions().subscribe((response: IUserPermissions) => {
      const found = response.permissions.find((p: IPermission) => {
        return p.name === 'clientAdmin';
      })

      if (found)
        this.clientAdminProfile = true;
    });
  }
}
