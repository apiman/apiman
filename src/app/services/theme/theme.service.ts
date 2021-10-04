import { Injectable } from '@angular/core';
import { ConfigService } from '../config/config.service';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  initTheme(theme: string) {
    document.body.classList.add('theme-' + theme);
  }
}
