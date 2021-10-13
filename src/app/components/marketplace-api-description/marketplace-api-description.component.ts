import { Component, Input, OnInit } from '@angular/core';
import {IApiVersionExt} from '../../interfaces/IApiVersionExt';
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-marketplace-api-description',
  templateUrl: './marketplace-api-description.component.html',
  styleUrls: ['./marketplace-api-description.component.scss'],
})
export class MarketplaceApiDescriptionComponent implements OnInit {
  @Input() api!: IApiVersionExt;
  @Input() isLatest!: boolean;

  features: string[] = ['fast', 'free', 'fancy features'];
  public markDownText = this.translator.instant('API_DETAILS.NO_EXT_DESCRIPTION');

  constructor(private translator: TranslateService) {}

  ngOnInit(): void {
    if (this.api.extendedDescription)
      this.markDownText = this.api.extendedDescription;
  }

  hasMdAndFeatures() {
    return this.markDownText && this.features.length > 0;
  }

  hasMdOnly() {
    return this.markDownText && this.features.length === 0
  }
}
