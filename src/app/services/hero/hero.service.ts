import { EventEmitter, Injectable } from '@angular/core';
import { ConfigService } from '../config/config.service';
import { IHero } from '../../interfaces/IConfig';

@Injectable({
  providedIn: 'root',
})
export class HeroService {
  hero!: IHero;
  heroChanged: EventEmitter<IHero> = new EventEmitter<IHero>();

  constructor(private configService: ConfigService) {
    this.hero = configService.getHero();
  }

  setUpHero(hero: IHero | any): void {
    this.hero.large = hero.large ? true : false;
    this.hero.title = this.checkTitle(hero.title) ? hero.title : '';
    this.hero.subtitle = this.checkTitle(hero.subtitle) ? hero.subtitle : '';

    this.heroChanged.emit(hero);
  }

  private checkTitle(title: string): boolean {
    return title !== '';
  }
}
