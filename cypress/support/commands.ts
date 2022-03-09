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
import { IPolicyCard } from './interfaces/IPolicyCard';
import Chainable = Cypress.Chainable;
import { IGaugeChart } from './interfaces/IGaugeChart';
import { IApimanData } from '../../src/app/interfaces/ICommunication';
import { IUserCredentials } from './interfaces/IUserCredentials';

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

Cypress.Commands.add(
  'checkPolicyCard',
  (policyContainerId: string, policyCardInfo: IPolicyCard) => {
    cy.get(`${policyContainerId} > .policies > app-policy-card`).within(() => {
      cy.get('#policy-card-icon').should('include.text', policyCardInfo.icon);
      cy.get('#policy-card-title').should('include.text', policyCardInfo.title);
      cy.get('#policy-description').should(
        'include.text',
        policyCardInfo.description
      );
      cy.get('#policy-header-info').should(
        'include.text',
        policyCardInfo.headerInfo
      );
      cy.get('#policy-header-list')
        .children()
        .should('have.length', policyCardInfo.headerListEntries);
      cy.get('#policy-header-limit').should(
        'include.text',
        policyCardInfo.headerLimit
      );
      cy.get('#policy-header-remaining').should(
        'include.text',
        policyCardInfo.headerRemaining
      );
      cy.get('#policy-card-reset').should(
        'include.text',
        policyCardInfo.headerReset
      );
    });
  }
);

Cypress.Commands.add(
  'checkGaugeChart',
  (
    policyContainerId: string,
    gaugeChartId: string,
    gaugeChartInfo: IGaugeChart
  ) => {
    cy.get(
      `${policyContainerId} > policy-charts-grid > div > ${gaugeChartId}`
    ).within(() => {
      cy.get('#chart-name').should('include.text', gaugeChartInfo.name);
      cy.get('#chart-icon').should('include.text', gaugeChartInfo.icon);
      cy.get('#chart-info-header').should(
        'include.text',
        gaugeChartInfo.infoHeader
      );
      cy.get('#chart-usage').should('include.text', gaugeChartInfo.usage);
      cy.get('#chart-bottom-text').should(
        'include.text',
        gaugeChartInfo.bottomText
      );
    });
  }
);

Cypress.Commands.add('compareWithClipboard', (value: string) => {
  cy.window()
    .its('navigator.clipboard')
    .invoke('readText')
    .should('equal', value);
});

Cypress.Commands.add('checkTocLength', (clientCount: number) => {
  cy.get('#toc-client-list').children().and('have.length', clientCount);
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
  cy.visit('/marketplace');
  cy.get(`#api-card_${apiId} #details-btn`).should('exist').click();
  cy.waitUntilLoaded();
});

Cypress.Commands.add('updateApimanMetadata', (apimanData: IApimanData) => {
  const now = Date.now();
  apimanData.Metadata.id = now;
  apimanData.Metadata.exportedOn = new Date(now).toISOString();
  return apimanData as unknown as Chainable<IApimanData>;
});

Cypress.Commands.add('initApimanData', (apimanData: IApimanData) => {
  cy.fixture('userCredentials.json').then(
    (userCredentials: IUserCredentials) => {
      cy.updateApimanMetadata(apimanData).then((updatedApimanData) => {
        cy.request({
          url: (Cypress.env('apiman_endpoint') as string) + '/system/import',
          method: 'POST',
          body: updatedApimanData,
          auth: {
            user: userCredentials.adminUsername,
            pass: userCredentials.adminPassword
          }
        }).then((response: Cypress.Response<string>) => {
          expect(response.status).to.eq(200);
        });
      });
    }
  );
});

Cypress.Commands.add(
  'retireApi',
  (orgId: string, apiId: string, apiVersion: string) => {
    cy.fixture('userCredentials.json').then(
      (userCredentials: IUserCredentials) => {
        cy.request({
          url: (Cypress.env('apiman_endpoint') as string) + '/actions',
          method: 'POST',
          body: {
            type: 'retireAPI',
            entityId: apiId,
            organizationId: orgId,
            entityVersion: apiVersion
          },
          auth: {
            user: userCredentials.adminUsername,
            pass: userCredentials.adminPassword
          }
        }).then((response: Cypress.Response<string>) => {
          expect(response.status).to.eq(204);
        });
      }
    );
  }
);

Cypress.Commands.add('deleteApi', (orgId: string, apiId: string) => {
  cy.fixture('userCredentials.json').then(
    (userCredentials: IUserCredentials) => {
      cy.request({
        url:
          (Cypress.env('apiman_endpoint') as string) +
          `/organizations/${orgId}/apis/${apiId}`,
        method: 'DELETE',
        body: { organizationId: orgId, apiId: apiId },
        auth: {
          user: userCredentials.adminUsername,
          pass: userCredentials.adminPassword
        }
      }).then((response: Cypress.Response<string>) => {
        expect(response.status).to.eq(204);
      });
    }
  );
});

Cypress.Commands.add('deleteOrg', (orgId: string) => {
  cy.fixture('userCredentials.json').then(
    (userCredentials: IUserCredentials) => {
      cy.request({
        url:
          (Cypress.env('apiman_endpoint') as string) +
          `/organizations/${orgId}`,
        method: 'DELETE',
        auth: {
          user: userCredentials.adminUsername,
          pass: userCredentials.adminPassword
        }
      }).then((response: Cypress.Response<string>) => {
        expect(response.status).to.eq(204);
      });
    }
  );
});
