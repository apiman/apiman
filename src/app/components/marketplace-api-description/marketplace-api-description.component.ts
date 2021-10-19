import { Component, Input, OnInit } from '@angular/core';
import {IApiVersionExt} from '../../interfaces/IApiVersionExt';
import {TranslateService} from '@ngx-translate/core';
import {ConfigService} from "../../services/config/config.service";

@Component({
  selector: 'app-marketplace-api-description',
  templateUrl: './marketplace-api-description.component.html',
  styleUrls: ['./marketplace-api-description.component.scss'],
})
export class MarketplaceApiDescriptionComponent implements OnInit {
  @Input() api!: IApiVersionExt;
  @Input() isLatest!: boolean;
  @Input() apiImgUrl!: string;

  // TODO get features from backend as soon as the endpoint is created
  features = [];
  public markDownText = this.translator.instant('API_DETAILS.NO_EXT_DESCRIPTION');

  constructor(private translator: TranslateService,
              public configService: ConfigService) {
  }

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
