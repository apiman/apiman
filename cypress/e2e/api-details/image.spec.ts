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
 * 2 public APIs - one API with Image, one API without Image
 *
 * Test explanation
 * We want to check if the API Image is displayed if it is available and
 * if the default API icon is displayed if no Image is available
 * */

describe('Testing API image on api-details page', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/api-details-image.json');
  });

  it('Check default API Icon', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi1');

    // Check default API icon
    cy.get('app-img-or-icon-selector')
      .find('mat-icon')
      .should('exist')
      .and('include.text', 'developer_board');

    // Check missing API image
    cy.get('app-img-or-icon-selector').find('img').should('not.exist');
  });

  it('Check API Image', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi2');

    // Check API image
    cy.get('app-img-or-icon-selector')
      .find('img')
      .should('exist')
      .and('have.attr', 'alt', 'api logo');

    // Check missing default API icon
    cy.get('app-img-or-icon-selector').find('mat-icon').should('not.exist');
  });
});
