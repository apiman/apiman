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
 * 2 Orgs
 * 3 private APIs
 * 3 Clients
 *
 * Test explanation
 * We want to check if the search input filters the 'my clients' list and toc correctly
 * */

describe('Testing search on my clients', () => {
  before(() => {
    cy.cleanUp();
    // cy.initApimanData('test-data/my-clients-search.json');
  });

  beforeEach(() => {
    cy.visit('/home');
    cy.tryLogout();
  });

  it('Check search input behaviour', () => {
    cy.login(
      Cypress.env('devportalUser') as string,
      Cypress.env('devportalPassword') as string
    );
    cy.visit('/applications');

    // Check initial state
    cy.get('#search-input').should('have.value', '');
    cy.get('#search-input-clear-btn').should('not.exist');

    // Check if clear btn appears if input is filled
    cy.get('#search-input').type('something');
    cy.get('#search-input-clear-btn')
      .should('exist')
      .and('include.text', 'clear');

    // Check clear btn working correctly
    cy.get('#search-input-clear-btn').click();
    cy.get('#search-input').should('have.value', '');
    cy.get('#search-input-clear-btn').should('not.exist');
  });

  it('Check search functionality', () => {
    // TODO finish test
  });
});
