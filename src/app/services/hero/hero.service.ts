import {EventEmitter, Injectable} from '@angular/core';
import {Data} from '@angular/router';
import {IHero} from '../../interfaces/IHero';
import {ConfigService} from '../config/config.service';

@Injectable({
  providedIn: 'root'
})
export class HeroService {
  hero!: IHero;
  heroChanged: EventEmitter<IHero> = new EventEmitter<IHero>();

  constructor(private configService: ConfigService) {
    this.hero = configService.getHero();
  }

  setUpHero(hero: IHero | any) {
    this.hero.large = hero.large ? true : false;
    this.hero.title = this.checkTitle(hero.title) ? hero.title : '';
    this.hero.subtitle = this.checkTitle(hero.subtitle) ? hero.subtitle : '';

    this.heroChanged.emit(hero);
  }

  private checkTitle(title: string){
    return title !== '';
  }
}
