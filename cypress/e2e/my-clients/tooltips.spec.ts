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

describe('Testing tooltips on my clients', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/my-clients-tooltips.json');
  });

  beforeEach(() => {
    cy.visit('/home');
    cy.tryLogout();
  });

  it('Check tooltips with one org', () => {
    cy.login(
      Cypress.env('devportalUser') as string,
      Cypress.env('devportalPassword') as string
    );
    cy.visit('/applications');

    cy.get('#CypressTestOrg1-CypressTestClient1-1\\.0').within(() => {
      cy.get('.content-text > h3').realHover();
    });
    cy.get('.mat-tooltip').should('not.exist');
    cy.get('#CypressTestApi1').within(() => {
      cy.get(
        '#CypressTestOrg1-CypressTestClient1-1\\.0-CypressTestApi1-1\\.0 > h3'
      ).realHover();
    });
    cy.get('.mat-tooltip')
      .should('exist')
      .and('be.visible')
      .and('include.text', 'CypressTestOrg1')
      .and('include.text', 'CypressTestApi1 (1.0)')
      .and('include.text', 'CypressTestPlan1 (1.0)');
  });

  it('Check tooltips with two org', () => {
    cy.login(
      Cypress.env('nonAdminUser') as string,
      Cypress.env('nonAdminPassword') as string
    );
    cy.visit('/applications');

    // first org/client/api
    cy.get('#CypressTestOrg1-CypressTestClient1-1\\.0').within(() => {
      cy.get('.content-text > h3').realHover();
    });
    cy.get('.mat-tooltip')
      .should('exist')
      .and('be.visible')
      .and('include.text', 'CypressTestOrg1')
      .and('include.text', 'CypressTestClient1 (1.0)');
    cy.get('#CypressTestApi1').within(() => {
      cy.get(
        '#CypressTestOrg1-CypressTestClient1-1\\.0-CypressTestApi1-1\\.0 > h3'
      ).realHover();
    });
    cy.get('.mat-tooltip')
      .should('exist')
      .and('be.visible')
      .and('include.text', 'CypressTestOrg1')
      .and('include.text', 'CypressTestApi1 (1.0)')
      .and('include.text', 'CypressTestPlan1 (1.0)');

    // second org/client/api
    cy.get('#CypressTestOrg2-CypressTestClient2-1\\.0').within(() => {
      cy.get('.content-text > h3').realHover();
    });
    cy.get('.mat-tooltip')
      .should('exist')
      .and('be.visible')
      .and('include.text', 'CypressTestOrg2')
      .and('include.text', 'CypressTestClient1 (1.0)');
    cy.get('#CypressTestApi2').within(() => {
      cy.get(
        '#CypressTestOrg2-CypressTestClient2-1\\.0-CypressTestApi2-1\\.0 > h3'
      ).realHover();
    });
    cy.get('.mat-tooltip')
      .should('exist')
      .and('be.visible')
      .and('include.text', 'CypressTestOrg2')
      .and('include.text', 'CypressTestApi2 (1.0)')
      .and('include.text', 'CypressTestPlan2 (1.0)');
  });
});
