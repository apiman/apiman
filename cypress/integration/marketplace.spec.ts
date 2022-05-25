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
    cy.initApimanData('test-data/apiman_data.json');
  });

  beforeEach(() => {
    cy.retireApi('CypressTestOrg', 'TestApi1', '1.0');
    cy.deleteApi('CypressTestOrg', 'TestApi1');
    cy.retireApi('CypressTestOrg', 'TestApi2', '1.0');
    cy.deleteApi('CypressTestOrg', 'TestApi2');
    cy.deleteOrg('CypressTestOrg');
    cy.deleteOrg('cypress.user');
    cy.deleteOrg('cypress.admin');
  });

  it('Check Hero Image and Title home', () => {
    cy.visit('/marketplace');
    cy.get('#hero-title').should('include.text', 'Marketplace');
    cy.get('#hero-subtitle').should('include.text', 'Discover our APIs');
    cy.get('#hero-login-btn').should('exist').and('be.visible');
    cy.get('#navbar-link-router-marketplace').should('have.class', 'primary');
  });

  it('Check API List', () => {
    cy.visit('/marketplace');
    cy.intercept('GET', '**/devportal/organizations/**').as('getRequests');
    cy.intercept('POST', '**/devportal/apis/**').as('postRequests');

    cy.get('mat-card.api-card').should('have.length', 2);

    cy.typeSearch('TestApi');
    cy.get('mat-card.api-card').should('have.length', 2);

    cy.typeSearch('TestApi1');
    cy.get('mat-card.api-card').should('have.length', 1);

    cy.typeSearch('TestApi2');
    cy.get('mat-card.api-card').should('have.length', 1);

    cy.typeSearch('TestApi 3');
    cy.get('mat-card.api-card').should('have.length', 0);
    cy.get('#no-data-text')
      .should('be.visible')
      .and('include.text', 'No APIs were found');
  });

  it('Check infinite Scrolling', () => {
    let fetchApiCount = 0;
    cy.intercept('POST', '/search/apis', () => {
      fetchApiCount += 1;
    }).as('fetchApis');
    cy.visit('/marketplace');
    cy.should(() => {
      expect(fetchApiCount, 'fetchApis call count').to.equal(1);
    });
    cy.scrollTo(0, 500);
    cy.should(() => {
      expect(fetchApiCount, 'fetchApis call count').to.equal(2);
    });
  });
});
