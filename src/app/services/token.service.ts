import { Injectable } from '@angular/core';
import {Subject} from 'rxjs';
import {Token} from '@angular/compiler';

export interface Tokens {
  token: string;
  refreshToken: string;
}

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  private tokenSubject = new Subject<Tokens>();

  constructor() { }

  /**
   * Set token to subject
   * @param token the token
   */
  public setTokens(token: string, refreshToken: string) {
    console.log('got fresh tokens');
    this.tokenSubject.next({
      token,
      refreshToken
    });
  }

  /**
   * Get token from subject
   */
  public getTokens() {
    return this.tokenSubject.asObservable();
  }
}
