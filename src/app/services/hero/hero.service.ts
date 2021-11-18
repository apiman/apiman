import { EventEmitter, Injectable } from '@angular/core';
import { ConfigService } from '../config/config.service';
import { IHero } from '../../interfaces/IConfig';
import { Title } from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
export class HeroService {
  hero: IHero;
  heroChanged: EventEmitter<IHero> = new EventEmitter<IHero>();

  constructor(
    private configService: ConfigService,
    private titleService: Title
  ) {
    this.hero = configService.getHero();
  }

  setUpHero(hero: IHero): void {
    this.hero.large = hero.large ?? false;
    this.hero.title = hero.title ?? '';
    this.hero.subtitle = hero.subtitle ?? '';

    this.titleService.setTitle(this.hero.title);
    this.heroChanged.emit(this.hero);
  }
}
