import {Component, EventEmitter, Output, OnInit} from '@angular/core';
import {BackendService} from '../../services/backend/backend.service';
import {MatTableDataSource} from '@angular/material/table';
import {
  IClient,
  IClientSummary,
  IOrganizationSummary
} from '../../interfaces/ICommunication';
import {SnackbarService} from '../../services/snackbar/snackbar.service';
import {HttpErrorResponse} from '@angular/common/http';
import {catchError, switchMap} from 'rxjs/operators';
import {forkJoin, of} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';

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
  organizationId = '';
  organizations: IOrganizationSummary[] = [];

  @Output() selectedClients = new EventEmitter<Set<IClientSummary>>();

  constructor(
    private backend: BackendService,
    private snackbar: SnackbarService,
    private translator: TranslateService
  ) {}

  ngOnInit(): void {
    this.createOrgAndLoadClients();
  }

  public selectClient(client: IClientSummary): void {
    // always clear, because at the moment we only allow one client to be selected
    this.clickedRows.clear();
    this.clickedRows.add(client);
    this.clientName = client.name;
    this.organizationId = client.organizationId;
    this.selectedClients.emit(this.clickedRows);
  }

  /**
   * Add a new client and refresh table after that
   */
  public addClient(): void {
    const orgId: string =
      this.organizations.length > 1
        ? this.organizationId
        : this.organizations[0].id;
    this.backend.createClient(orgId, this.clientName)
      .pipe(
        switchMap((client: IClient) => {
          this.snackbar.showPrimarySnackBar(
            this.translator.instant('COMMON.SUCCESS')
          );
          return this.loadClients();
        })
      )
      .subscribe(
        (results: [IOrganizationSummary[], IClientSummary[]]) => {
          this.createTableView(results);
        },
        (error) => {
          this.snackbar.showErrorSnackBar(error);
        }
      );
  }

  public applyFilter(event: Event): void {
    this.clickedRows.clear();
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
            this.translator.instant('COMMON.ERROR')
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
    const organizations = results[0];
    const clients = results[1];
    this.organizations = results[0];
    if (organizations.length > 1) {
      this.displayedColumns = ['org-name', 'name'];
    }
    this.dataSource = new MatTableDataSource(results[1]);
    if (clients.length === 1) {
      // if we only have one client we can select it automatically
      this.selectClient(clients[0]);
    }
  }
}
