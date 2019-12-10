import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SpinnerService {

  private waiting = false;

  constructor() { }

  /**
   * Start waiting and show spinner
   */
  public startWaiting() {
    this.waiting = true;
  }

  /**
   * Stop waiting and hide spinner
   */
  public stopWaiting() {
    this.waiting = false;
  }

  /**
   * Checks waiting state of spinner
   */
  public isWaiting() {
    return this.waiting;
  }

}
