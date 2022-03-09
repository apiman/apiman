/// <reference types="cypress" />

import { IApimanData } from '../../src/app/interfaces/ICommunication';
import { IUserCredentials } from '../support/interfaces/IUserCredentials';

describe('Testing the api-details page', () => {
  before(() => {
    cy.fixture('apiman_data.json').then((apimanData: IApimanData) => {
      cy.initApimanData(apimanData);
    });
    cy.visit('/applications');
    cy.fixture('userCredentials.json').then(
      (userCredentials: IUserCredentials) => {
        cy.typeLogin(
          userCredentials.adminUsername,
          userCredentials.adminPassword
        );
      }
    );
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
    cy.get('#hero-title').should('include.text', 'My Clients');
    cy.get('#hero-subtitle').should('have.text', 'Groups of APIs');
    cy.get('#logout-btn').should('exist').and('be.visible');
  });

  it('Check Navigation Bar', () => {
    cy.get('.navigation-links').should('have.length', 4);
    cy.get('#navbar-link-router-my\\ clients').should('have.class', 'primary');
  });

  it('Check Search Input', () => {
    //Check default
    cy.get('.search-input').should('be.visible');
    cy.get('#search-input-clear-btn').should('not.exist');
    cy.get('#DemoClient-1\\.0').should('exist');
    cy.checkTocLength(1);

    // Check search with client name
    cy.get('.search-input').type('DemoClient');
    cy.get('#DemoClient-1\\.0').should('exist');
    cy.checkTocLength(1);
    cy.get('#search-input-clear-btn').should('exist').click();
    cy.get('.search-input').should('have.text', '');
    cy.get('#DemoClient-1\\.0').should('exist');

    // Check search with API name
    cy.get('.search-input').type('TestApi');
    cy.get('#DemoClient-1\\.0').should('exist');
    cy.checkTocLength(1);
    cy.get('#search-input-clear-btn').should('exist').click();
    cy.get('.search-input').should('have.text', '');

    // Check search with wrong input
    cy.get('.search-input').type('DemoApi');
    cy.get('#DemoClient-1\\.0').should('not.exist');
    cy.checkTocLength(0);
    cy.get('#no-data-text').should('exist').and('be.visible');
    cy.get('#search-input-clear-btn').should('exist').click();
    cy.get('.search-input').should('have.text', '');
    cy.get('#DemoClient-1\\.0').should('exist');
  });

  it('Check Client Header', () => {
    cy.get('#DemoClient-1\\.0').within(() => {
      cy.get('.app-header').should('include.text', 'DemoClient - 1.0');
      cy.get('#client-delete-btn').should('exist');
      cy.get('#client-status-label').should('include.text', 'Registered');
    });
  });

  it('Check Summary Section', () => {
    cy.get('#TestApi1').within(() => {
      cy.get('.app-api-menu-entry').should('have.length', 4);
      cy.get('#api-menu-summary').should('have.class', 'primary');

      cy.get('#DemoClient-1\\.0-TestApi1-1\\.0')
        .should('exist')
        .and('include.text', 'TestApi1 1.0 - Silver');
      cy.get('.app-api-header-labels')
        .should('be.visible')
        .and('include.text', 'Published');

      // Check API Endpoint
      cy.get('#api-endpoint-icon').should('include.text', 'public');
      cy.get('#api-endpoint-input').should(
        'have.value',
        'https://vagrantguest/pas/gateway/CypressTestOrg/TestApi1/1.0'
      );
      cy.get('#api-endpoint-copy-btn-icon').should(
        'include.text',
        'content_copy'
      );
      cy.get('#api-endpoint-copy-btn').click();
      cy.get('#api-endpoint-input')
        .invoke('val')
        .then((apiEndpoint: string) => {
          cy.compareWithClipboard(apiEndpoint);
        });

      // Check API Key
      cy.get('#api-key-icon').should('include.text', 'vpn_key');
      cy.get('#api-key-copy-btn-icon').should('include.text', 'content_copy');
      cy.get('#api-key-copy-btn').click();
      cy.get('#api-key-input')
        .invoke('val')
        .then((apiKey: string) => {
          cy.compareWithClipboard(apiKey);
        });

      // Check Rate Limiting Policy Card
      cy.get('#policy-card-RateLimitingPolicy').within(() => {
        cy.get('#policy-card-icon').should('include.text', 'tune');
        cy.get('#policy-card-title').should('include.text', 'Rate Limit');
        cy.get('#short-limit').should('include.text', '20 requests per Hour');
        cy.get('#probe').should('include.text', '0 / 20');
      });

      // Check Transfer Quota Policy Card
      cy.get('#policy-card-TransferQuotaPolicy').within(() => {
        cy.get('#policy-card-icon').should('include.text', 'import_export');
        cy.get('#policy-card-title').should('include.text', 'Transfer Quota');
        cy.get('#short-limit').should(
          'include.text',
          '200 Bytes data per Hour'
        );
        cy.get('#probe').should('include.text', '0 / 200 Bytes');
      });
    });
  });

  it('Check Description Section', () => {
    cy.get('#TestApi1').within(() => {
      cy.get('#api-menu-description')
        .should('not.have.class', 'primary')
        .click();
      cy.get('#api-menu-description').should('have.class', 'primary');

      // Check API Header
      cy.get('#DemoClient-1\\.0-TestApi1-1\\.0')
        .should('exist')
        .and('include.text', 'TestApi1 1.0 - Silver');
      cy.get('.app-api-header-labels')
        .should('be.visible')
        .and('include.text', 'Published');

      // Check content
      cy.get('markdown').should(
        'include.text',
        'No further description has been stored for this API'
      );
    });
  });

  it('Check Use API Section', () => {
    cy.get('#TestApi1').within(() => {
      cy.get('#api-menu-use-api').should('not.have.class', 'primary').click();
      cy.get('#api-menu-use-api').should('have.class', 'primary');

      // Check API Header
      cy.get('#DemoClient-1\\.0-TestApi1-1\\.0')
        .should('exist')
        .and('include.text', 'TestApi1 1.0 - Silver');
      cy.get('.app-api-header-labels')
        .should('be.visible')
        .and('include.text', 'Published');

      // Check Content
      cy.get('#api-docs-btn').should('exist').and('be.visible');
      cy.get('#api-docs-download-icon').should('include.text', 'download');

      // Check API-Key
      cy.get('#api-key-copy-icon').should('include.text', 'content_copy');
      cy.get('#api-key-copy-btn').click();
      cy.get('#api-key-input')
        .invoke('val')
        .then((apiKey: string) => {
          cy.compareWithClipboard(apiKey);
        });

      // Check API-Endpoint
      cy.get('#api-endpoint-copy-icon').should('include.text', 'content_copy');
      cy.get('#api-endpoint-copy-btn').click();
      cy.get('#api-endpoint-input')
        .invoke('val')
        .then((apiEndpoint: string) => {
          cy.compareWithClipboard(apiEndpoint);
        });
    });
  });

  it('Check Policies Section', () => {
    cy.get('#TestApi1').within(() => {
      context('Check Rate Limiting Policy', () => {
        const policyContainerId = '#policy-container-RateLimitingPolicy';
        cy.checkPolicyCard(policyContainerId, {
          icon: 'tune',
          title: 'Rate Limiting Policy',
          description: 'Requests are limited to 20 requests per Hour',
          headerInfo:
            'The following headers in the response contain useful information relating to rate limit:',
          headerListEntries: 3,
          headerLimit:
            'X-RateLimit-Limit: Rate limit allowance (per time period)',
          headerRemaining:
            'X-RateLimit-Remaining: Rate limit allowance remaining',
          headerReset:
            'X-RateLimit-Reset: The time the rate limit will be reset'
        });

        cy.checkGaugeChart(policyContainerId, '#policy-chart-usage', {
          name: 'Rate Limit',
          icon: 'tune',
          infoHeader: 'Usage (hour)',
          usage: '0 / 20',
          bottomText: '0.00 % used'
        });

        cy.checkGaugeChart(policyContainerId, '#policy-chart-reset', {
          name: 'Reset Timer',
          icon: 'timer',
          infoHeader: 'Countdown',
          usage: '/ 60 min',
          bottomText: 'Resets at'
        });
      });

      context('Check Transfer Quota Policy', () => {
        const policyContainerId = '#policy-container-TransferQuotaPolicy';
        cy.checkPolicyCard(policyContainerId, {
          icon: 'export_import',
          title: 'Transfer Quota Policy',
          description:
            'You can transfer a maximum of 200 Bytes of data per Hour',
          headerInfo:
            'The following headers in the response contain useful information relating to quota limit:',
          headerListEntries: 3,
          headerLimit:
            'X-TransferQuota-Limit: Quota limit allowance (per time period)',
          headerRemaining:
            'X-TransferQuota-Remaining: Quota limit allowance remaining',
          headerReset:
            'X-TransferQuota-Reset: The time the quota limit will be reset'
        });

        cy.checkGaugeChart(policyContainerId, '#policy-chart-usage', {
          name: 'Transfer Quota',
          icon: 'export_import',
          infoHeader: 'Usage (hour)',
          usage: '0 / 200 Bytes',
          bottomText: '0.00 % used'
        });

        cy.checkGaugeChart(policyContainerId, '#policy-chart-reset', {
          name: 'Reset Timer',
          icon: 'timer',
          infoHeader: 'Countdown',
          usage: '/ 60 min',
          bottomText: 'Resets at'
        });
      });
    });
  });

  it('Check Client Deletion', () => {
    cy.get('#DemoClient-1\\.0').within(() => {
      cy.get('#client-delete-btn').click();
      cy.contains('Remove').click();
    });
  });
});
