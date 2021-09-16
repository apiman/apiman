import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  constructor() {}

  setTheme(theme: string) {
    document.body.classList.add(theme);
  }
}
