import { Component, Input, OnInit } from '@angular/core';
import {IContractExt} from '../../interfaces/IContractExt';
import {IApiVersionEndpointSummary} from "../../interfaces/ICommunication";
import {BackendService} from "../../services/backend/backend.service";
import {SnackbarService} from "../../services/snackbar/snackbar.service";

@Component({
  selector: 'app-my-apps-summary',
  templateUrl: './my-apps-summary.component.html',
  styleUrls: ['./my-apps-summary.component.scss'],
})
export class MyAppsSummaryComponent implements OnInit {
  @Input() contract?: IContractExt;
  managedEndpoint: string = '';
  constructor(private backendService: BackendService,
              private snackbarService: SnackbarService) {}

  ngOnInit(): void {
    if (this.contract) {
      this.backendService
        .getManagedApiEndpoint(
          this.contract.api.api.organization.id,
          this.contract.api.api.id,
          this.contract.api.version
        )
        .subscribe(
          (endpoint: IApiVersionEndpointSummary) => {
            this.managedEndpoint = endpoint.managedEndpoint;
          },
          (error) => this.snackbarService.showErrorSnackBar(error.message, error)
        );
    }
  }
}
