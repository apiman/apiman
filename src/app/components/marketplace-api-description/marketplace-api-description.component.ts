import {Component, Input, OnInit} from '@angular/core';
import {Api} from "../../interfaces/api";

@Component({
  selector: 'app-marketplace-api-description',
  templateUrl: './marketplace-api-description.component.html',
  styleUrls: ['./marketplace-api-description.component.sass']
})
export class MarketplaceApiDescriptionComponent implements OnInit {

  @Input() api: Api = {
    id: "",
    title: "",
    shortDescription: "",
    longDescription: "",
    featuredApi: false,
    icon: ""
  };

  features: string[] = ['fast', 'free', 'fancy features']

  constructor() { }

  ngOnInit(): void {
  }

}
