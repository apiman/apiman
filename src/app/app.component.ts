import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import { Router } from '@angular/router';
import { HeroService } from './services/hero/hero.service';
import { IHero, INavigation } from './interfaces/IConfig';
import { NavigationService } from './services/navigation/navigation.service';
import { SpinnerService } from './services/spinner/spinner.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  @ViewChild('content') content!: ElementRef;
  title = 'devportal';

  constructor(
    private router: Router,
    private heroService: HeroService,
    private navigationService: NavigationService,
    public loadingSpinnerService: SpinnerService
  ) {}

  ngOnInit() {
    this.initHeroEmitter();
    this.initNavigationEmitter();
  }

  private initHeroEmitter() {
    this.heroService.heroChanged.subscribe((hero: IHero) => {
      if (hero.large) {
        this.content.nativeElement.classList.remove('free-height');
      } else {
        this.content.nativeElement.classList.add('free-height');
      }
    });
  }

  private initNavigationEmitter() {
    this.navigationService.navigationChanged.subscribe(
      (navigation: INavigation) => {}
    );
  }
}
