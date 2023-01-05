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

describe('Testing the api-details page', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/my-clients-register-btn.json');
  });

  beforeEach(() => {
    cy.visit('/home');
    cy.tryLogout();
  });

  it('Check register btn as viewer', () => {
    cy.login(
      Cypress.env('devportalUser') as string,
      Cypress.env('devportalPassword') as string
    );

    cy.visit('/applications');

    cy.get('#CypressTestOrg1-CypressTestClient1-1\\.0').within(() => {
      cy.get('#client-status-label').should('include.text', 'Ready');
      cy.get('#client-register-btn').should('not.exist');
    });
  });

  it('Check register btn as editor', () => {
    cy.login(
      Cypress.env('nonAdminUser') as string,
      Cypress.env('nonAdminPassword') as string
    );

    cy.visit('/applications');

    cy.get('#CypressTestOrg1-CypressTestClient1-1\\.0').within(() => {
      cy.get('#client-register-btn').should('exist').and('be.visible');
      cy.get('#client-status-label').should('include.text', 'Ready');
      cy.get('#client-register-btn').click();
      cy.get('#client-status-label').should('include.text', 'Registered');
    });
  });
});
