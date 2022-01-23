describe('Apiman e2e UI smoke test', () => {

    before(() => {
        cy.clearCookies();
        cy.visit('/');
        cy.typeLogin('admin', 'admin123!')
    });

    beforeEach(() => {
        Cypress.Cookies.preserveOnce('JSESSIONID', 'OAuth_Token_Request_State');
        cy.visit('/');
    });

    it('Create the Organization via NavBar and edit the description', () => {
        cy.get('#apiman-sidebar-orgs').trigger('mouseover');
        cy.get('#apiman-sidebar-orgs-new').click();
        cy.get('.btn-primary').should('be.disabled');
        cy.get('#apiman-entityname').type('TestOrganization');
        cy.get('#apiman-description').type('Description of TestOrganization');
        cy.get('.btn-primary').should('be.enabled').click();
        cy.get('#descriptionWrapper').should('include.text', 'Description of TestOrganization');
        cy.get('#descriptionWrapper').click();
        cy.get('.autoExpand').type('{selectall}{del}Edited Description of TestOrganization');
        cy.get('.fa-check').click();
        cy.get('#descriptionWrapper').should('include.text', 'Edited Description of TestOrganization');
    })

    it('Delete the Organization', () => {
        cy.visitTestOrganization();
        cy.server();
        cy.route('DELETE', 'apiman/organizations/**').as('deleteOrg');
        cy.get('.dropdown-cog').click();
        cy.get('.org-delete').click();
        cy.get('.btn-danger').should('be.disabled');
        cy.get('.apiman-form-control').click();
        cy.get('.apiman-form-control').type('TestOrganization');
        cy.get('.btn-danger').should('be.enabled').click();
        cy.wait('@deleteOrg').should('have.property', 'status', 204);
    })

    it('Recreate the Organization via Button', () => {
        cy.get('#apiman-sidebar-orgs').trigger('mouseover');
        cy.get('#apiman-sidebar-orgs-my-orgs').click();
        cy.get('#new-org').click();
        cy.get('.btn-primary').should('be.disabled');
        cy.get('#apiman-entityname').click();
        cy.get('#apiman-entityname').type('TestOrganization');
        cy.get('#apiman-description').type('Description of TestOrganization');
        cy.get('.btn-primary').should('be.enabled').click();
    })

    it('Create the Plan and edit the description', () => {
        cy.visitTestOrganization();
        cy.contains('New Plan').click();
        cy.get('#apiman-entityname').type('TestPlan');
        cy.get('#apiman-description').click();
        cy.get('#apiman-description').type('Description of TestPlan');
        cy.get('#create-plan').click();
        cy.get('#descriptionWrapper').should('include.text', 'Description of TestPlan');
        cy.get('#descriptionWrapper').click();
        cy.get('.autoExpand').type('{selectall}{del}Edited Description of TestPlan');
        cy.get('.fa-check').click();
        cy.get('#descriptionWrapper').should('include.text', 'Edited Description of TestPlan');
    });

    it('Delete the unlocked Plan', () => {
        cy.visitTestPlan();
        cy.server()
        cy.route('DELETE', 'apiman/organizations/*/plans/*').as('deletePlan');
        cy.get('.dropdown-cog').click();
        cy.get('.plan-delete').click();
        cy.get('.btn-danger').should('be.disabled');
        cy.get('.apiman-form-control').click();
        cy.get('.apiman-form-control').type('TestPlan');
        cy.get('.btn-danger').should('be.enabled').click();
        cy.wait('@deletePlan').should('have.property', 'status', 204)
    });

    it('Recreate the Plan', () => {
        cy.visitTestOrganization();
        cy.contains('New Plan').click();
        cy.get('#apiman-entityname').type('TestPlan');
        cy.get('#apiman-description').click();
        cy.get('#apiman-description').type('Description of TestPlan');
        cy.get('#create-plan').click();
    });

    it('Add Rate Limiting Policy to the Plan and lock it', () => {
        cy.visitTestPlan();
        cy.get('#tab-policies').click();
        cy.get('.pull-right:nth-child(1)').click();
        cy.get('.select2-chosen:nth-child(1)').click();
        cy.contains('Rate Limiting Policy').click();
        cy.get('#add-policy').should('be.disabled');
        cy.get('#num-requests').click();
        cy.get('#num-requests').type('1');
        cy.get('.btn-group:nth-child(4) > .btn .apiman-label-faded').click();
        cy.get('.btn-group:nth-child(4) li:nth-child(2) .text').click();
        cy.get('.filter-option > .apiman-label-faded').click();
        cy.get('.btn-group:nth-child(6) li:nth-child(2) .text').click();
        cy.get('#add-policy').should('be.enabled').click();
        cy.get('.col-md-5 > :nth-child(3) > .btn').click();
        cy.get('.apiman-label').should('have.text', 'Locked');
    })

    it('Create the API via NavBar and edit the description', () => {
        cy.get('#apiman-sidebar-apis').trigger('mouseover');
        cy.get('#apiman-sidebar-apis-new').click();
        cy.get('#create-api').should('be.disabled');
        cy.get('#apiman-entityname').type('TestApi');
        cy.get('#apiman-description').click();
        cy.get('#apiman-description').type('Description of TestApi');
        cy.get('#create-api').should('be.enabled').click();
        cy.get('#descriptionWrapper').should('include.text', 'Description of TestApi');
        cy.get('#descriptionWrapper').click();
        cy.get('.autoExpand').type('{selectall}{del}Edited Description of TestApi');
        cy.get('.fa-check').click();
        cy.get('#descriptionWrapper').should('include.text', 'Edited Description of TestApi');
    });

    it('Deletes the API', () => {
        cy.visitTestApi();
        cy.get('.dropdown-cog').click();
        cy.get('.api-delete').click();
        cy.get('.btn-danger').should('be.disabled');
        cy.get('.apiman-form-control').click();
        cy.get('.apiman-form-control').type('TestApi');
        cy.get('.btn-danger').should('be.enabled').click();
    })

    it('Recreates the API via Button', () => {
        cy.visitTestOrganization();
        cy.get('#tab-apis').click();
        cy.get('.btn-primary:nth-child(1)').click();
        cy.get('#create-api').should('be.disabled');
        cy.get('#apiman-entityname').type('TestApi');
        cy.get('#apiman-description').click();
        cy.get('#apiman-description').type('Description of TestApi');
        cy.get('#create-api').should('be.enabled').click();
    });

    it('Add the API Implementation', () => {
        cy.visitTestApi();
        cy.get('#tab-impl').click();
        cy.get('.apiman-form-control:nth-child(2)').click();
        cy.get('.apiman-form-control:nth-child(2)').type('https://petstore.swagger.io/v2/');
        cy.get('.actions > .btn-primary').click();
    });

    it('Choose Plan and publish API', () => {
        cy.visitTestApi();
        cy.get('#tab-plans').click();
        cy.get('[data-cy=apiman-plan-enabled-checkbox-TestPlan]').click();
        cy.get('[data-cy=save]').click();
        cy.contains('Publish').should('be.enabled');
        cy.contains('Publish').click();
    })

    it('Create the Client via NavBar and edit the description', () => {
        cy.get('#apiman-sidebar-clients').trigger('mouseover');
        cy.get('#apiman-sidebar-clients-new').click();
        cy.get('#apiman-entityname').type('TestClient');
        cy.get('#apiman-description').type('Description of TestClient');
        cy.get('#create-client').click();
        cy.get('#descriptionWrapper').should('include.text', 'Description of TestClient')
        cy.get('#descriptionWrapper').click();
        cy.get('.autoExpand').type('{selectall}{del}Edited Description of TestClient');
        cy.get('.fa-check').click();
        cy.get('#descriptionWrapper').should('include.text', 'Edited Description of TestClient')
    });

    it('Delete the Client', () => {
        cy.visitTestClient();
        cy.get('.fa-cog').should('be.visible').click();
        cy.get('.client-app-delete').click();
        cy.get('.btn-danger').should('be.disabled');
        cy.get('.apiman-form-control').click();
        cy.get('.apiman-form-control').type('TestClient');
        cy.get('.btn-danger').should('be.enabled').click();
    });

    it('Recreate the Client via Button', () => {
        cy.visitTestOrganization();
        cy.get('#tab-clients').click();
        cy.get('.btn-primary').click();
        cy.get('#create-client').should('be.disabled');
        cy.get('#apiman-entityname').type('TestClient');
        cy.get('#apiman-description').type('Description of TestClient');
        cy.get('#create-client').should('be.enabled').click();
    });

    it('Create the Contract', () => {
        cy.visitTestClient();
        cy.get('#tab-contracts').click();
        cy.get('.pull-right:nth-child(2)').click();
        cy.get('.api > .body').click();
        cy.get('#api > span').click();
        cy.get('.item').click();
        cy.get('.modal-footer > .btn-primary').click();
        cy.get('#create-contract').click();
    });

    it('Register the Client', () => {
        cy.visitTestClient();
        cy.get('.btn-primary:nth-child(1)').click();
        cy.get('.apiman-label-success').should('have.text', 'Registered');
    });
});
