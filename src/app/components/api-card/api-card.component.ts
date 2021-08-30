import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-api-card',
  templateUrl: './api-card.component.html',
  styleUrls: ['./api-card.component.sass']
})
export class ApiCardComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }
  @Input() apiCard = {
    title: "",
    shortDescription: "",
    longDescription: "",
    icon: ""
  };
}
