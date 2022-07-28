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

describe('Testing the home page', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/apiman_data.json');
  });

  it('Check Hero Image and Title home', () => {
    cy.visit('/home');
    cy.get('#hero-title').should('include.text', 'API Developer Portal');
    cy.get('#hero-subtitle').should('include.text', '');
    cy.get('#hero-login-btn').should('exist').and('be.visible');
  });

  it('Check Navigation Bar', () => {
    cy.visit('/home');
    cy.get('.navigation-links').should('have.length', 3);
    cy.get('#navbar-link-router-home').should('have.class', 'primary');
  });

  it('Check featured APIs', () => {
    cy.visit('/home');
    cy.get('mat-card.api-card').should('have.length', 2);
    // Checking first api-card
    // api-card-header
    cy.get('#api-card_TestApi1 > .api-card-header .api-icon')
      .should('exist')
      .and('have.text', 'developer_board');
    cy.get('#api-card_TestApi1 > .api-card-header #img').should('not.exist');
    // api-card-content
    cy.get('#api-card_TestApi1 .content-text')
      .should('exist')
      .and('have.text', ' Description of TestApi1 ');
    cy.get('#api-card_TestApi1 #no-data-text').should('not.exist');
    //api-card-actions
    cy.get('#api-card_TestApi1 #swagger-btn').should('exist');

    // Checking second api-card
    // api-card-header
    cy.get('#api-card_TestApi2 > .api-card-header .api-icon').should(
      'not.exist'
    );
    cy.get('#api-card_TestApi2 > .api-card-header #img').should('exist');
    // api-card-content
    cy.get('#api-card_TestApi2 .content-text').should('not.exist');
    cy.get('#api-card_TestApi2 #no-data-text')
      .should('exist')
      .and('have.text', 'No API description available');
    //api-card-actions
    cy.get('#api-card_TestApi2 #swagger-btn').should('not.exist');
  });

  it('Check "Show All"-Button', () => {
    cy.get('#show-all-btn').should('be.visible').click();
    cy.url().should('include', '/marketplace');
  });
});
