import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  initTheme(theme: string): void {
    document.body.classList.add('theme-' + theme);
  }
}
