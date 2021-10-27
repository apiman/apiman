import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IContractExt} from '../../interfaces/IContractExt';
import {ISection} from "../../interfaces/ISection";

@Component({
  selector: 'app-my-apps-summary',
  templateUrl: './my-apps-summary.component.html',
  styleUrls: ['./my-apps-summary.component.scss'],
})
export class MyAppsSummaryComponent implements OnInit {
  @Input() contract?: IContractExt;
  hasOAuth = false;

  @Output() sectionChangedEmitter = new EventEmitter();

  constructor() {}

  ngOnInit(): void {}

  sectionChanged($event: {contract: IContractExt, section: ISection}) {
    this.sectionChangedEmitter.emit($event);
  }
}
