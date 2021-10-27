import {Component, EventEmitter, OnInit} from '@angular/core';
import {IContractExt} from "../../../interfaces/IContractExt";
import {IAction} from "../../../interfaces/ICommunication";
import {BackendService} from "../../../services/backend/backend.service";
import {flatMap} from "rxjs/internal/operators";
import {catchError} from "rxjs/operators";
import {EMPTY} from "rxjs";
import {SnackbarService} from "../../../services/snackbar/snackbar.service";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-unregister-client',
  templateUrl: './unregister-client.component.html',
  styleUrls: ['./unregister-client.component.scss']
})
export class UnregisterClientComponent implements OnInit {
  contract!: IContractExt;
  clientNameVersion = {value: ''};

  unregisterEmitter = new EventEmitter();

  constructor(private backend: BackendService,
              private snackbarService: SnackbarService,
              private translator: TranslateService) { }

  ngOnInit(): void { }

  onUnregister() {
    const action: IAction = {
      type: 'unregisterClient',
      organizationId: this.contract.client.client.organization.id,
      entityId: this.contract.client.client.id,
      entityVersion: this.contract.client.version
    }

    this.backend.breakAllContracts(action.organizationId, action.entityId, action.entityVersion).pipe(
      flatMap(() => this.backend.sendAction(action)),
      flatMap(() => this.backend.deleteClient(action.organizationId, action.entityId)),
      catchError(() => {
        console.warn('Deleting Client failed');
        this.snackbarService.showErrorSnackBar(this.translator.instant('APPS.REMOVE_CLIENT_FAILED'));
        return EMPTY;
      })
    ).subscribe(() => {
      this.unregisterEmitter.emit();
    });
  }
}
