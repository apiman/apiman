package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.DataEncryptionContext.EntityType;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apis.ApiVersionStatusBean;
import io.apiman.manager.api.beans.apis.NewApiBean;
import io.apiman.manager.api.beans.apis.NewApiDefinitionBean;
import io.apiman.manager.api.beans.apis.NewApiVersionBean;
import io.apiman.manager.api.beans.apis.UpdateApiBean;
import io.apiman.manager.api.beans.apis.UpdateApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.data.EntityUpdatedData;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.NewPolicyBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.policies.UpdatePolicyBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiPlanSummaryBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IApiValidator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.exceptions.ApiAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ApiDefinitionNotFoundException;
import io.apiman.manager.api.rest.exceptions.ApiNotFoundException;
import io.apiman.manager.api.rest.exceptions.ApiVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.EntityStillActiveException;
import io.apiman.manager.api.rest.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidApiStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.InvalidParameterException;
import io.apiman.manager.api.rest.exceptions.InvalidVersionException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.FieldValidator;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.rest.impl.util.SwaggerWsdlHelper;
import io.apiman.manager.api.security.ISecurityContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.StreamSupport;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;

import static java.util.stream.Collectors.toList;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class ApiService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ApiService.class);
    private IStorage storage;
    private IStorageQuery query;
    private OrganizationService organizationService;
    private IApiValidator apiValidator;
    private ISecurityContext securityContext;
    private IDataEncrypter encrypter;
    private IGatewayLinkFactory gatewayLinkFactory;
    private PolicyService policyService;

    @Inject
    public ApiService(IStorage storage,
        IStorageQuery query,
        OrganizationService organizationService,
        IApiValidator apiValidator,
        ISecurityContext securityContext,
        IDataEncrypter encrypter,
        IGatewayLinkFactory gatewayLinkFactory,
        PolicyService policyService) {
        this.storage = storage;
        this.query = query;
        this.organizationService = organizationService;
        this.apiValidator = apiValidator;
        this.securityContext = securityContext;
        this.encrypter = encrypter;
        this.gatewayLinkFactory = gatewayLinkFactory;
        this.policyService = policyService;
    }

    public ApiService() {
    }

    public void deleteApi(String organizationId, String apiId)
        throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        securityContext.checkPermissions(PermissionType.apiAdmin, organizationId);
        tryAction(() -> {
            ApiBean api = getApiFromStorage(organizationId, apiId);

            Iterator<ApiVersionBean> apiVersions = storage.getAllApiVersions(organizationId, apiId);
            Iterable<ApiVersionBean> iterable = () -> apiVersions;

            List<ApiVersionBean> apiVersionBeans = StreamSupport.stream(iterable.spliterator(), false)
                .collect(toList());

            List<ApiVersionBean> registeredElems = apiVersionBeans.stream()
                .filter(clientVersion -> clientVersion.getStatus() == ApiStatus.Published)
                .limit(5)
                .collect(toList());

            if (!registeredElems.isEmpty()) {
                throw ExceptionFactory.entityStillActiveExceptionApiVersions(registeredElems);
            }

            for (ApiVersionBean apiVersion : apiVersionBeans) {
                // add apiBean to apiVersionBean, otherwise deleteApiDefinition fails for EsStorage
                apiVersion.setApi(api);
                if (apiVersionHasApiDefinition(apiVersion)) {
                    storage.deleteApiDefinition(apiVersion);
                }
            }

            storage.deleteApi(api);
            LOGGER.debug("Deleted API: {0}", api.getName()); //$NON-NLS-1$
        });
    }

    public ApiBean createApi(String organizationId, NewApiBean bean)
        throws OrganizationNotFoundException, ApiAlreadyExistsException, NotAuthorizedException,
        InvalidNameException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        FieldValidator.validateName(bean.getName());

        ApiBean newApi = new ApiBean();
        newApi.setName(bean.getName());
        newApi.setDescription(bean.getDescription());
        newApi.setId(BeanUtils.idFromName(bean.getName()));
        newApi.setCreatedOn(new Date());
        newApi.setCreatedBy(securityContext.getCurrentUser());

        return tryAction(() -> {
            GatewaySummaryBean gateway = getSingularGateway();

            OrganizationBean orgBean = organizationService.getOrg(organizationId);
            if (storage.getApi(orgBean.getId(), newApi.getId()) != null) {
                throw ExceptionFactory.apiAlreadyExistsException(bean.getName());
            }
            newApi.setOrganization(orgBean);
            // Store/persist the new API
            storage.createApi(newApi);
            storage.createAuditEntry(AuditUtils.apiCreated(newApi, securityContext));

            if (bean.getInitialVersion() != null) {
                NewApiVersionBean newApiVersion = new NewApiVersionBean();
                newApiVersion.setEndpoint(bean.getEndpoint());
                newApiVersion.setEndpointType(bean.getEndpointType());
                newApiVersion.setEndpointContentType(bean.getEndpointContentType());
                newApiVersion.setPlans(bean.getPlans());
                newApiVersion.setPublicAPI(bean.getPublicAPI());
                newApiVersion.setParsePayload(bean.getParsePayload());
                newApiVersion.setDisableKeysStrip(bean.getDisableKeysStrip());
                newApiVersion.setVersion(bean.getInitialVersion());
                newApiVersion.setDefinitionUrl(bean.getDefinitionUrl());
                newApiVersion.setDefinitionType(bean.getDefinitionType());
                createApiVersionInternal(newApiVersion, newApi, gateway);
            }

            return newApi;
        });
    }
    
    public ApiBean getApi(String organizationId, String apiId)
        throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return getApiFromStorage(organizationId, apiId);
    }

    /**
     * Gets the API from storage
     * @param organizationId the organizationId
     * @param apiId the apiId
     * @return the api
     * @throws StorageException if the API is not found
     */
    private ApiBean getApiFromStorage(String organizationId, String apiId) throws ApiNotFoundException {
        ApiBean apiBean = tryAction(() -> storage.getApi(organizationId, apiId));
        if (apiBean == null) {
            throw ExceptionFactory.apiNotFoundException(apiId);
        }
        return apiBean;
    }
    
    public SearchResultsBean<AuditEntryBean> getApiActivity(String organizationId, String apiId,
        int page, int pageSize) throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }

        final PagingBean paging = new PagingBean();
        paging.setPage(page);
        paging.setPageSize(pageSize);

        return tryAction(() -> query.auditEntity(organizationId, apiId, null, ApiBean.class, paging));
    }
    
    public List<ApiSummaryBean> listApis(String organizationId) throws OrganizationNotFoundException {
        // No permission check is needed, because this would break All Organizations UI

        // make sure the org exists
        organizationService.getOrg(organizationId);

        return tryAction(() -> {
            // Hide sensitive data and set only needed data for the UI
            if (securityContext.hasPermission(PermissionType.orgView, organizationId)) {
                return query.getApisInOrg(organizationId);
            } else {
                return RestHelper.hideSensitiveDataFromApiSummaryBeanList(query.getApisInOrg(organizationId));
            }
        });
    }
    
    public void updateApi(String organizationId, String apiId, UpdateApiBean bean)
        throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        tryAction(() -> {
            ApiBean apiForUpdate = getApiFromStorage(organizationId, apiId);
            EntityUpdatedData auditData = new EntityUpdatedData();
            if (AuditUtils.valueChanged(apiForUpdate.getDescription(), bean.getDescription())) {
                auditData.addChange("description", apiForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
                apiForUpdate.setDescription(bean.getDescription());
            }
            storage.updateApi(apiForUpdate);
            storage.createAuditEntry(AuditUtils.apiUpdated(apiForUpdate, auditData, securityContext));
        });
    }
    
    public ApiVersionBean createApiVersion(String organizationId, String apiId,
        NewApiVersionBean bean) throws ApiNotFoundException, NotAuthorizedException,
        InvalidVersionException, ApiVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        FieldValidator.validateVersion(bean.getVersion());

        ApiVersionBean newVersion = tryAction(() -> {
            GatewaySummaryBean gateway = getSingularGateway();

            ApiBean api = getApiFromStorage(organizationId, apiId);

            if (storage.getApiVersion(organizationId, apiId, bean.getVersion()) != null) {
                throw ExceptionFactory.apiVersionAlreadyExistsException(apiId, bean.getVersion());
            }

            return createApiVersionInternal(bean, api, gateway);
        });

        if (bean.isClone() && bean.getCloneVersion() != null) {
            try {
                ApiVersionBean cloneSource = getApiVersion(organizationId, apiId, bean.getCloneVersion());

                // Clone primary attributes of the API version unless those attributes
                // were included in the NewApiVersionBean.  In other words, information
                // sent as part of the "create version" payload take precedence over the
                // cloned attributes.
                UpdateApiVersionBean updatedApi = new UpdateApiVersionBean();
                if (bean.getEndpoint() == null) {
                    updatedApi.setEndpoint(cloneSource.getEndpoint());
                }
                if (bean.getEndpointType() == null) {
                    updatedApi.setEndpointType(cloneSource.getEndpointType());
                }
                if (bean.getEndpointContentType() == null) {
                    updatedApi.setEndpointContentType(cloneSource.getEndpointContentType());
                }
                updatedApi.setEndpointProperties(cloneSource.getEndpointProperties());
                updatedApi.setGateways(cloneSource.getGateways());
                if (bean.getPlans() == null) {
                    updatedApi.setPlans(cloneSource.getPlans());
                }
                if (bean.getPublicAPI() == null) {
                    updatedApi.setPublicAPI(cloneSource.isPublicAPI());
                }
                if (bean.getParsePayload() == null) {
                    updatedApi.setParsePayload(bean.getParsePayload());
                }
                newVersion = updateApiVersion(organizationId, apiId, bean.getVersion(), updatedApi);

                if (bean.getDefinitionUrl() == null) {
                    // Clone the API definition document
                    InputStream definition = null;
                    try {
                        definition = getApiDefinition(organizationId, apiId, bean.getCloneVersion()).getDefinition();
                        setApiDefinition(organizationId, apiId, newVersion.getVersion(),
                            cloneSource.getDefinitionType(), definition, cloneSource.getDefinitionUrl());
                    } catch (ApiDefinitionNotFoundException svnfe) {
                        // This is ok - it just means the API doesn't have one, so do nothing.
                    } catch (Exception sdnfe) {
                        LOGGER.error("Unable to create response", sdnfe); //$NON-NLS-1$
                    } finally {
                        IOUtils.closeQuietly(definition);
                    }
                }

                // Clone all API policies
                List<PolicySummaryBean> policies = listApiPolicies(organizationId, apiId, bean.getCloneVersion());
                for (PolicySummaryBean policySummary : policies) {
                    PolicyBean policy = getApiPolicy(organizationId, apiId, bean.getCloneVersion(), policySummary.getId());
                    NewPolicyBean npb = new NewPolicyBean();
                    npb.setDefinitionId(policy.getDefinition().getId());
                    npb.setConfiguration(policy.getConfiguration());
                    createApiPolicy(organizationId, apiId, newVersion.getVersion(), npb);
                }
            } catch (Exception e) {
                // TODO it's ok if the clone fails - we did our best
                if (e != null) {
                    Throwable t = e;
                    e = (Exception) t;
                }
            }
        }

        return newVersion;
    }

    /**
     * Creates an API version.
     */
    protected ApiVersionBean createApiVersionInternal(NewApiVersionBean bean,
        ApiBean api, GatewaySummaryBean gateway) throws Exception, StorageException {
        if (!BeanUtils.isValidVersion(bean.getVersion())) {
            throw new StorageException("Invalid/illegal API version: " + bean.getVersion()); //$NON-NLS-1$
        }

        ApiVersionBean newVersion = new ApiVersionBean();
        newVersion.setVersion(bean.getVersion());
        newVersion.setCreatedBy(securityContext.getCurrentUser());
        newVersion.setCreatedOn(new Date());
        newVersion.setModifiedBy(securityContext.getCurrentUser());
        newVersion.setModifiedOn(new Date());
        newVersion.setStatus(ApiStatus.Created);
        newVersion.setApi(api);
        newVersion.setEndpoint(bean.getEndpoint());
        newVersion.setEndpointType(bean.getEndpointType());
        newVersion.setEndpointContentType(bean.getEndpointContentType());
        newVersion.setDefinitionUrl(bean.getDefinitionUrl());
        if (bean.getPublicAPI() != null) {
            newVersion.setPublicAPI(bean.getPublicAPI());
        }
        if (bean.getParsePayload() != null) {
            newVersion.setParsePayload(bean.getParsePayload());
        }
        if (bean.getDisableKeysStrip() != null) {
            newVersion.setDisableKeysStrip(bean.getDisableKeysStrip());
        }
        if (bean.getPlans() != null) {
            newVersion.setPlans(bean.getPlans());
        }
        if (bean.getDefinitionType() != null) {
            newVersion.setDefinitionType(bean.getDefinitionType());
        } else {
            newVersion.setDefinitionType(ApiDefinitionType.None);
        }

        if (gateway != null && newVersion.getGateways() == null) {
            newVersion.setGateways(new HashSet<>());
            ApiGatewayBean sgb = new ApiGatewayBean();
            sgb.setGatewayId(gateway.getId());
            newVersion.getGateways().add(sgb);
        }

        if (apiValidator.isReady(newVersion)) {
            newVersion.setStatus(ApiStatus.Ready);
        } else {
            newVersion.setStatus(ApiStatus.Created);
        }

        // Ensure all the plans are in the right status (locked)
        Set<ApiPlanBean> plans = newVersion.getPlans();
        if (plans != null) {
            for (ApiPlanBean splanBean : plans) {
                String orgId = newVersion.getApi().getOrganization().getId();
                PlanVersionBean pvb = storage.getPlanVersion(orgId, splanBean.getPlanId(), splanBean.getVersion());
                if (pvb == null) {
                    throw new StorageException(
                        Messages.i18n.format("PlanVersionDoesNotExist", splanBean.getPlanId(), splanBean.getVersion())); //$NON-NLS-1$
                }
                if (pvb.getStatus() != PlanStatus.Locked) {
                    throw new StorageException(Messages.i18n.format("PlanNotLocked", splanBean.getPlanId(), splanBean.getVersion())); //$NON-NLS-1$
                }
            }
        }

        storage.createApiVersion(newVersion);

        if (bean.getDefinitionUrl() != null) {
            InputStream definition = null;
            try {
                definition = new URL(bean.getDefinitionUrl()).openStream();
                storage.updateApiDefinition(newVersion, definition);
            } catch (Exception e) {
                LOGGER.error("Unable to store API definition from: " + bean.getDefinitionUrl(), e); //$NON-NLS-1$
                // Set definition type silently to None
                newVersion.setDefinitionType(ApiDefinitionType.None);
                storage.updateApiVersion(newVersion);
            } finally {
                IOUtils.closeQuietly(definition);
            }
        }

        storage.createAuditEntry(AuditUtils.apiVersionCreated(newVersion, securityContext));
        return newVersion;
    }
    
    public ApiVersionBean getApiVersion(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException {
        // No permission check is needed, because this would break All APIs UI
        return tryAction(() -> {
            ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);

            if (securityContext.hasPermission(PermissionType.apiView, organizationId)) {
                decryptEndpointProperties(apiVersion);
                return apiVersion;
            } else {
                return RestHelper.hideSensitiveDataFromApiVersionBean(apiVersion);
            }
        });
    }
    
    public ApiVersionStatusBean getApiVersionStatus(String organizationId, String apiId,
        String version) throws ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        ApiVersionBean versionBean = getApiVersion(organizationId, apiId, version);
        List<PolicySummaryBean> policies = listApiPolicies(organizationId, apiId, version);
        return apiValidator.getStatus(versionBean, policies);
    }

    // TODO do not return response from service layer, only presentation layer.
    public ApiDefinitionStream getApiDefinition(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException {
        // No permission check is needed, because this would break All APIs UI
        // Allow the user to view a definition

       return tryAction(() -> {
            ApiVersionBean apiVersion = getApiVersion(organizationId, apiId, version);

            InputStream definition = storage.getApiDefinition(apiVersion);
            if (definition == null) {
                throw ExceptionFactory.apiDefinitionNotFoundException(apiId, version);
            }

            definition = updateDefinitionWithManagedEndpoint(organizationId, apiId, version, apiVersion, definition);
            return new ApiDefinitionStream(apiVersion.getDefinitionType(), definition);
       });
    }

    /**
     * Replaces the location with the location of the managed endpoint if it is a wsdl definition.
     * Replaces the host and base path with the information of the managed endpoint if it is a swagger 2+ definition.
     * Updates the definition in storage if needed.
     * @param organizationId the organizationId
     * @param apiId the apiId
     * @param version the version
     * @param definition the definition as stream
     * @param apiVersion the apiVersion
     * @return a ByteArrayInputStream with the updated definition
     * @throws IOException
     * @throws StorageException
     */
    protected InputStream updateDefinitionWithManagedEndpoint(String organizationId, String apiId, String version, ApiVersionBean apiVersion, InputStream definition) throws IOException, StorageException {
        // If it is not a published API we will not try to update the API definition. We will return definition from storage
        if (apiVersion.getStatus() != ApiStatus.Published) {
            return definition;
        }

        URL managedEndpoint = null;
        try {
            managedEndpoint = new URL(getApiVersionEndpointInfoFromStorage(apiVersion, organizationId, apiId, version).getManagedEndpoint());
        } catch (Exception e) {
            // If the gateway is not available we return the definition from storage
            return definition;
        }

        String definitionString = null;
        String updatedDefinitionString = null;
        if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerJSON) {
            definitionString = SwaggerWsdlHelper.readSwaggerStreamToString(definition);
            updatedDefinitionString = SwaggerWsdlHelper.updateSwaggerDefinitionWithEndpoint(managedEndpoint, definitionString, apiVersion, storage);
        } else if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerYAML) {
            definitionString = SwaggerWsdlHelper.convertYamlToJson(SwaggerWsdlHelper.readSwaggerStreamToString(definition));
            updatedDefinitionString = SwaggerWsdlHelper.updateSwaggerDefinitionWithEndpoint(managedEndpoint, definitionString, apiVersion, storage);
        } else if (apiVersion.getDefinitionType() == ApiDefinitionType.WSDL) {
            updatedDefinitionString = SwaggerWsdlHelper.updateLocationEndpointInWsdl(definition, managedEndpoint, apiVersion, storage);
        } else {
            return definition;
        }

        return new ByteArrayInputStream(updatedDefinitionString.getBytes(StandardCharsets.UTF_8));
    }

    public ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException, InvalidApiStatusException {
        // No permission check is needed, because this would break All APIs UI
        return tryAction(() -> {
            ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);
            if (apiVersion.getStatus() != ApiStatus.Published) {
                throw new InvalidApiStatusException(Messages.i18n.format("ApiNotPublished")); //$NON-NLS-1$
            }
            ApiVersionEndpointSummaryBean rval = getApiVersionEndpointInfoFromStorage(apiVersion, organizationId, apiId, version);
            return rval;
        });
    }

    private ApiVersionEndpointSummaryBean getApiVersionEndpointInfoFromStorage(ApiVersionBean apiVersion, String organizationId,
        String apiId, String version) throws GatewayNotFoundException, GatewayAuthenticationException, StorageException {
        Set<ApiGatewayBean> gateways = apiVersion.getGateways();
        if (gateways.isEmpty()) {
            throw new SystemErrorException("No Gateways for published API!"); //$NON-NLS-1$
        }
        GatewayBean gateway = storage.getGateway(gateways.iterator().next().getGatewayId());
        if (gateway == null) {
            throw new GatewayNotFoundException();
        } else {
            LOGGER.debug(String.format("Got endpoint summary: %s", gateway)); //$NON-NLS-1$
        }
        IGatewayLink link = gatewayLinkFactory.create(gateway);
        ApiEndpoint endpoint = link.getApiEndpoint(organizationId, apiId, version);
        ApiVersionEndpointSummaryBean rval = new ApiVersionEndpointSummaryBean();
        rval.setManagedEndpoint(endpoint.getEndpoint());
        return rval;
    }
    
    public SearchResultsBean<AuditEntryBean> getApiVersionActivity(String organizationId,
        String apiId, String version, int page, int pageSize) throws ApiVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        PagingBean paging = new PagingBean();
        paging.setPage(page);
        paging.setPageSize(pageSize);

        return tryAction(() -> query.auditEntity(organizationId, apiId, version, ApiBean.class, paging));
    }

    public ApiVersionBean updateApiVersion(String organizationId, String apiId, String version,
        UpdateApiVersionBean bean) throws ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        if (avb.isPublicAPI()) {
            if (avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        } else {
            if (avb.getStatus() == ApiStatus.Published || avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        }

        avb.setModifiedBy(securityContext.getCurrentUser());
        avb.setModifiedOn(new Date());
        EntityUpdatedData data = new EntityUpdatedData();
        if (AuditUtils.valueChanged(avb.getPlans(), bean.getPlans())) {
            data.addChange("plans", AuditUtils.asString_ApiPlanBeans(avb.getPlans()), AuditUtils.asString_ApiPlanBeans(bean.getPlans())); //$NON-NLS-1$
            if (avb.getPlans() == null) {
                avb.setPlans(new HashSet<>());
            }
            avb.getPlans().clear();
            if (bean.getPlans() != null) {
                avb.getPlans().addAll(bean.getPlans());
            }
        }
        if (AuditUtils.valueChanged(avb.getGateways(), bean.getGateways())) {
            data.addChange("gateways", AuditUtils.asString_ApiGatewayBeans(avb.getGateways()), AuditUtils.asString_ApiGatewayBeans(bean.getGateways())); //$NON-NLS-1$
            if (avb.getGateways() == null) {
                avb.setGateways(new HashSet<>());
            }
            avb.getGateways().clear();
            avb.getGateways().addAll(bean.getGateways());
        }
        if (AuditUtils.valueChanged(avb.getEndpoint(), bean.getEndpoint())) {
            // validate the endpoint is a URL
            validateEndpoint(bean.getEndpoint());
            data.addChange("endpoint", avb.getEndpoint(), bean.getEndpoint()); //$NON-NLS-1$
            avb.setEndpoint(bean.getEndpoint());
        }
        if (AuditUtils.valueChanged(avb.getEndpointType(), bean.getEndpointType())) {
            data.addChange("endpointType", avb.getEndpointType(), bean.getEndpointType()); //$NON-NLS-1$
            avb.setEndpointType(bean.getEndpointType());
        }
        if (AuditUtils.valueChanged(avb.getEndpointContentType(), bean.getEndpointContentType())) {
            data.addChange("endpointContentType", avb.getEndpointContentType(), bean.getEndpointContentType()); //$NON-NLS-1$
            avb.setEndpointContentType(bean.getEndpointContentType());
        }
        if (AuditUtils.valueChanged(avb.getEndpointProperties(), bean.getEndpointProperties())) {
            if (avb.getEndpointProperties() == null) {
                avb.setEndpointProperties(new HashMap<>());
            } else {
                avb.getEndpointProperties().clear();
            }
            if (bean.getEndpointProperties() != null) {
                avb.getEndpointProperties().putAll(bean.getEndpointProperties());
            }
        }
        if (AuditUtils.valueChanged(avb.isPublicAPI(), bean.getPublicAPI())) {
            data.addChange("publicAPI", String.valueOf(avb.isPublicAPI()), String.valueOf(bean.getPublicAPI())); //$NON-NLS-1$
            avb.setPublicAPI(bean.getPublicAPI());
        }
        if (AuditUtils.valueChanged(avb.isParsePayload(), bean.getParsePayload())) {
            data.addChange("parsePayload", String.valueOf(avb.isParsePayload()), String.valueOf(bean.getParsePayload())); //$NON-NLS-1$
            avb.setParsePayload(bean.getParsePayload());
        }

        if (AuditUtils.valueChanged(avb.getDisableKeysStrip(), bean.getDisableKeysStrip())) {
            data.addChange("disableKeysStrip", String.valueOf(avb.getDisableKeysStrip()), String.valueOf(bean.getDisableKeysStrip())); //$NON-NLS-1$
            avb.setDisableKeysStrip(bean.getDisableKeysStrip());
        }

        return tryAction(() -> {
            if (avb.getGateways() == null || avb.getGateways().isEmpty()) {
                GatewaySummaryBean gateway = getSingularGateway();
                if (gateway != null && avb.getGateways() == null) {
                    avb.setGateways(new HashSet<>());
                    ApiGatewayBean sgb = new ApiGatewayBean();
                    sgb.setGatewayId(gateway.getId());
                    avb.getGateways().add(sgb);
                }
            }

            if (avb.getStatus() != ApiStatus.Published) {
                if (apiValidator.isReady(avb)) {
                    avb.setStatus(ApiStatus.Ready);
                } else {
                    avb.setStatus(ApiStatus.Created);
                }
            } else {
                if (!apiValidator.isReady(avb)) {
                    throw ExceptionFactory.invalidApiStatusException();
                }
            }

            encryptEndpointProperties(avb);

            // Ensure all the plans are in the right status (locked)
            Set<ApiPlanBean> plans = avb.getPlans();
            if (plans != null) {
                for (ApiPlanBean splanBean : plans) {
                    String orgId = avb.getApi().getOrganization().getId();
                    PlanVersionBean pvb = storage.getPlanVersion(orgId, splanBean.getPlanId(), splanBean.getVersion());
                    if (pvb == null) {
                        throw new StorageException(Messages.i18n.format("PlanVersionDoesNotExist", splanBean.getPlanId(), splanBean.getVersion())); //$NON-NLS-1$
                    }
                    if (pvb.getStatus() != PlanStatus.Locked) {
                        throw new StorageException(Messages.i18n.format("PlanNotLocked", splanBean.getPlanId(), splanBean.getVersion())); //$NON-NLS-1$
                    }
                }
            }

            storage.updateApiVersion(avb);
            storage.createAuditEntry(AuditUtils.apiVersionUpdated(avb, data, securityContext));
            LOGGER.debug(String.format("Successfully updated API Version: %s", avb)); //$NON-NLS-1$
            decryptEndpointProperties(avb);
            return avb;
        });
    }

    public void setApiDefinition(String organizationId, String apiId, String version, ApiDefinitionType definitionType, InputStream data) {
        tryAction(() -> setApiDefinition(organizationId, apiId, version, definitionType, data, null));
    }

    public void setApiDefinition(String organizationId, String apiId, String version, NewApiDefinitionBean apiDefinition, InputStream data) {
        //log.debug(String.format("Updated API definition for %s", apiId)); //$NON-NLS-1$
        tryAction(() -> setApiDefinition(organizationId, apiId, version,
            apiDefinition.getDefinitionType(), data, apiDefinition.getDefinitionUrl()));
    }

    private void setApiDefinition(String organizationId, String apiId, String version,
        ApiDefinitionType definitionType, InputStream data, String definitionUrl) throws StorageException  {

        ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);

        if (apiVersion.getDefinitionType() != definitionType) {
            apiVersion.setDefinitionType(definitionType);
            storage.updateApiVersion(apiVersion);
        }
        // update the definition url silently in storage if it's a new one
        if ((definitionUrl != null && (apiVersion.getDefinitionUrl() == null || !apiVersion.getDefinitionUrl().equals(definitionUrl)))) {
            apiVersion.setDefinitionUrl(definitionUrl);
            storage.updateApiVersion(apiVersion);
        }
        storage.createAuditEntry(AuditUtils.apiDefinitionUpdated(apiVersion, securityContext));
        storage.updateApiDefinition(apiVersion, data);

        apiVersion.setModifiedOn(new Date());
        apiVersion.setModifiedBy(securityContext.getCurrentUser());
        storage.updateApiVersion(apiVersion);

        LOGGER.debug(String.format("Stored API definition %s: %s", apiId, apiVersion)); //$NON-NLS-1$
    }

    public List<ApiVersionSummaryBean> listApiVersions(String organizationId, String apiId)
        throws ApiNotFoundException {
        // No permission check is needed, because this would break All APIs UI
        // Try to get the API first - will throw a ApiNotFoundException if not found.
        getApi(organizationId, apiId);
        return tryAction(() -> query.getApiVersions(organizationId, apiId));
    }
    
    public List<ApiPlanSummaryBean> getApiVersionPlans(String organizationId, String apiId,
        String version) throws ApiVersionNotFoundException, NotAuthorizedException {
        // No permission check is needed, because this would break All APIs UI
        // Ensure the version exists first.
        getApiVersion(organizationId, apiId, version);

        return tryAction(() -> query.getApiVersionPlans(organizationId, apiId, version));
    }

    public PolicyBean createApiPolicy(String organizationId, String apiId, String version,
        NewPolicyBean bean) throws OrganizationNotFoundException, ApiVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        // Make sure the API exists
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        if (avb.isPublicAPI()) {
            if (avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        } else {
            if (avb.getStatus() == ApiStatus.Published || avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        }

        return tryAction(() -> {
            PolicyBean policy = policyService.createPolicy(organizationId, apiId, version, bean, PolicyType.Api);
            LOGGER.debug(String.format("Created API policy %s", avb)); //$NON-NLS-1$

            avb.setModifiedOn(new Date());
            avb.setModifiedBy(securityContext.getCurrentUser());

            return policy;
        });
    }

    public PolicyBean getApiPolicy(String organizationId, String apiId, String version, long policyId)
        throws OrganizationNotFoundException, ApiVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        // Make sure the API exists
        getApiVersion(organizationId, apiId, version);

        return policyService.getPolicy(PolicyType.Api, organizationId, apiId, version, policyId);
    }
    
    public void updateApiPolicy(String organizationId, String apiId, String version,
        long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
        ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        // Make sure the API exists
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        tryAction(() -> {
            PolicyBean policy = storage.getPolicy(PolicyType.Api, organizationId, apiId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            // TODO capture specific change values when auditing policy updates
            if (AuditUtils.valueChanged(policy.getConfiguration(), bean.getConfiguration())) {
                policy.setConfiguration(bean.getConfiguration());
            }
            policy.setModifiedOn(new Date());
            policy.setModifiedBy(securityContext.getCurrentUser());
            storage.updatePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyUpdated(policy, PolicyType.Api, securityContext));

            avb.setModifiedBy(securityContext.getCurrentUser());
            avb.setModifiedOn(new Date());
            storage.updateApiVersion(avb);

            storage.commitTx();
            LOGGER.debug(String.format("Updated API policy %s", policy)); //$NON-NLS-1$
        });
    }

    public void deleteApiPolicy(String organizationId, String apiId, String version, long policyId)
        throws OrganizationNotFoundException, ApiVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        // Make sure the API exists and is in the right status.
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        if (avb.isPublicAPI()) {
            if (avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        } else {
            if (avb.getStatus() == ApiStatus.Published || avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        }

        tryAction(() -> {
            PolicyBean policy = this.storage.getPolicy(PolicyType.Api, organizationId, apiId, version,
                policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            storage.deletePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyRemoved(policy, PolicyType.Api, securityContext));

            avb.setModifiedBy(securityContext.getCurrentUser());
            avb.setModifiedOn(new Date());
            storage.updateApiVersion(avb);

            LOGGER.debug(String.format("Deleted API %s policy: %s", apiId, policy)); //$NON-NLS-1$
        });
    }
    
    public void deleteApiDefinition(String organizationId, String apiId, String version)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        tryAction(() -> {
            ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);
            apiVersion.setDefinitionType(ApiDefinitionType.None);
            apiVersion.setModifiedBy(securityContext.getCurrentUser());
            apiVersion.setModifiedOn(new Date());
            storage.createAuditEntry(AuditUtils.apiDefinitionDeleted(apiVersion, securityContext));
            storage.deleteApiDefinition(apiVersion);
            storage.updateApiVersion(apiVersion);
            LOGGER.debug(String.format("Deleted API %s definition %s", apiId, apiVersion)); //$NON-NLS-1$
        });
    }

    public List<PolicySummaryBean> listApiPolicies(String organizationId, String apiId, String version)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        // Try to get the API first - will throw an exception if not found.
        getApiVersion(organizationId, apiId, version);
        return tryAction(() -> query.getPolicies(organizationId, apiId, version, PolicyType.Api));
    }

    public void reorderApiPolicies(String organizationId, String apiId, String version,
        PolicyChainBean policyChain) throws OrganizationNotFoundException,
        ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        // Make sure the API exists
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        tryAction(() -> {
            List<Long> newOrder = new ArrayList<>(policyChain.getPolicies().size());
            for (PolicySummaryBean psb : policyChain.getPolicies()) {
                newOrder.add(psb.getId());
            }
            storage.reorderPolicies(PolicyType.Api, organizationId, apiId, version, newOrder);
            storage.createAuditEntry(AuditUtils.policiesReordered(avb, PolicyType.Api, securityContext));

            avb.setModifiedBy(securityContext.getCurrentUser());
            avb.setModifiedOn(new Date());
            storage.updateApiVersion(avb);
        });
    }

    public PolicyChainBean getApiPolicyChain(String organizationId, String apiId, String version,
        String planId) throws ApiVersionNotFoundException, PlanNotFoundException {
        // No permission check is needed, because this would break All APIs UI

        // Try to get the API first - will throw an exception if not found.
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        return tryAction(() -> {
            String planVersion = null;
            Set<ApiPlanBean> plans = avb.getPlans();
            if (plans != null) {
                for (ApiPlanBean apiPlanBean : plans) {
                    if (apiPlanBean.getPlanId().equals(planId)) {
                        planVersion = apiPlanBean.getVersion();
                        break;
                    }
                }
            }
            if (planVersion == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }

            // Hide sensitive data and set only needed data for the UI
            List<PolicySummaryBean> apiPolicies = RestHelper.hideSensitiveDataFromPolicySummaryBeanList(
                securityContext, query.getPolicies(organizationId, apiId, version, PolicyType.Api));

            List<PolicySummaryBean> planPolicies = RestHelper.hideSensitiveDataFromPolicySummaryBeanList(
                securityContext, query.getPolicies(organizationId, planId, planVersion, PolicyType.Plan));

            PolicyChainBean chain = new PolicyChainBean();
            chain.getPolicies().addAll(planPolicies);
            chain.getPolicies().addAll(apiPolicies);
            return chain;
        });
    }

    public List<ContractSummaryBean> getApiVersionContracts(String organizationId,
        String apiId, String version, int page, int pageSize) throws ApiVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        // Try to get the API first - will throw an exception if not found.
        getApiVersion(organizationId, apiId, version);

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize == 0 ? 20 : pageSize;

        return tryAction(() -> {
            List<ContractSummaryBean> contracts = query.getContracts(organizationId, apiId, version, finalPage, finalPageSize);
            LOGGER.debug(String.format("Got API %s version %s contracts: %s", apiId, version, contracts)); //$NON-NLS-1$
            return contracts;
        });
    }

    /**
     * Make sure we've got a valid URL.
     */
    private void validateEndpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new InvalidParameterException(Messages.i18n.format("OrganizationResourceImpl.InvalidEndpointURL")); //$NON-NLS-1$
        }
    }

    private ApiVersionBean getApiVersionFromStorage(String organizationId, String apiId, String version) {
        ApiVersionBean apiVersion = tryAction(() -> storage.getApiVersion(organizationId, apiId, version));
        if (apiVersion == null) {
            throw ExceptionFactory.apiVersionNotFoundException(apiId, version);
        }
        return apiVersion;
    }


    /**
     * Checks if the api version has an api definition
     *
     * @param apiVersion the apiVersion
     * @return true if the version has a definition, else false
     */
    private boolean apiVersionHasApiDefinition(ApiVersionBean apiVersion) throws StorageException {
        // additional check if the document really exists in the storage
        return apiVersion.getDefinitionType() != null
            && apiVersion.getDefinitionType() != ApiDefinitionType.None
            && storage.getApiDefinition(apiVersion) != null;
    }

    /**
     * @return a {@link GatewayBean} iff there is a single configured gateway in the system
     * @throws StorageException
     */
    private GatewaySummaryBean getSingularGateway() throws StorageException {
        List<GatewaySummaryBean> gateways = query.listGateways();
        if (gateways != null && gateways.size() == 1) {
            return gateways.get(0);
        } else {
            return null;
        }
    }

    /**
     * Decrypt the endpoint properties
     */
    private void decryptEndpointProperties(ApiVersionBean versionBean) {
        Map<String, String> endpointProperties = versionBean.getEndpointProperties();
        if (endpointProperties != null) {
            for (Entry<String, String> entry : endpointProperties.entrySet()) {
                DataEncryptionContext ctx = new DataEncryptionContext(
                    versionBean.getApi().getOrganization().getId(),
                    versionBean.getApi().getId(),
                    versionBean.getVersion(),
                    EntityType.Api);
                entry.setValue(encrypter.decrypt(entry.getValue(), ctx));
            }
        }
    }

    /**
     * Encrypt the endpoint properties
     */
    private void encryptEndpointProperties(ApiVersionBean versionBean) {
        Map<String, String> endpointProperties = versionBean.getEndpointProperties();
        if (endpointProperties != null) {
            for (Entry<String, String> entry : endpointProperties.entrySet()) {
                DataEncryptionContext ctx = new DataEncryptionContext(
                    versionBean.getApi().getOrganization().getId(),
                    versionBean.getApi().getId(),
                    versionBean.getVersion(),
                    EntityType.Api);
                entry.setValue(encrypter.encrypt(entry.getValue(), ctx));
            }
        }
    }

    // TODO(msavy): put with rest of DTOs when get to that phase
    public static final class ApiDefinitionStream {
        private final ApiDefinitionType definitionType;
        private final InputStream definition;

        public ApiDefinitionStream(ApiDefinitionType definitionType, InputStream definition) {
            this.definitionType = definitionType;
            this.definition = definition;
        }

        public ApiDefinitionType getDefinitionType() {
            return definitionType;
        }

        public InputStream getDefinition() {
            return definition;
        }
    }
}
