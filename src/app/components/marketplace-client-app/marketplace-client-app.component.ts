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

import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { BackendService } from '../../services/backend/backend.service';
import { MatTableDataSource } from '@angular/material/table';
import {
  IClient,
  IClientSummary,
  IOrganizationSummary
} from '../../interfaces/ICommunication';
import { SnackbarService } from '../../services/snackbar/snackbar.service';
import { HttpErrorResponse } from '@angular/common/http';
import { switchMap } from 'rxjs/operators';
import { forkJoin } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { OrganizationService } from '../../services/org/organization.service';
import { SelectionModel } from '@angular/cdk/collections';

@Component({
  selector: 'app-marketplace-client-app',
  templateUrl: './marketplace-client-app.component.html',
  styleUrls: ['./marketplace-client-app.component.scss']
})
export class MarketplaceClientAppComponent implements OnInit {
  displayedColumns: string[] = ['select', 'name'];
  dataSource = new MatTableDataSource<IClientSummary>([]);
  selection = new SelectionModel<IClientSummary>(false);
  clientName = '';
  organizationId = '';
  organizations: IOrganizationSummary[] = [];
  clients: IClientSummary[] = [];

  @Output() selectedClient = new EventEmitter<IClientSummary>();

  constructor(
    private backend: BackendService,
    private snackbar: SnackbarService,
    private translator: TranslateService,
    private orgService: OrganizationService
  ) {}

  ngOnInit(): void {
    this.createOrgAndLoadClients();
  }

  /**
   * This method toggles between a selected/unselected client and
   * emits the value to the parent component.
   * @param client - the client that should be selected
   */
  public selectClient(client: IClientSummary): void {
    if (this.selection.isSelected(client)) {
      this.clearSelection();
    } else {
      this.clientName = client.name;
      this.organizationId = client.organizationId;
      this.selection.select(client);
      this.selectedClient.emit(client);
    }
  }

  /**
   * Add a new client and refresh table after that
   */
  public addClient(): void {
    const orgId = this.getOrgIdForClient();
    this.backend
      .createClient(orgId, this.clientName)
      .pipe(
        switchMap((client: IClient) => {
          console.log(`${client.organization.id}/${client.id} created`);
          this.snackbar.showPrimarySnackBar(
            this.translator.instant('COMMON.SUCCESS') as string
          );
          return this.loadClients();
        })
      )
      .subscribe({
        next: (results: [IOrganizationSummary[], IClientSummary[]]) => {
          this.createTableView(results);
        },
        error: (error: HttpErrorResponse) => {
          this.snackbar.showErrorSnackBar(error.message, error);
        }
      });
  }

  public applyFilter(event: Event): void {
    this.clearSelection();
    // https://material.angular.io/components/table/examples
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  /**
   * Try to create a user organization every time.
   * Refactor this later to accept org-names
   * @private
   */
  private createOrgAndLoadClients() {
    this.orgService
      .createHomeOrgIfNotExists()
      .pipe(
        switchMap(() => {
          return this.loadClients();
        })
      )
      .subscribe({
        next: (results: [IOrganizationSummary[], IClientSummary[]]) => {
          this.createTableView(results);
        },
        error: (error) => {
          console.warn(error);
          this.snackbar.showErrorSnackBar(
            this.translator.instant('COMMON.ERROR') as string
          );
        }
      });
  }

  private loadClients() {
    return forkJoin([
      this.backend.getClientOrgs(),
      // we only need clients with clientEdit otherwise you cannot create a contract with that client
      this.backend.getEditableClients()
    ]);
  }

  private createTableView(results: [IOrganizationSummary[], IClientSummary[]]) {
    this.clients = results[1];
    this.organizations = results[0];
    if (this.organizations.length > 1) {
      this.displayedColumns = ['select', 'org-name', 'name'];
    }
    this.dataSource = new MatTableDataSource(results[1]);
    if (this.clients.length === 1) {
      // if we only have one client we can select it automatically
      this.selectClient(this.clients[0]);
    }
  }

  /**
   * The create client button is disabled when...
   * no name was chosen
   * the client already exists
   * there are multiple orgs and no org is selected
   *
   * @return true if the create client button should be disabled
   */
  public isCreateButtonDisabled(): boolean {
    return (
      this.clientName.length === 0 ||
      this.clients.some(
        (clientSummary) =>
          clientSummary.name === this.clientName &&
          clientSummary.organizationId === this.getOrgIdForClient()
      ) ||
      (this.organizations.length > 1 && !this.organizationId)
    );
  }

  /**
   * If there are multiple orgs available use the one the user selected
   * otherwise use the default home org of the user
   *
   * @returns the org id in which the client should be created
   */
  private getOrgIdForClient(): string {
    return this.organizations.length === 1
      ? this.organizations[0].id
      : this.organizationId;
  }

  /**
   * Clears the selected client.
   * This must be emitted to the parent component.
   */
  private clearSelection(): void {
    this.selection.clear();
    this.selectedClient.emit(undefined);
  }
}
