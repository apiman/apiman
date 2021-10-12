import {Component, EventEmitter, OnInit} from '@angular/core';
import {IContractExt} from "../../../interfaces/IContractExt";
import {IAction} from "../../../interfaces/ICommunication";
import {BackendService} from "../../../services/backend/backend.service";
import {flatMap} from "rxjs/internal/operators";

@Component({
  selector: 'app-unregister-client',
  templateUrl: './unregister-client.component.html',
  styleUrls: ['./unregister-client.component.scss']
})
export class UnregisterClientComponent implements OnInit {
  contract!: IContractExt;
  clientNameVersion = {value: ''};

  unregisterEmitter = new EventEmitter();

  constructor(private backend: BackendService) { }

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
      flatMap(() => this.backend.deleteClient(action.organizationId, action.entityId))
    ).subscribe(() => {
      this.unregisterEmitter.emit();
    });
  }
}
