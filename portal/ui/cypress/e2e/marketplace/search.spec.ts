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

describe('Testing the marketplace search', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/marketplace_search.json');
  });

  it('Check search input', () => {
    cy.visit('/marketplace');
    cy.intercept('GET', '**/devportal/organizations/**').as('getRequests');
    cy.intercept('POST', '**/devportal/search/apis/**').as('postRequests');

    cy.get('mat-card.api-card').should('have.length', 3);
    // Clear btn shouldn't be visible if input is empty
    cy.get('#search-input-clear-btn').should('not.exist');

    cy.typeSearch('CypressTestApi');
    cy.get('mat-card.api-card').should('have.length', 3);

    cy.typeSearch('CypressTestApi1');
    cy.get('mat-card.api-card').should('have.length', 1);

    cy.typeSearch('CypressTestApi2');
    cy.get('mat-card.api-card').should('have.length', 1);

    cy.typeSearch('CypressTestApi 3');
    cy.get('mat-card.api-card').should('have.length', 0);
    cy.get('#no-data-text').should('be.visible');

    // Check if clear btn appears if input is not empty
    cy.get('#search-input-clear-btn')
      .should('exist')
      .and('include.text', 'clear');

    // Check clear btn working correctly
    cy.get('#search-input-clear-btn').click();
    cy.get('mat-card.api-card').should('have.length', 3);
    cy.get('#search-input').should('have.value', '');
    cy.get('#search-input-clear-btn').should('not.exist');
  });
});
