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

import { defineConfig } from 'cypress';

// eslint-disable-next-line @typescript-eslint/no-unsafe-call
export default defineConfig({
  projectId: 'API Management',
  chromeWebSecurity: false,
  watchForFileChanges: false,
  viewportHeight: 1080,
  viewportWidth: 1920,
  defaultCommandTimeout: 15000,
  responseTimeout: 300000,
  video: false,
  includeShadowDom: true,
  env: {
    apiman_endpoint: 'https://<your-host-goes-here>/apiman',
    adminUser: 'cypress.admin',
    adminPassword: 'Demo123!',
    nonAdminUser: 'cypress.user',
    nonAdminPassword: 'Demo123!',
    devportalUser: 'cypress.user2',
    devportalPassword: 'Demo123!'
  },
  e2e: {
    // We've imported your old cypress plugins here.
    // You may want to clean this up later by importing these.
    // setupNodeEvents(on, config) {
    //   return require('./cypress/plugins/index.ts')(on, config);
    // },
    baseUrl: 'http://localhost:4200',
    specPattern: 'cypress/e2e/**/*.{js,jsx,ts,tsx}'
  }
});
