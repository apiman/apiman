import { Component, OnInit } from '@angular/core';
import {ConfigService} from '../../services/config/config.service';
import {IFooter, ILink} from '../../interfaces/IConfig';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.sass']
})
export class FooterComponent implements OnInit {
  footer: IFooter

  constructor(configService: ConfigService) {
    this.footer = configService.getFooter();
  }

  ngOnInit(): void { }
}
