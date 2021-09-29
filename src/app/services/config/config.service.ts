import { Injectable } from '@angular/core';
import {
  IAuthProvider,
  IConfig,
  IFooter,
  IHero,
  INavigation,
  ITerms,
} from '../../interfaces/IConfig';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  config: any | IConfig;

  constructor(private http: HttpClient) {}

  async readAndEvaluateConfig(): Promise<any> {
    this.config = await this.http.get('assets/config.json').toPromise();

    try {
      JSON.stringify(this.config);
    } catch (e) {
      throw Error('Invalid Config File');
    }

    return this.config;
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

  getAvailableLanguages(): string[]{
    return this.config.supportedLanguages;
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

  getTerms(): ITerms {
    return { ...this.config.terms };
  }

  setConfig(config: any) {
    this.config = config;
  }
}
