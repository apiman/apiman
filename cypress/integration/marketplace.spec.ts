/// <reference types="cypress" />

import { IApimanData } from '../../src/app/interfaces/ICommunication';

describe('Testing the marketplace', () => {
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
    cy.visit('/marketplace');
    cy.get('#hero-title').should('include.text', 'Marketplace');
    cy.get('#hero-subtitle').should('include.text', 'Discover our APIs');
    cy.get('#login-btn').should('exist').and('be.visible');
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
});
