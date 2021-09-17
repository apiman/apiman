import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  OnInit,
  Renderer2,
  ViewChild,
} from '@angular/core';
import config from './../../../../config.json';
import { HeroService } from '../../services/hero/hero.service';
import { Data } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements AfterViewInit {
  @ViewChild('heroImage') heroImageDiv!: ElementRef;
  @ViewChild('heroTitle') heroTitle!: ElementRef;
  @ViewChild('heroSubtitle') heroSubtitle!: ElementRef;
  @ViewChild('heroOverlay') heroOverlay!: ElementRef;

  loggedIn = false;

  constructor(
    private renderer: Renderer2,
    public heroService: HeroService,
    private keycloak: KeycloakService
  ) {}

  async ngAfterViewInit(): Promise<void> {
    this.renderer.setStyle(
      this.heroImageDiv.nativeElement,
      'background-image',
      'url("' + this.heroService.hero.heroImgUrl + '")'
    );
    this.renderer.setStyle(
      this.heroTitle.nativeElement,
      'color',
      this.heroService.hero.fontColor.title
    );
    this.renderer.setStyle(
      this.heroSubtitle.nativeElement,
      'color',
      this.heroService.hero.fontColor.subtitle
    );
    this.renderer.setStyle(
      this.heroOverlay.nativeElement,
      'background-color',
      this.heroService.hero.overlayColor
    );

    this.loggedIn = await this.keycloak.isLoggedIn();
  }
}
