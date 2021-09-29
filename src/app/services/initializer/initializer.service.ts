import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import {ConfigService} from '../config/config.service';

@Injectable({
  providedIn: 'root',
})
export class InitializerService {
  constructor(private translator: TranslateService, private configService: ConfigService) {}

  /**
   * The functions calls the ngx-translation service to load a language. If the language file can not be found the fallback language
   * is English (loads the file en.json).
   */
  initLanguage(): Promise<void> {
    const language =
      this.configService.getAvailableLanguages().includes(this.configService.getLanguage()) ?
      this.configService.getLanguage() : 'en';

    return new Promise((resolve) => {
      this.translator.use(language).toPromise().then(() => {
        resolve();
      })
    });
  }
}
