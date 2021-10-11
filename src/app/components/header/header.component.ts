import {
  AfterViewInit,
  Component,
  ElementRef, OnInit,
  Renderer2,
  ViewChild,
} from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { KeycloakService } from 'keycloak-angular';
import {KeycloakHelperService} from "../../services/keycloak-helper/keycloak-helper.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, AfterViewInit {
  @ViewChild('heroImage') heroImageDiv!: ElementRef;
  @ViewChild('heroTitle') heroTitle!: ElementRef;
  @ViewChild('heroSubtitle') heroSubtitle!: ElementRef;
  @ViewChild('heroOverlay') heroOverlay!: ElementRef;
  @ViewChild('loginBtn') heroLoginBtn!: ElementRef;
  @ViewChild('logoutBtn') heroLogoutBtn!: ElementRef;

  loggedIn = false;

  constructor(
    private renderer: Renderer2,
    public heroService: HeroService,
    private keycloak: KeycloakService,
    private keycloakHelper: KeycloakHelperService
  ) {}

  async ngOnInit() {
    this.loggedIn = await this.keycloak.isLoggedIn();
  }

  async ngAfterViewInit() {
    this.setStyle(this.heroImageDiv.nativeElement, 'background-image', 'url("' + this.heroService.hero.heroImgUrl + '")');
    this.setStyle(this.heroTitle.nativeElement,'color', this.heroService.hero.fontColor.title);
    this.setStyle(this.heroSubtitle.nativeElement,'color', this.heroService.hero.fontColor.subtitle);
    this.setStyle(this.heroOverlay.nativeElement,'background-color', this.heroService.hero.overlayColor);

    this.loggedIn = await this.keycloak.isLoggedIn();
    this.setStyleForLoginBtn();
  }

  setStyle(el: any, style: string, value: string){
    this.renderer.setStyle(el, style, value);
  }

  public login() {
    this.keycloakHelper.login();
  }

  public logout(): void {
    this.keycloakHelper.logout();
  }

  private setStyleForLoginBtn() {
    if (this.heroService.hero.buttonColor.login && !this.loggedIn) {
      this.setStyle(this.heroLoginBtn.nativeElement, 'color', this.heroService.hero.buttonColor.login);
      this.setStyle(this.heroLoginBtn.nativeElement, 'border-color', this.heroService.hero.buttonColor.login);
    }

    if (this.heroService.hero.buttonColor.logout && this.loggedIn) {
      this.setStyle(this.heroLogoutBtn.nativeElement, 'color', this.heroService.hero.buttonColor.logout);
      this.setStyle(this.heroLogoutBtn.nativeElement, 'border-color', this.heroService.hero.buttonColor.logout);
    }
  }
}
