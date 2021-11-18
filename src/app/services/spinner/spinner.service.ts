import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SpinnerService {
  public waiting = false;

  constructor() {}

  /**
   * Start waiting and show spinner
   */
  public startWaiting(): void {
    this.setWaiting(true);
  }

  /**
   * Stop waiting and hide spinner
   */
  public stopWaiting(): void {
    this.setWaiting(false);
  }

  /**
   * Because of change detection we need a timeout, otherwise console would show several errors
   * @param waiting
   */
  setWaiting(waiting: boolean): void {
    setTimeout(() => {
      this.waiting = waiting;
    });
  }
}
