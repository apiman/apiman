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

import { Component, Inject } from '@angular/core';
import { IClientVersionExt } from '../../../interfaces/IClientVersionSummaryExt';
import { ClientService } from '../../../services/client/client.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-unregister-client',
  templateUrl: './unregister-client.component.html',
  styleUrls: ['./unregister-client.component.scss']
})
export class UnregisterClientComponent {
  clientVersion: IClientVersionExt;
  translations = { client: '' };

  constructor(
    private clientService: ClientService,
    public dialogRef: MatDialogRef<UnregisterClientComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: { clientVersion: IClientVersionExt; clientName: string }
  ) {
    this.clientVersion = data.clientVersion;
    this.translations.client = data.clientName;
  }

  onUnregister(): void {
    this.clientService
      .deleteClient(this.clientVersion)
      .subscribe(() => this.dialogRef.close(true));
  }
}
