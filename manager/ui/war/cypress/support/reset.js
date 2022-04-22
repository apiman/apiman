Cypress.Commands.add('deleteOrg', (orgName) => {
    const BASIC_AUTH = {
        username: 'admin',
        password: 'admin123!',
        sendImmediately: true
    };

    // Unregister all clients in this org
    cy.request({
        url: `/apiman/organizations/${orgName}/clients`,
        auth: BASIC_AUTH,
        failOnStatusCode: false
    })
    .then(response => {
        const clientList = response.body;
        if (!Array.isArray(clientList)) {
            return;
        }
        clientList.map(client => client.id)
            .forEach(id => unregisterAllClients(orgName, id));
    });

    // Retire all API Versions in this org
    cy.request({
        url: `/apiman/organizations/${orgName}/apis`,
        auth: BASIC_AUTH,
        failOnStatusCode: false
    })
    .then(response => {
        const apiList = response.body;
        if (!Array.isArray(apiList)) {
            return;
        }
        apiList.map(api => api.id)
            .forEach(id => retireAllVersions(orgName, id));
    });

    cy.request({
        method: 'DELETE',
        url: `/apiman/organizations/${orgName}`,
        auth: BASIC_AUTH,
        failOnStatusCode: false
    });

    // -- Helpers

    function retireAllVersions(orgName, apiId) {
        cy.request({
            url: `/apiman/organizations/${orgName}/apis/${apiId}/versions`,
            auth: BASIC_AUTH,
            failOnStatusCode: false
        })
        .then(response => {
            const apiVersionsList = response.body || [];
            for (let apiVersion in apiVersionsList) {
                cy.request({
                    method: 'POST',
                    url: 'apiman/actions',
                    auth: BASIC_AUTH,
                    body: {
                        'entityId': apiId,
                        'entityVersion': apiVersion.version,
                        'organizationId': orgName,
                        'type': 'retireApi'
                    },
                    failOnStatusCode: false
                });
            }
        });
    }

    function unregisterAllClients(orgName, clientId) {
        cy.request({
            url: `/apiman/organizations/${orgName}/clients/${clientId}/versions`,
            auth: BASIC_AUTH
        })
        .then(response => {
            const clientVersionList = response.body || [];
            for (let clientVersion in clientVersionList) {
                cy.request({
                    method: 'POST',
                    url: 'apiman/actions',
                    auth: BASIC_AUTH,
                    body: {
                        'entityId': clientId,
                        'entityVersion': clientVersion.version,
                        'organizationId': orgName,
                        'type': 'unregisterClient'
                    },
                    failOnStatusCode: false
                });
            }
        });
    }
});
