import { Component, OnInit } from '@angular/core';
import { BackendService, Client } from '../../services/backend/backend.service';
import { MatTableDataSource } from '@angular/material/table';
import { switchMap } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-marketplace-client-app',
  templateUrl: './marketplace-client-app.component.html',
  styleUrls: ['./marketplace-client-app.component.scss'],
})
export class MarketplaceClientAppComponent implements OnInit {
  displayedColumns: string[] = ['name'];
  dataSource = new MatTableDataSource<Client>([]);
  clickedRows = new Set<Client>();
  clientName = '';

  constructor(
    private backend: BackendService,
    private translator: TranslateService
  ) {
    console.log(translator.instant('MPLACE.TITLE'));
  }

  ngOnInit(): void {
    this.loadClients();
  }

  public selectClient(client: Client): void {
    // always clear, because at the moment we only allow one client to be selected
    this.clickedRows.clear();
    this.clickedRows.add(client);
    this.clientName = client.name;
  }

  /**
   * Add a new client
   */
  public addClient(): void {
    this.backend
      .getClientOrgs()
      .pipe(
        switchMap((orgs) => {
          return this.backend.createClient(orgs[0].id, this.clientName);
        })
      )
      .subscribe(
        () => this.loadClients(),
        (error) => console.error(error)
      );
  }

  /**
   * Refresh list of clients
   * @private
   */
  private loadClients() {
    this.backend.getClients().subscribe(
      (clients: Client[]) => {
        this.dataSource = new MatTableDataSource(clients);
      },
      (error) => {
        console.error(error);
      }
    );
  }

  public applyFilter(event: Event) {
    // https://material.angular.io/components/table/examples
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }
}
