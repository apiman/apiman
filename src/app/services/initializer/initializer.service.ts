import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root',
})
export class InitializerService {
  constructor(private translator: TranslateService) {}

  /**
   * The functions calls the ngx-translation service to load a language. If the language file can not be found the fallback language
   * is English (loads the file en.json).
   * @param language is the language identifier, e.g. 'en' for English or 'de' for German
   */
  initLanguage(language: string): Promise<void> {
    return new Promise((resolve, reject) => {
      this.translator
        .use(language)
        .toPromise()
        .then(() => {
          resolve();
        })
        .catch(() => {
          console.warn(
            'Could not load language: ' +
              language +
              "\nDefault language 'en' will be used"
          );
          this.translator
            .use('en')
            .toPromise()
            .then(() => {
              resolve();
            });
        });
    });
  }
}
