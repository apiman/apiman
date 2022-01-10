/// <reference types="cypress" />

describe('Testing the api-details page', () => {
  before(() => {
    cy.navigateToApiDetails('TestApi1');
  });

  it('Check Hero Image and Title home', () => {
    cy.get('#hero-title').should('include.text', 'TestApi1');
    cy.get('#hero-subtitle').should('have.text', '');
    cy.get('#login-btn').should('exist').and('be.visible');
  });

  it('Check Navigation Bar', () => {
    cy.get('.navigation-links')
      .should('have.length', 4)
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
