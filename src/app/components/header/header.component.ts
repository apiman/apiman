import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.sass']
})
export class HeaderComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  // To-Do
  // replace hard-coded values with configurable variable
  header = {
    title: "KoolBank API Developer Portal",
    subtitle: "Welcome Message goes here."
  }
}
