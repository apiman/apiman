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
import { flatMap } from 'rxjs/internal/operators';
import { IContractExt } from '../../interfaces/IContractExt';

@Component({
  selector: 'app-my-apps',
  templateUrl: './my-apps.component.html',
  styleUrls: ['./my-apps.component.scss'],
})
export class MyAppsComponent implements OnInit {
  contracts: IContractExt[] = [];
  clientContractsMap = new Map<string, IContractExt[]>();

  tmpUrl = 'https://pbs.twimg.com/media/Ez-AaifWYAIiFSQ.jpg';
  tmpUrl2 =
    'https://cdn0.iconfinder.com/data/icons/customicondesignoffice5/256/examples.png';

  constructor(
    private heroService: HeroService,
    private translator: TranslateService,
    private backend: BackendService
  ) {}

  ngOnInit(): void {
    this.setUpHero();
    this.fetchContracts();
  }

  private fetchContracts() {
    this.backend
      .getEditableClients()
      .pipe(
        flatMap((clients: IClientSummaryBean[]) => {
          return clients;
        }),
        flatMap((clientSum: IClientSummaryBean) => {
          return this.backend.getClientVersions(
            clientSum.organizationId,
            clientSum.id
          );
        }),
        flatMap((versions: IClientVersionSummaryBean[]) => {
          return versions;
        }),
        flatMap((versionSum: IClientVersionSummaryBean) => {
          return this.backend.getContracts(
            versionSum.organizationId,
            versionSum.id,
            versionSum.version
          );
        }),
        flatMap((contracts: IContractSummary[]) => {
          return contracts;
        }),
        flatMap((contractSum: IContractSummary) => {
          return this.backend.getContract(
            contractSum.clientOrganizationId,
            contractSum.clientId,
            contractSum.clientVersion,
            contractSum.contractId
          );
        })
      )
      .subscribe((contracts: IContract[]) => {
        const extended = contracts as IContractExt[];
        this.extractContracts(extended);
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

      this.extendContract(contract);

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

  /**
   * Extends the ContractBean object with additional values
   * @param contract
   * @private
   */
  private extendContract(contract: IContract): IContractExt {
    const contractExt = contract as IContractExt;
    contractExt.section = 'summary';
    contractExt.policies = this.getPolicies();
    return contractExt;
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('APPS.TITLE'),
      subtitle: this.translator.instant('APPS.SUBTITLE'),
    });
  }

  private getPolicies() {
    return [];
  }

  setSection(api: any, sectionName: ISection) {
    api.section = api.section === sectionName ? api.section : sectionName;
  }

  getColorForLabel(status: IApiStatus | IClientStatus) {
    return statusColorMap.get(status);
  }
}
