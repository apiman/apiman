import { EventEmitter, Injectable } from '@angular/core';
import { ILink, INavigation } from '../../interfaces/IConfig';
import { ConfigService } from '../config/config.service';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root',
})
export class NavigationService {
  navigation: INavigation;

  links: ILink[] = [
    {
      name: this.translator.instant('MPLACE.TITLE'),
      link: 'marketplace',
      openInNewTab: false,
    },
    {
      name: this.translator.instant('APPS.TITLE'),
      link: 'applications',
      openInNewTab: false,
    },
    // To-Do
    // Show account link again once the account page is implemented correctly
    // {
    //   name: this.translator.instant('ACCOUNT.TITLE'),
    //   link: 'account',
    //   openInNewTab: false,
    // },

  ];

  navigationChanged: EventEmitter<INavigation> =
    new EventEmitter<INavigation>();

  constructor(
    private configService: ConfigService,
    private translator: TranslateService
  ) {
    this.navigation = this.initLinks();
  }

  private initLinks(): INavigation {
    const navigationConfig = this.configService.getNavigation();
    navigationConfig.links = this.links.concat(navigationConfig.links);

    return navigationConfig;
  }
}
