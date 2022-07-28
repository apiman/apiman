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
    cy.initApimanData('test-data/apiman_data.json');
    cy.navigateToApiDetails('TestApi1');
  });

  it('Check Hero Image and Title home', () => {
    cy.get('#hero-title').should('include.text', 'TestApi1');
    cy.get('#hero-subtitle').should('have.text', '');
    cy.get('#hero-login-btn').should('exist').and('be.visible');
  });

  it('Check Navigation Bar', () => {
    cy.get('.navigation-links')
      .should('have.length', 3)
      .and('not.have.class', 'primary');
  });

  it('Check api version accordion', () => {
    cy.get('mat-expansion-panel').should('have.length', 2);
    cy.get('mat-expansion-panel')
      .first()
      .should('include.text', '[latest]')
      .and('have.class', 'mat-expanded');
    cy.get('mat-expansion-panel')
      .not('.mat-expanded')
      .should('not.include.text', '[latest]');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('markdown')
      .should('include.text', 'Markdown Description of Version 2.0');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('#plan-card-Bronze')
      .find('#sign-up-btn')
      .should('not.exist');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('#plan-card-Silver')
      .find('#sign-up-btn')
      .should('not.exist');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('#plan-card-Unlimited')
      .find('#sign-up-btn')
      .should('not.exist');

    cy.get('mat-expansion-panel').not('.mat-expanded').click();
    cy.get('mat-expansion-panel')
      .first()
      .should('include.text', '[latest]')
      .and('not.have.class', 'mat-expanded');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('markdown')
      .should(
        'include.text',
        'No further description has been stored for this API'
      );
    cy.get('mat-expansion-panel.mat-expanded').should(
      'not.include.text',
      '[latest]'
    );
    cy.get('mat-expansion-panel.mat-expanded')
      .find('#plan-card-Bronze')
      .find('#sign-up-btn')
      .should('exist');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('#plan-card-Silver')
      .find('#sign-up-btn')
      .should('exist');
    cy.get('mat-expansion-panel.mat-expanded')
      .find('#plan-card-Unlimited')
      .find('#sign-up-btn')
      .should('exist');
  });
});
