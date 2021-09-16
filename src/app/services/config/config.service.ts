import { Injectable } from '@angular/core';
import config from './../../../../config.json';
import {
  IAuthProvider,
  IConfig,
  IFooter,
  IHero,
  INavigation,
} from '../../interfaces/IConfig';

@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  config!: IConfig;

  constructor() {}

  readAndEvaluateConfig(): boolean {
    this.config = config;

    try {
      JSON.stringify(this.config);
    } catch (e) {
      throw Error('Invalid Config File');
    }
    return true;
  }

  getFooter(): IFooter {
    return { ...this.config.footer };
  }

  getHero(): IHero {
    return { ...this.config.hero };
  }

  getNavigation(): INavigation {
    return { ...this.config.navigation };
  }

  getLanguage(): string {
    return this.config.language;
  }

  getEndpoint(): string {
    return this.config.endpoint;
  }

  getAuth(): IAuthProvider {
    return { ...this.config.auth };
  }
}
