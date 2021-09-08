import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {ApiService} from "../../services/api/api.service";
import {ApiBean} from "../../services/backend/backend.service";

@Component({
  selector: 'app-marketplace-api-details',
  templateUrl: './marketplace-api-details.component.html',
  styleUrls: ['./marketplace-api-details.component.sass']
})
export class MarketplaceApiDetailsComponent implements OnInit {

  @Input() id: string = "";

  api: ApiBean = {};

  constructor(private route: ActivatedRoute,
              public apiService: ApiService) { }

  ngOnInit(): void {
    const orgId = this.route.snapshot.paramMap.get('orgId');
    const apiId = this.route.snapshot.paramMap.get('apiId');
    this.apiService.getApi(orgId!, apiId!);
    console.log(this.apiService.currentApi);
  }

}
