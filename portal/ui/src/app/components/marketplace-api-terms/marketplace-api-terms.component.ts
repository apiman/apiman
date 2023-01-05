/*
 * Copyright 2021 Scheer PAS Schweiz AG
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

import { Component, EventEmitter, Output } from '@angular/core';
import { ConfigService } from '../../services/config/config.service';

@Component({
  selector: 'app-marketplace-api-terms',
  templateUrl: './marketplace-api-terms.component.html',
  styleUrls: ['./marketplace-api-terms.component.scss']
})
export class MarketplaceApiTermsComponent {
  @Output() agreedTermsAndPrivacy = new EventEmitter<boolean>();
  acceptedTerms = false;
  acceptedPrivacyTerms = false;
  termsLink: string;
  privacyLink: string;

  // TODO: fetch terms from e.g. text-file
  terms = undefined;

  constructor(private configService: ConfigService) {
    this.termsLink = this.configService.getTerms().termsLink;
    this.privacyLink = this.configService.getTerms().privacyLink;
  }

  onClick(): void {
    this.agreedTermsAndPrivacy.emit(
      this.acceptedPrivacyTerms && this.acceptedTerms
    );
  }
}
