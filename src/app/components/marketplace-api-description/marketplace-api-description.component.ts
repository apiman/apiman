import { Component, Input, OnInit } from '@angular/core';
import {IApiVersionExt} from '../../interfaces/IApiVersionExt';

@Component({
  selector: 'app-marketplace-api-description',
  templateUrl: './marketplace-api-description.component.html',
  styleUrls: ['./marketplace-api-description.component.scss'],
})
export class MarketplaceApiDescriptionComponent implements OnInit {
  @Input() api!: IApiVersionExt;
  @Input() isLatest!: boolean;

  features: string[] = ['fast', 'free', 'fancy features'];
  shortDescription = '';

  constructor() {}

  ngOnInit(): void {
    this.getShortDescription();
  }

  private getShortDescription() {
    if (this.api.api.description) {
      this.shortDescription = this.api.api.description.substring(0, 240);
    }
  }
}
