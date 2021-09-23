import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class SnackbarService {
  readonly snackbarDuration = 6000;

  constructor(private snackBar: MatSnackBar) {}

  public showPrimarySnackBar(msg: string, action?: string) {
    this.showCustomSnackBar(msg, action);
  }

  public showErrorSnackBar(msg: string, error?: any, action?: string) {
    console.error(error);
    this.showCustomSnackBar(msg, action, {
      duration: this.snackbarDuration,
      panelClass: ['warn-bg'],
    });
  }

  public showCustomSnackBar(
    msg: string,
    action = '',
    config: MatSnackBarConfig = {
      duration: this.snackbarDuration,
      panelClass: ['primary-bg'],
    }
  ) {
    this.snackBar.open(msg, action, config);
  }
}
