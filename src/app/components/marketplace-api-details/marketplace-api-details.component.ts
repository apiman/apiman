import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {ApiService} from "../../services/api/api.service";
import {Api} from "../../interfaces/api";

@Component({
  selector: 'app-marketplace-api-details',
  templateUrl: './marketplace-api-details.component.html',
  styleUrls: ['./marketplace-api-details.component.sass']
})
export class MarketplaceApiDetailsComponent implements OnInit {

  @Input() id: string = "";

  api: Api = {
    id: "",
    title: "",
    shortDescription: "",
    longDescription: "",
    featuredApi: false,
    icon: ""
  };

  constructor(private route: ActivatedRoute,
              private apiService: ApiService) { }

  ngOnInit(): void {
    // this.getApi();
    // console.log(this.api);
  }

  getApi(): Api {
    const id = this.route.snapshot.paramMap.get('id');
    this.apiService.getApi(id)
      .subscribe(api => this.api = api!);
    return this.api
  };
}
