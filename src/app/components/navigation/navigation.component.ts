import { Component, OnInit } from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {ConfigService} from '../../services/config/config.service';
import {INavigation} from '../../interfaces/IConfig';
import {NavigationService} from '../../services/navigation/navigation.service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.sass']
})
export class NavigationComponent {
  navigation: INavigation;

  constructor(private navigationService: NavigationService) {
    this.navigation = navigationService.navigation;
  }
}
