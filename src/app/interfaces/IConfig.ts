/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

export interface IConfig {
  language: string;
  supportedLanguages: string[];
  theme: string;
  endpoint: string;
  managerUiEndpoint?: string; // TODO (fvolk)
  backendRoles: string[];
  hero: IHero;
  navigation: INavigation;
  footer: IFooter;
  auth: IAuthProvider;
  terms: ITerms;
}

export interface IHero {
  title: string;
  subtitle: string;
  heroImgUrl?: string;
  large?: boolean;
  fontColor?: {
    title: string;
    subtitle: string;
  };
  overlayColor?: string;
  buttonColor?: {
    login: string;
    logout: string;
  };
  notificationCount?: string;
}

export interface INavigation {
  links: ILink[];
  separator: string;
  showHomeLink: boolean;
}

export interface IFooter {
  links: ILink[];
  separator: string;
}

export interface ILink {
  name: string;
  link: string;
  openInNewTab: boolean;
  useRouter?: boolean;
}

export interface IAuthProvider {
  accountUrl?: string;
  url: string;
  realm: string;
  clientId: string;
}

export interface ITerms {
  enabled: boolean;
  termsLink: string;
  privacyLink: string;
}
