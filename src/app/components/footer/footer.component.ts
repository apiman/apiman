import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.sass']
})
export class FooterComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }
  // To-Do replace hard-coded values with configurable variable
  links = [
    {
      name: 'Scheer PAS',
      link: 'https://www.scheer-pas.com/'
    },
    {
      name: 'Apiman',
      link: 'https://www.apiman.io/'
    },
    {
      name: 'Github',
      link: 'https://www.github.com/'
    },
    {
      name: 'Keybase',
      link: 'https://keybase.io/'
    }];
}
