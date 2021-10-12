import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-no-data',
  templateUrl: './no-data.component.html',
  styleUrls: ['./no-data.component.scss']
})
export class NoDataComponent implements OnInit {
  @Input() text!: string;

  constructor() { }

  ngOnInit(): void {}
}
