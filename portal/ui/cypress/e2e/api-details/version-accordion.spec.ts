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

/*
 * Testdata explanation
 * 1 Org
 * 1 public API - with 5 versions, one for every API status plus an additional published one
 *
 * Test explanation
 * We want to check if API version accordion works properly
 * */

describe('Testing the API version accordion on api-details page', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/api-details-accordion.json');
  });

  it('Check that only published versions are listed', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi1');

    // Only Version 3.0 and 5.0 are published
    cy.get('mat-expansion-panel').should('have.length', 2);
    cy.get('#CypressTestApi1-3\\.0').should('exist');
    cy.get('#CypressTestApi1-5\\.0').should('exist');
  });

  it('Check general accordion behaviour', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi1');

    // Latest version should be automatically expanded and have a [latest] tag
    cy.get('mat-expansion-panel')
      .first()
      .should('include.text', '[latest]')
      .and('have.class', 'mat-expanded');

    // Other versions shouldn't be expanded nor should they have a [latest] tag
    cy.get('mat-expansion-panel')
      .not('.mat-expanded')
      .should('not.include.text', '[latest]');

    // Clicking on a closed accordion should open it and close the previously opened one
    cy.get('mat-expansion-panel')
      .not('.mat-expanded')
      .click()
      .should('have.class', 'mat-expanded');

    cy.get('mat-expansion-panel')
      .first()
      .should('not.have.class', 'mat-expanded');
  });
});
