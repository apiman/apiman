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
 * 2 public APIs - one API with descriptions, one API without descriptions
 *
 * Test explanation
 * We want to check if the API short description and extended description is displayed correctly if they are available and
 * if the placeholders are displayed if the descriptions are missing
 * */

describe('Testing the API descriptions on api-details page', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/api-details-descriptions.json');
  });

  it('Check API without descriptions', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi1');

    // Check short description
    // Check placeholder for short description
    cy.get('#content-header')
      .find('app-no-data')
      .should('exist')
      .and('include.text', 'No API description available');

    // Check missing short description
    cy.get('#content-header')
      .find('#api-short-description')
      .should('not.exist');

    // Check extended description
    cy.get('markdown')
      .should('exist')
      .and(
        'include.text',
        'No further description has been stored for this API'
      );
  });

  it('Check API with descriptions', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi2');

    // Check short description
    // Check existing short description
    cy.get('#content-header')
      .find('#api-short-description')
      .should('exist')
      .and('include.text', 'This is the short description');

    // Check missing placeholder
    cy.get('#content-header').find('app-no-data').should('not.exist');

    // Check extended description
    cy.get('markdown')
      .should('exist')
      .and('include.text', 'This is the extended description');
  });
});
