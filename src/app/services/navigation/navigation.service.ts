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
      useRouter: true
    },
    {
      name: this.translator.instant('APPS.TITLE'),
      link: 'applications',
      openInNewTab: false,
      useRouter: true
    }
    // To-Do
    // Show account link again once the account page is implemented correctly
    // {
    //   name: this.translator.instant('ACCOUNT.TITLE'),
    //   link: 'account',
    //   openInNewTab: false,
    //   useRouter: true
    // }
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
    if (this.configService.getShowHomeLink()) {
      this.links.unshift({
        name: this.translator.instant('COMMON.HOME'),
        link: 'home',
        openInNewTab: false,
        useRouter: true
      });
    }

    const navigationConfig = this.configService.getNavigation();
    navigationConfig.links = this.links.concat(navigationConfig.links);

    return navigationConfig;
  }
}
