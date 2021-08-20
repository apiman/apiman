package io.apiman.manager.sso.keycloak.approval;

import io.apiman.manager.sso.keycloak.KeycloakOptsMapShim;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class AccountApprovalRequiredActionFactory implements RequiredActionFactory {

    public static final String PROVIDER_ID = "approval-required";
    private static final Logger LOGGER = Logger.getLogger(AccountApprovalRequiredActionFactory.class);
    private static AccountApprovalRequiredActionProvider APPROVAL_REQUIRED_ACTION;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayText() {
        return "Account Approval Required";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return APPROVAL_REQUIRED_ACTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Scope config) {
        AccountApprovalOptions approvalConfig = new AccountApprovalOptions(new KeycloakOptsMapShim(config));
        APPROVAL_REQUIRED_ACTION = new AccountApprovalRequiredActionProvider(approvalConfig);
        LOGGER.debugv("Apiman SPI {0} initialised", getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
