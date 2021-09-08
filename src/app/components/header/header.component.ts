import {AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Renderer2, ViewChild} from '@angular/core';
import config from './../../../../config.json';
import {HeroService} from './../../services/hero/hero.service';
import {Data} from '@angular/router';
import {IHero} from '../../interfaces/IHero';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.sass']
})
export class HeaderComponent implements AfterViewInit {
  @ViewChild('heroImage') heroImageDiv!: ElementRef;

  constructor(private renderer: Renderer2,
              public heroService: HeroService) { }

  ngAfterViewInit(): void {
    this.renderer.setStyle(this.heroImageDiv.nativeElement, 'background-image', 'url(\"' + this.heroService.hero.heroImgUrl + '\")');
  }
}
