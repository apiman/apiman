import { Component, EventEmitter, Input, Output } from '@angular/core';
import { IContractExt } from '../../interfaces/IContractExt';
import { ISection } from '../../interfaces/ISection';

@Component({
  selector: 'app-my-apps-summary',
  templateUrl: './my-apps-summary.component.html',
  styleUrls: ['./my-apps-summary.component.scss']
})
export class MyAppsSummaryComponent {
  @Input() contract?: IContractExt;
  hasOAuth = false;

  @Output() sectionChangedEmitter = new EventEmitter();

  constructor() {}

  sectionChanged($event: { contract: IContractExt; section: ISection }): void {
    this.sectionChangedEmitter.emit($event);
  }
}
