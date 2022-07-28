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

describe('Testing tooltips on api-details page', () => {
  before(() => {
    cy.visit('/home');
    cy.tryLogout();
    cy.cleanUp();
    cy.initApimanData('test-data/public-endpoint.json');
  });

  beforeEach(() => {
    cy.visit('/home');
    cy.tryLogout();
  });

  it('Check public endpoint on public API', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi1');

    cy.get('app-api-public-endpoint').should('exist').and('be.visible');
    cy.get('#api-endpoint-input').should(
      'include.value',
      '/gateway/CypressTestOrg1/CypressTestApi1/1.0'
    );
    cy.get('#api-endpoint-copy-btn').click();
    cy.get('#api-endpoint-input')
      .invoke('val')
      .then((apiEndpoint: string) => {
        cy.compareWithClipboard(apiEndpoint);
      });
  });

  it('Check public endpoint on private API', () => {
    cy.visit('/api-details/CypressTestOrg1/CypressTestApi2');

    cy.get('app-api-public-endpoint').should('not.exist');
  });
});
