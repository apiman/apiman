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

import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { IApiVersionEndpointSummary } from '../../interfaces/ICommunication';
import { IApiVersionExt } from '../../interfaces/IApiVersionExt';
import { ApiService } from '../../services/api/api.service';

@Component({
  selector: 'app-api-public-endpoint',
  templateUrl: './api-public-endpoint.component.html',
  styleUrls: ['./api-public-endpoint.component.scss']
})
export class ApiPublicEndpointComponent implements OnInit {
  @Input() publicApiVersion: IApiVersionExt | undefined;

  publicEndpoint$: Observable<IApiVersionEndpointSummary> | undefined;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    if (this.publicApiVersion) {
      this.publicEndpoint$ = this.apiService.getManagedApiEndpoint(
        this.publicApiVersion.api.organization.id,
        this.publicApiVersion.api.id,
        this.publicApiVersion.version
      );
    }
  }
}
