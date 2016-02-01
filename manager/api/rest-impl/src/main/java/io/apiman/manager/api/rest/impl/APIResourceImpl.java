package io.apiman.manager.api.rest.impl;

/**
 * Created by e050764 on 1/20/16.
 */

import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.rest.contract.IAPIResource;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.rest.contract.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidApiStatusException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.i18n.Messages;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.RoleNotFoundException;
import io.apiman.manager.api.core.exceptions.StorageException;


import java.util.List;
import java.util.Set;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@RequestScoped
public class APIResourceImpl implements IAPIResource {
    @Inject
    IStorage storage;
    @Inject
    ISecurityContext securityContext;
    @Inject @ApimanLogger(APIResourceImpl.class) IApimanLogger log;

    /**
     * Delete an unpublished API (all versions).
     */
    public void delete(String orgId, String apiId) throws InvalidApiStatusException, RoleNotFoundException, NotAuthorizedException{
        ApiBean api;
        List<ApiVersionSummaryBean> verSummaries;

        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        try{
            verSummaries = storage.getApiVersions(orgId, apiId);
        }
        catch(StorageException e){
            log.error(e);
            throw new SystemErrorException(e);
        }

        try {
            storage.beginTx();

            api = storage.getApi(orgId, apiId);

            if (api == null) {
                throw ExceptionFactory.apiNotFoundException(apiId);
            }
            else if (api.getNumPublished() != null) {
                //Retired APIs have 0 for number published.
                throw new InvalidApiStatusException(Messages.i18n.format("ApiPublished"));
            }

            for(ApiVersionSummaryBean verSummary: verSummaries){
                ApiVersionBean version = storage.getApiVersion(orgId, api.getId(), verSummary.getVersion());
                Set<ApiPlanBean> plans = version.getPlans();

                for(ApiPlanBean plan: plans){
                    //There can be multiple versions of the plan associated with an API version.
                    storage.deleteApiVersionPlan(version.getId(), plan.getPlanId());
                    storage.deleteEndpointProperties(version.getId());
                    storage.deleteApiDefinition(version);
                }

                storage.deleteApiVersion(version);
                storage.deleteEntityAudit(AuditEntityType.Api, api.getId(), api.getOrganization().getId());
            }

            storage.deleteApi(api);

            storage.commitTx();

        } catch (AbstractRestException e) {
            storage.rollbackTx();
            log.error(e);
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            log.error(e);
            throw new SystemErrorException(e);

        }
    }
}