import { Injectable } from '@angular/core';
import { ConfigService } from '../config/config.service';
import { IFooter, ILink } from '../../interfaces/IConfig';

@Injectable({
  providedIn: 'root'
})
export class FooterService {
  footer: IFooter;

  links: ILink[] = [];

  constructor(private configService: ConfigService) {
    this.footer = this.initLinks();
  }

  private initLinks(): IFooter {
    const footerConfig = this.configService.getFooter();
    footerConfig.links = this.links.concat(footerConfig.links);

    return footerConfig;
  }
}
