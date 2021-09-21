import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { BackendService } from '../../services/backend/backend.service';
import { MatTableDataSource } from '@angular/material/table';
import { IClient, IOrganizationSummary } from '../../interfaces/ICommunication';

@Component({
  selector: 'app-marketplace-client-app',
  templateUrl: './marketplace-client-app.component.html',
  styleUrls: ['./marketplace-client-app.component.scss'],
})
export class MarketplaceClientAppComponent implements OnInit {
  displayedColumns: string[] = ['name'];
  dataSource = new MatTableDataSource<IClient>([]);
  clickedRows = new Set<IClient>();
  clientName = '';
  organizationId = '';
  organizations: IOrganizationSummary[] = [];

  @Output() selectedClients = new EventEmitter<Set<IClient>>();

  constructor(private backend: BackendService) {}

  ngOnInit(): void {
    this.loadClients();
  }

  public selectClient(client: IClient): void {
    // always clear, because at the moment we only allow one client to be selected
    this.clickedRows.clear();
    this.clickedRows.add(client);
    this.clientName = client.name;
    this.selectedClients.emit(this.clickedRows);
  }

  /**
   * Add a new client
   */
  public addClient(): void {
    console.log(this.organizations);
    const orgId: string =
      this.organizations.length > 1
        ? this.organizationId
        : this.organizations[0].id;
    this.backend.createClient(orgId, this.clientName).subscribe(
      () => this.loadClients(),
      (error) => console.error(error)
    );
  }

  /**
   * Refresh list of clients
   * @private
   */
  private loadClients(): void {
    this.backend.getClientOrgs().subscribe(
      (orgs: IOrganizationSummary[]) => {
        this.organizations = orgs;
        if (orgs.length > 1) {
          this.displayedColumns = ['org-name', 'name'];
        }
      },
      (error) => console.error(error)
    );

    this.backend.getClients().subscribe(
      (clients: IClient[]) => {
        this.dataSource = new MatTableDataSource(clients);
      },
      (error) => console.error(error)
    );
  }

  public applyFilter(event: Event): void {
    this.clickedRows.clear();
    // https://material.angular.io/components/table/examples
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }
}
