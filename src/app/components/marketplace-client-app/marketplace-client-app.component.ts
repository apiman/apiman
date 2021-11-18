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
import { catchError, switchMap } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { KeycloakHelperService } from '../../services/keycloak-helper/keycloak-helper.service';

@Component({
  selector: 'app-marketplace-client-app',
  templateUrl: './marketplace-client-app.component.html',
  styleUrls: ['./marketplace-client-app.component.scss']
})
export class MarketplaceClientAppComponent implements OnInit {
  displayedColumns: string[] = ['name'];
  dataSource = new MatTableDataSource<IClientSummary>([]);
  clickedRows = new Set<IClientSummary>();
  clientName = '';
  organizationId = this.keycloakHelper.getUsername();
  organizations: IOrganizationSummary[] = [];
  clients: IClientSummary[] = [];

  @Output() selectedClients = new EventEmitter<Set<IClientSummary>>();

  constructor(
    private backend: BackendService,
    private snackbar: SnackbarService,
    private translator: TranslateService,
    private keycloakHelper: KeycloakHelperService
  ) {}

  ngOnInit(): void {
    this.createOrgAndLoadClients();
  }

  public clickClient(client: IClientSummary): void {
    this.clientName = client.name;
    this.organizationId = client.organizationId;
    this.selectClient(client);
  }

  private selectClient(client: IClientSummary): void {
    // always clear, because at the moment we only allow one client to be selected
    this.clickedRows.clear();
    this.clickedRows.add(client);
    this.selectedClients.emit(this.clickedRows);
  }

  /**
   * Add a new client and refresh table after that
   */
  public addClient(): void {
    const orgId: string =
      this.organizations.length > 1
        ? this.organizationId
        : this.keycloakHelper.getUsername();
    this.backend
      .createClient(orgId, this.clientName)
      .pipe(
        switchMap((client: IClient) => {
          console.log(client.id + 'created');
          this.snackbar.showPrimarySnackBar(
            this.translator.instant('COMMON.SUCCESS') as string
          );
          return this.loadClients();
        })
      )
      .subscribe(
        (results: [IOrganizationSummary[], IClientSummary[]]) => {
          this.createTableView(results);
        },
        (error: HttpErrorResponse) => {
          this.snackbar.showErrorSnackBar(error.message, error);
        }
      );
  }

  public applyFilter(event: Event): void {
    this.clickedRows.clear();
    // https://material.angular.io/components/table/examples
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    // if only one client is left we select it
    if (this.dataSource.filteredData.length === 1) {
      this.selectClient(this.dataSource.filteredData[0]);
    }
  }

  /**
   * Try to create a user organization every time.
   * Refactor this later to accept org-names
   * @private
   */
  private createOrgAndLoadClients() {
    this.backend
      .createOrganization()
      .pipe(
        catchError((err: HttpErrorResponse) => {
          if (err.status !== 409) {
            console.error(err);
          }
          return of({});
        }),
        switchMap(() => {
          return this.loadClients();
        })
      )
      .subscribe(
        (results: [IOrganizationSummary[], IClientSummary[]]) => {
          this.createTableView(results);
        },
        (error) => {
          console.warn(error);
          this.snackbar.showErrorSnackBar(
            this.translator.instant('COMMON.ERROR') as string
          );
        }
      );
  }

  private loadClients() {
    return forkJoin([
      this.backend.getClientOrgs(),
      this.backend.getEditableClients()
    ]);
  }

  private createTableView(results: [IOrganizationSummary[], IClientSummary[]]) {
    this.clients = results[1];
    this.organizations = results[0];
    if (this.organizations.length > 1) {
      this.displayedColumns = ['org-name', 'name'];
    }
    this.dataSource = new MatTableDataSource(results[1]);
    if (this.clients.length === 1) {
      // if we only have one client we can select it automatically
      this.clickClient(this.clients[0]);
    }
  }

  public isCreateButtonDisabled(): boolean {
    return (
      this.clientName.length === 0 ||
      this.clients.some(
        (clientSummary) =>
          clientSummary.name === this.clientName &&
          clientSummary.organizationId === this.organizationId
      )
    );
  }
}
