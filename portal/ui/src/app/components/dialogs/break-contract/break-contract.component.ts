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
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { IContractExt } from '../../../interfaces/IContractExt';
import { ContractService } from '../../../services/contract/contract.service';

@Component({
  selector: 'app-break-contract',
  templateUrl: './break-contract.component.html',
  styleUrls: ['./break-contract.component.scss']
})
export class BreakContractComponent {
  contract: IContractExt;
  translations = {
    client: '',
    api: ''
  };

  constructor(
    private contractService: ContractService,
    public dialogRef: MatDialogRef<BreakContractComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { contract: IContractExt }
  ) {
    this.contract = data.contract;
    this.translations = {
      client: this.contract.client.client.name,
      api: this.contract.api.api.name
    };
  }

  breakContract() {
    this.contractService
      .breakContract(this.contract)
      .subscribe(() => this.dialogRef.close(true));
  }
}
