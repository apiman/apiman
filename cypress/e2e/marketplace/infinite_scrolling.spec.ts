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

describe('Testing the marketplace', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/marketplace_infinite_scrolling.json');
    cy.visit('/home');
    cy.tryLogout();
  });

  it('Check infinite Scrolling', () => {
    cy.visit('/marketplace');
    let fetchApiCount = 0;
    cy.intercept('POST', '**/search/apis', () => {
      fetchApiCount += 1;
    }).as('fetchApis');
    cy.login(
      Cypress.env('adminUser') as string,
      Cypress.env('adminPassword') as string
    );

    cy.url().should('include', '/marketplace');
    cy.should(() => {
      expect(fetchApiCount, 'fetchApis call count').to.equal(1);
    });

    cy.scrollTo('bottom');
    cy.get('#grid-spinner').should('be.visible');
    cy.get('#grid-spinner').should('not.be.visible');
    cy.should(() => {
      expect(fetchApiCount, 'fetchApis call count').to.equal(2);
    });

    cy.scrollTo('bottom');
    cy.get('#grid-spinner').should('be.visible');
    cy.get('#grid-spinner').should('not.be.visible');
    cy.should(() => {
      expect(fetchApiCount, 'fetchApis call count').to.equal(3);
    });

    cy.scrollTo('bottom');
    cy.get('#grid-spinner').should('be.visible');
    cy.get('#grid-spinner').should('not.be.visible');
    cy.should(() => {
      expect(fetchApiCount, 'fetchApis call count').to.equal(4);
    });

    cy.scrollTo('bottom');
    cy.get('#grid-spinner').should('be.visible');
    cy.get('#grid-spinner').should('not.be.visible');
    cy.should(() => {
      expect(fetchApiCount, 'fetchApis call count').to.equal(5);
    });

    cy.scrollTo('bottom');
    cy.get('#grid-spinner').should('not.be.visible');
    cy.should(() => {
      expect(fetchApiCount, 'fetchApis call count').to.equal(5);
    });
  });
});
