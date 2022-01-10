// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
/// <reference types="cypress" />

Cypress.Commands.overwrite('visit', (originalFn, url) => {
  originalFn(url);
  cy.waitUntilLoaded();
});

Cypress.Commands.add('waitUntilLoaded', (aliases) => {
  if (aliases) {
    aliases.forEach((alias) => {
      cy.wait(alias);
    });
  }
  cy.get('circle').should('not.be.visible');
});

Cypress.Commands.add('checkHeroTitle', (title, subtitle) => {
  cy.get('#hero-title').should('include.text', title ? title : '');
  cy.get('#hero-subtitle').should('include.text', subtitle ? subtitle : '');
});

Cypress.Commands.add('typeLogin', (username, password) => {
  cy.get('#username').clear().type(username);
  cy.get('#password').clear().type(password);
  cy.get('#kc-login').click();
});

Cypress.Commands.add('typeSearch', (searchTerm) => {
  cy.get('#search-input').clear().type(searchTerm);
  cy.waitUntilLoaded(['@postRequests', '@getRequests']);
});

Cypress.Commands.add('navigateToApiDetails', (apiId) => {
  cy.visit('http://localhost:4200/marketplace');
  cy.get(`#api-card_${apiId} #details-btn`).should('exist').click();
  cy.waitUntilLoaded();
});
