/// <reference types="cypress" />

import { IApimanData } from '../../src/app/interfaces/ICommunication';

describe('Testing the api-signup', () => {
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

  it('Check api sign-up process as admin', () => {
    cy.intercept('POST', '**/clients').as('postClients');
    cy.intercept('GET', '**/clientorgs').as('getClientOrgs');
    cy.intercept('GET', '**/editable-clients').as('getEditableClients');
    cy.navigateToApiDetails('TestApi1');
    cy.checkHeroTitle('TestApi1', '');
    cy.get('mat-expansion-panel').not('.mat-expanded').click();
    cy.get('mat-expansion-panel.mat-expanded')
      .find('#plan-card-Bronze')
      .find('#sign-up-btn')
      .should('exist')
      .click();
    cy.typeLogin('cypress.admin', 'Demo123$');
    cy.get('#client-input').clear().type('TestClient1');
    cy.get('#register-client-btn').click({ force: true });
    cy.wait(['@postClients', '@getClientOrgs', '@getEditableClients']);
    cy.get('#next-step-btn').click();
    cy.get('#terms-and-conditions-checkbox')
      .find('input')
      .click({ force: true });
    cy.get('#privacy-policy-checkbox').find('input').click({ force: true });
    cy.get('#next-step-btn').click({ force: true });
    cy.get('#confirm-btn').click({ force: true });
    cy.get('.mat-snack-bar-container').should(
      'include.text',
      'A contract has been successfully created'
    );
    cy.get('#finish-btn').click({ force: true });
    cy.url().should('include', '/applications');
  });

  it('Delete admin client', () => {
    cy.request(
      'DELETE',
      'https://vagrantguest/pas/apiman/organizations/cypress.admin/clients/TestClient1/versions/1.0/contracts'
    );
    cy.request(
      'DELETE',
      'https://vagrantguest/pas/apiman/organizations/cypress.admin/clients/TestClient1'
    );
    cy.get('#logout-btn').click({ force: true });
  });

  it('Check api sign-up process as non admin', () => {
    cy.intercept('POST', '**/clients').as('postClients');
    cy.intercept('GET', '**/clientorgs').as('getClientOrgs');
    cy.intercept('GET', '**/editable-clients').as('getEditableClients');
    cy.navigateToApiDetails('TestApi1');
    cy.checkHeroTitle('TestApi1', '');
    cy.get('mat-expansion-panel').not('.mat-expanded').click();
    cy.get('mat-expansion-panel.mat-expanded')
      .find('#plan-card-Bronze')
      .find('#sign-up-btn')
      .should('exist')
      .click();
    cy.typeLogin('cypress.user', 'Demo123$');
    cy.get('#client-input').clear().type('TestClient2');
    cy.get('#register-client-btn').click({ force: true });
    cy.wait(['@postClients', '@getClientOrgs', '@getEditableClients']);
    cy.get('#next-step-btn').click();
    cy.get('#terms-and-conditions-checkbox')
      .find('input')
      .click({ force: true });
    cy.get('#privacy-policy-checkbox').find('input').click({ force: true });
    cy.get('#next-step-btn').click({ force: true });
    cy.get('#confirm-btn').click({ force: true });
    cy.get('.mat-snack-bar-container').should(
      'include.text',
      'A contract has been successfully created'
    );
    cy.url().should('include', '/approval');
  });

  it('Delete user client', () => {
    cy.request(
      'DELETE',
      'https://vagrantguest/pas/apiman/organizations/cypress.user/clients/TestClient2/versions/1.0/contracts'
    );
    cy.request(
      'DELETE',
      'https://vagrantguest/pas/apiman/organizations/cypress.user/clients/TestClient2'
    );
    cy.get('#logout-btn').click({ force: true });
  });
});
