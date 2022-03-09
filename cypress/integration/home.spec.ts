/// <reference types="cypress" />

import { IApimanData } from '../../src/app/interfaces/ICommunication';

describe('Testing the home page', () => {
  before(() => {
    cy.fixture('apiman_data.json').then((apimanData: IApimanData) => {
      cy.initApimanData(apimanData);
    });
  });

  after(() => {
    cy.retireApi('CypressTestOrg', 'TestApi1', '1.0');
    cy.deleteApi('CypressTestOrg', 'TestApi1');
    cy.retireApi('CypressTestOrg', 'TestApi2', '1.0');
    cy.deleteApi('CypressTestOrg', 'TestApi2');
    cy.deleteOrg('CypressTestOrg');
    cy.deleteOrg('cypress.user');
    cy.deleteOrg('cypress.admin');
  });

  it('Check Hero Image and Title home', () => {
    cy.visit('/home');
    cy.get('#hero-title').should('include.text', 'API Developer Portal');
    cy.get('#hero-subtitle').should('include.text', '');
    cy.get('#login-btn').should('exist').and('be.visible');
  });

  it('Check Navigation Bar', () => {
    cy.visit('/home');
    cy.get('.navigation-links').should('have.length', 4);
    cy.get('#navbar-link-router-home').should('have.class', 'primary');
  });

  it('Check featured APIs', () => {
    cy.visit('/home');
    cy.get('mat-card.api-card').should('have.length', 2);
    // Checking first api-card
    // api-card-header
    cy.get('#api-card_TestApi1 > .api-card-header #icon')
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
    cy.get('#api-card_TestApi2 > .api-card-header #icon').should('not.exist');
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
