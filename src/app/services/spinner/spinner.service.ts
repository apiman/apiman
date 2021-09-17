import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SpinnerService {
  private waiting = false;

  constructor() {}

  /**
   * Start waiting and show spinner
   */
  public startWaiting(): void {
    this.waiting = true;
  }

  /**
   * Stop waiting and hide spinner
   */
  public stopWaiting(): void {
    this.waiting = false;
  }

  /**
   * Checks waiting state of spinner
   */
  public isWaiting(): boolean {
    return this.waiting;
  }
}
