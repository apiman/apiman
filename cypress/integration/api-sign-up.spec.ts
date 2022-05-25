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

describe('Testing the api-signup', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/apiman_data.json');
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
    cy.get('#hero-logout-btn').click({ force: true });
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
    cy.get('#hero-logout-btn').click({ force: true });
  });
});
