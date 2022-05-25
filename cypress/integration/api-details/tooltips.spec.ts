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

/// <reference types="cypress" />

import { Discoverability } from '../../../src/app/interfaces/ICommunication';

describe('Testing tooltips on api-details page', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/api-details-tooltips.json');
  });

  beforeEach(() => {
    cy.visit('/home');
    cy.tryLogout();
  });

  it('Check tooltips anonymous', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi1');

    // Tooltip should not appear because we're not logged in
    cy.get('#plan-card-CypressTestPlan3-title').realHover();
    cy.get('.mat-tooltip').should('not.exist');
  });

  it('Check tooltips logged in', () => {
    cy.login(
      Cypress.env('nonAdminUser') as string,
      Cypress.env('devportalPassword') as string
    );
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi1');

    // Tooltips should appear because we're logged in
    cy.get('#plan-card-CypressTestPlan1-title').realHover();
    cy.get('.mat-tooltip')
      .should('exist')
      .and('include.text', `Visibility: ${Discoverability.ORG_MEMBERS}`);

    cy.get('#plan-card-CypressTestPlan2-title').realHover();
    cy.get('.mat-tooltip')
      .should('exist')
      .and(
        'include.text',
        `Visibility: ${Discoverability.FULL_PLATFORM_MEMBERS}`
      );

    cy.get('#plan-card-CypressTestPlan3-title').realHover();
    cy.get('.mat-tooltip')
      .should('exist')
      .and('include.text', `Visibility: ${Discoverability.PORTAL}`);
  });
});
