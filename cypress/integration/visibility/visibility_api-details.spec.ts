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

describe('Testing the visibility on api-details', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/visibility.json');
  });

  beforeEach(() => {
    cy.visit('/home');
    cy.tryLogout();
  });

  it('Check API details anonymous', () => {
    // Public API
    // Version 1.0 - Expose in Portal
    // Version 2.0 - Full Platform Member
    cy.visit('/api-details/CypressTestOrg/CypressTestApi1');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('.plan-card')
      .should('have.length', 1);
    cy.get('mat-expansion-panal').should('have.length', 1);

    // Private API (Plans with all visibilities)
    cy.visit('/api-details/CypressTestOrg/CypressTestApi2');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('.plan-card')
      .should('have.length', 1);
  });

  it('Check API details devportal user', () => {
    cy.visit('/home');
    cy.login(
      Cypress.env('devportalUser') as string,
      Cypress.env('devportalPassword') as string
    );

    // Public API
    // Version 1.0 - Expose in Portal
    // Version 2.0 - Full Platform Member
    cy.visit('/api-details/CypressTestOrg/CypressTestApi1');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('.plan-card')
      .should('have.length', 1);
    cy.get('mat-expansion-panal').should('have.length', 1);

    // Private API (Plans with all visibilities)
    cy.visit('/api-details/CypressTestOrg/CypressTestApi2');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('.plan-card')
      .should('have.length', 1);
  });

  it('Check API details platform user', () => {
    cy.visit('/home');
    cy.logout();
    cy.login(
      Cypress.env('nonAdminUser') as string,
      Cypress.env('nonAdminPassword') as string
    );

    // Public API
    // Version 1.0 - Expose in Portal
    // Version 2.0 - Full Platform Member
    cy.visit('/api-details/CypressTestOrg/CypressTestApi1');
    cy.get('mat-expansion-panel').should('have.length', 2);

    // Private API (Plans with all visibilities)
    cy.visit('/api-details/CypressTestOrg/CypressTestApi2');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('.plan-card')
      .should('have.length', 2);
  });

  it('Check APIs org member', () => {
    cy.visit('/home');
    cy.logout();
    cy.login(
      Cypress.env('adminUser') as string,
      Cypress.env('adminPassword') as string
    );

    // Public API
    // Version 1.0 - Expose in Portal
    // Version 2.0 - Full Platform Member
    cy.visit('/api-details/CypressTestOrg/CypressTestApi1');
    cy.get('mat-expansion-panel').should('have.length', 2);

    // Private API (Plans with all visibilities)
    cy.visit('/api-details/CypressTestOrg/CypressTestApi2');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('.plan-card')
      .should('have.length', 3);
  });
});
