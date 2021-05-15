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


Cypress.Commands.add('typeLogin', (user, pw) => {
    cy.get('#username').type(user);
    cy.get('#password').type(pw);
    cy.get('#kc-login').click();
})

Cypress.Commands.add('visitTestOrganization', () => {
    cy.get('[title="Organizations"]').click();
    cy.contains('My Organizations').click();
    cy.contains('TestOrganization').click();
})

Cypress.Commands.add('visitTestPlan', () => {
    cy.visitTestOrganization();
    cy.get('#tab-plans').click();
    cy.contains('TestPlan').click();
})

Cypress.Commands.add('visitTestApi', () => {
    cy.visitTestOrganization();
    cy.get('#tab-apis').click();
    cy.contains('TestApi').click();
})

Cypress.Commands.add('visitTestClient', () => {
    cy.visitTestOrganization();
    cy.get('#tab-clients').click();
    cy.contains('TestClient').click();
})
