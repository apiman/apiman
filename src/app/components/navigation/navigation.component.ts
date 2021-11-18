import { Component } from '@angular/core';
import { INavigation } from '../../interfaces/IConfig';
import { NavigationService } from '../../services/navigation/navigation.service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent {
  navigation: INavigation;

  constructor(private navigationService: NavigationService) {
    this.navigation = navigationService.navigation;
  }
}
