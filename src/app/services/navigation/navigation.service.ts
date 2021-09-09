import {EventEmitter, Injectable} from '@angular/core';
import {ILink, INavigation} from '../../interfaces/IConfig';
import {ConfigService} from '../config/config.service';
import {TranslateService} from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  navigation: INavigation;

  links: ILink[] = [
    {
      name: this.translater.instant('MPLACE.TITLE'),
      link: 'marketplace',
      openInNewTab: false
    }, {
      name: this.translater.instant('APPS.TITLE'),
      link: 'applications',
      openInNewTab: false
    }, {
      name: this.translater.instant('ACCOUNT.TITLE'),
      link: 'account',
      openInNewTab: false
    }
  ];

  navigationChanged: EventEmitter<INavigation> = new EventEmitter<INavigation>();

  constructor(private configService: ConfigService,
              private translater: TranslateService) {
    this.navigation = this.initLinks();
  }

  private initLinks(): INavigation {
    const navigationConfig = this.configService.getNavigation()
    navigationConfig.links = this.links.concat(navigationConfig.links);

    return navigationConfig;
  }
}
