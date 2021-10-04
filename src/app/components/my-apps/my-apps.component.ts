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
  IClientSummaryBean,
  IClientVersionSummaryBean,
  IContract,
  IContractSummary,
} from '../../interfaces/ICommunication';
import { IContractExt } from '../../interfaces/IContractExt';
import {PolicyService} from "../../services/policy/policy.service";
import {map, switchMap} from "rxjs/operators";
import {forkJoin} from "rxjs";
import {flatArray} from "../../shared/utility";
import {SpinnerService} from "../../services/spinner/spinner.service";

@Component({
  selector: 'app-my-apps',
  templateUrl: './my-apps.component.html',
  styleUrls: ['./my-apps.component.scss'],
})
export class MyAppsComponent implements OnInit {
  contracts: IContractExt[] = [];
  clientContractsMap = new Map<string, IContractExt[]>();
  contractsLoaded: boolean= false;

  tmpUrl = 'https://pbs.twimg.com/media/Ez-AaifWYAIiFSQ.jpg';
  tmpUrl2 =
    'https://cdn0.iconfinder.com/data/icons/customicondesignoffice5/256/examples.png';

  constructor(
    private spinnerService: SpinnerService,
    private heroService: HeroService,
    private translator: TranslateService,
    private backend: BackendService,
    private policyService: PolicyService,
  ) {}

  ngOnInit(): void {
    this.setUpHero();
    this.fetchContracts();
  }
  // Detailed explanation of request chain: https://stackoverflow.com/questions/69421293/how-to-chain-requests-correctly-with-rxjs
  private fetchContracts() {
    this.spinnerService.startWaiting();
    this.contractsLoaded = false;

    this.backend
      .getEditableClients()
      .pipe(
        switchMap((clientSummaries: IClientSummaryBean[]) => {
          return forkJoin(clientSummaries.map(clientSummary => {
            return this.backend.getClientVersions(
              clientSummary.organizationId,
              clientSummary.id
            );
          }))
        }),
        switchMap((nestedClientVersionSummaries: IClientVersionSummaryBean[][])=> {
          const clientVersionSummaries: IClientVersionSummaryBean[] = flatArray(nestedClientVersionSummaries)
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
              this.backend.getManagedApiEndpoint(orgId, contract.api.api.id, contract.api.version)
            ]).pipe(
              map(([planPolicies, apiPolicies,endpoint]) => {
                return {
                  ...contract,
                  policies: planPolicies.concat(apiPolicies),
                  section: 'summary',
                  managedEndpoint: endpoint.managedEndpoint
                } as IContractExt;
              })
            )
          }))
        })
      ).subscribe((contracts) => {
        this.spinnerService.stopWaiting();
        this.contractsLoaded = true;
        this.extractContracts(contracts)
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
      const clientNameVersionMapped =
        contract.client.client.name + ':' + contract.client.version;
      const foundContracts = this.clientContractsMap.get(
        clientNameVersionMapped
      );

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
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('APPS.TITLE'),
      subtitle: this.translator.instant('APPS.SUBTITLE'),
    });
  }

  setSection(api: any, sectionName: ISection) {
    api.section = api.section === sectionName ? api.section : sectionName;
  }

  getColorForLabel(status: IApiStatus | IClientStatus) {
    return statusColorMap.get(status);
  }
}
