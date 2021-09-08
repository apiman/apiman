import { Injectable } from '@angular/core';
import config from './../../../../config.json';
import {IConfig, IFooter} from '../../interfaces/IConfig';
import {IHero} from '../../interfaces/IHero';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  config!: IConfig;

  constructor() { }

  readAndEvaluateConfig(): boolean{
    this.config = config;

    try{
      JSON.stringify(this.config);
    }catch (e) {
      throw Error('Invalid Config File');
    }

    return true;
  }

  getFooter(): IFooter {
    return {...this.config.footer};
  }

  getHero(): IHero {
    return {...this.config.hero};
  }
}
