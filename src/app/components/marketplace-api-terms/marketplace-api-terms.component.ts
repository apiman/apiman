import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ConfigService } from '../../services/config/config.service';

@Component({
  selector: 'app-marketplace-api-terms',
  templateUrl: './marketplace-api-terms.component.html',
  styleUrls: ['./marketplace-api-terms.component.scss'],
})
export class MarketplaceApiTermsComponent implements OnInit {
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

  ngOnInit(): void {}

  onClick() {
    this.agreedTermsAndPrivacy.emit(
      this.acceptedPrivacyTerms && this.acceptedTerms
    );
  }
}
