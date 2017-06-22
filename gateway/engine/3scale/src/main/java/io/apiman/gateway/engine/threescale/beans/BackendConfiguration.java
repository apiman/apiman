
package io.apiman.gateway.engine.threescale.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "account_id",
    "name",
    "oneline_description",
    "description",
    "txt_api",
    "txt_support",
    "txt_features",
    "created_at",
    "updated_at",
    "logo_file_name",
    "logo_content_type",
    "logo_file_size",
    "state",
    "intentions_required",
    "draft_name",
    "infobar",
    "terms",
    "display_provider_keys",
    "tech_support_email",
    "admin_support_email",
    "credit_card_support_email",
    "buyers_manage_apps",
    "buyers_manage_keys",
    "custom_keys_enabled",
    "buyer_plan_change_permission",
    "buyer_can_select_plan",
    "notification_settings",
    "default_application_plan_id",
    "default_service_plan_id",
    "buyer_can_see_log_requests",
    "default_end_user_plan_id",
    "end_user_registration_required",
    "tenant_id",
    "system_name",
    "backend_version",
    "mandatory_app_key",
    "buyer_key_regenerate_enabled",
    "support_email",
    "referrer_filters_required",
    "deployment_option",
    "proxiable?",
    "backend_authentication_type",
    "backend_authentication_value",
    "proxy"
})
public class BackendConfiguration implements Serializable
{

    @JsonProperty("id")
    private long id;
    @JsonProperty("account_id")
    private long accountId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("oneline_description")
    private String onelineDescription;
    @JsonProperty("description")
    private String description;
    @JsonProperty("txt_api")
    private String txtApi;
    @JsonProperty("txt_support")
    private String txtSupport;
    @JsonProperty("txt_features")
    private String txtFeatures;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("logo_file_name")
    private String logoFileName;
    @JsonProperty("logo_content_type")
    private String logoContentType;
    @JsonProperty("logo_file_size")
    private Object logoFileSize;
    @JsonProperty("state")
    private String state;
    @JsonProperty("intentions_required")
    private boolean intentionsRequired;
    @JsonProperty("draft_name")
    private String draftName;
    @JsonProperty("infobar")
    private Object infobar;
    @JsonProperty("terms")
    private Object terms;
    @JsonProperty("display_provider_keys")
    private boolean displayProviderKeys;
    @JsonProperty("tech_support_email")
    private String techSupportEmail;
    @JsonProperty("admin_support_email")
    private String adminSupportEmail;
    @JsonProperty("credit_card_support_email")
    private String creditCardSupportEmail;
    @JsonProperty("buyers_manage_apps")
    private boolean buyersManageApps;
    @JsonProperty("buyers_manage_keys")
    private boolean buyersManageKeys;
    @JsonProperty("custom_keys_enabled")
    private boolean customKeysEnabled;
    @JsonProperty("buyer_plan_change_permission")
    private String buyerPlanChangePermission;
    @JsonProperty("buyer_can_select_plan")
    private boolean buyerCanSelectPlan;
    @JsonProperty("notification_settings")
    private Object notificationSettings;
    @JsonProperty("default_application_plan_id")
    private long defaultApplicationPlanId;
    @JsonProperty("default_service_plan_id")
    private long defaultServicePlanId;
    @JsonProperty("buyer_can_see_log_requests")
    private boolean buyerCanSeeLogRequests;
    @JsonProperty("default_end_user_plan_id")
    private String defaultEndUserPlanId;
    @JsonProperty("end_user_registration_required")
    private boolean endUserRegistrationRequired;
    @JsonProperty("tenant_id")
    private long tenantId;
    @JsonProperty("system_name")
    private String systemName;
    @JsonProperty("backend_version")
    private String backendVersion;
    @JsonProperty("mandatory_app_key")
    private boolean mandatoryAppKey;
    @JsonProperty("buyer_key_regenerate_enabled")
    private boolean buyerKeyRegenerateEnabled;
    @JsonProperty("support_email")
    private String supportEmail;
    @JsonProperty("referrer_filters_required")
    private boolean referrerFiltersRequired;
    @JsonProperty("deployment_option")
    private String deploymentOption;
    @JsonProperty("proxiable?")
    private boolean proxiable;
    @JsonProperty("backend_authentication_type")
    private String backendAuthenticationType;
    @JsonProperty("backend_authentication_value")
    private String backendAuthenticationValue;
    @JsonProperty("proxy")
    private Proxy proxy;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = -6934510462570339651L;

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    public BackendConfiguration withId(long id) {
        this.id = id;
        return this;
    }

    @JsonProperty("account_id")
    public long getAccountId() {
        return accountId;
    }

    @JsonProperty("account_id")
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public BackendConfiguration withAccountId(long accountId) {
        this.accountId = accountId;
        return this;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public BackendConfiguration withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("oneline_description")
    public Object getOnelineDescription() {
        return onelineDescription;
    }

    @JsonProperty("oneline_description")
    public void setOnelineDescription(String onelineDescription) {
        this.onelineDescription = onelineDescription;
    }

    public BackendConfiguration withOnelineDescription(String onelineDescription) {
        this.onelineDescription = onelineDescription;
        return this;
    }

    @JsonProperty("description")
    public Object getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public BackendConfiguration withDescription(String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("txt_api")
    public Object getTxtApi() {
        return txtApi;
    }

    @JsonProperty("txt_api")
    public void setTxtApi(String txtApi) {
        this.txtApi = txtApi;
    }

    public BackendConfiguration withTxtApi(String txtApi) {
        this.txtApi = txtApi;
        return this;
    }

    @JsonProperty("txt_support")
    public Object getTxtSupport() {
        return txtSupport;
    }

    @JsonProperty("txt_support")
    public void setTxtSupport(String txtSupport) {
        this.txtSupport = txtSupport;
    }

    public BackendConfiguration withTxtSupport(String txtSupport) {
        this.txtSupport = txtSupport;
        return this;
    }

    @JsonProperty("txt_features")
    public Object getTxtFeatures() {
        return txtFeatures;
    }

    @JsonProperty("txt_features")
    public void setTxtFeatures(String txtFeatures) {
        this.txtFeatures = txtFeatures;
    }

    public BackendConfiguration withTxtFeatures(String txtFeatures) {
        this.txtFeatures = txtFeatures;
        return this;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public BackendConfiguration withCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BackendConfiguration withUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @JsonProperty("logo_file_name")
    public Object getLogoFileName() {
        return logoFileName;
    }

    @JsonProperty("logo_file_name")
    public void setLogoFileName(String logoFileName) {
        this.logoFileName = logoFileName;
    }

    public BackendConfiguration withLogoFileName(String logoFileName) {
        this.logoFileName = logoFileName;
        return this;
    }

    @JsonProperty("logo_content_type")
    public Object getLogoContentType() {
        return logoContentType;
    }

    @JsonProperty("logo_content_type")
    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }

    public BackendConfiguration withLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
        return this;
    }

    @JsonProperty("logo_file_size")
    public Object getLogoFileSize() {
        return logoFileSize;
    }

    @JsonProperty("logo_file_size")
    public void setLogoFileSize(Object logoFileSize) {
        this.logoFileSize = logoFileSize;
    }

    public BackendConfiguration withLogoFileSize(Object logoFileSize) {
        this.logoFileSize = logoFileSize;
        return this;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    public BackendConfiguration withState(String state) {
        this.state = state;
        return this;
    }

    @JsonProperty("intentions_required")
    public boolean isIntentionsRequired() {
        return intentionsRequired;
    }

    @JsonProperty("intentions_required")
    public void setIntentionsRequired(boolean intentionsRequired) {
        this.intentionsRequired = intentionsRequired;
    }

    public BackendConfiguration withIntentionsRequired(boolean intentionsRequired) {
        this.intentionsRequired = intentionsRequired;
        return this;
    }

    @JsonProperty("draft_name")
    public String getDraftName() {
        return draftName;
    }

    @JsonProperty("draft_name")
    public void setDraftName(String draftName) {
        this.draftName = draftName;
    }

    public BackendConfiguration withDraftName(String draftName) {
        this.draftName = draftName;
        return this;
    }

    @JsonProperty("infobar")
    public Object getInfobar() {
        return infobar;
    }

    @JsonProperty("infobar")
    public void setInfobar(Object infobar) {
        this.infobar = infobar;
    }

    public BackendConfiguration withInfobar(Object infobar) {
        this.infobar = infobar;
        return this;
    }

    @JsonProperty("terms")
    public Object getTerms() {
        return terms;
    }

    @JsonProperty("terms")
    public void setTerms(Object terms) {
        this.terms = terms;
    }

    public BackendConfiguration withTerms(Object terms) {
        this.terms = terms;
        return this;
    }

    @JsonProperty("display_provider_keys")
    public boolean isDisplayProviderKeys() {
        return displayProviderKeys;
    }

    @JsonProperty("display_provider_keys")
    public void setDisplayProviderKeys(boolean displayProviderKeys) {
        this.displayProviderKeys = displayProviderKeys;
    }

    public BackendConfiguration withDisplayProviderKeys(boolean displayProviderKeys) {
        this.displayProviderKeys = displayProviderKeys;
        return this;
    }

    @JsonProperty("tech_support_email")
    public Object getTechSupportEmail() {
        return techSupportEmail;
    }

    @JsonProperty("tech_support_email")
    public void setTechSupportEmail(String techSupportEmail) {
        this.techSupportEmail = techSupportEmail;
    }

    public BackendConfiguration withTechSupportEmail(String techSupportEmail) {
        this.techSupportEmail = techSupportEmail;
        return this;
    }

    @JsonProperty("admin_support_email")
    public Object getAdminSupportEmail() {
        return adminSupportEmail;
    }

    @JsonProperty("admin_support_email")
    public void setAdminSupportEmail(String adminSupportEmail) {
        this.adminSupportEmail = adminSupportEmail;
    }

    public BackendConfiguration withAdminSupportEmail(String adminSupportEmail) {
        this.adminSupportEmail = adminSupportEmail;
        return this;
    }

    @JsonProperty("credit_card_support_email")
    public Object getCreditCardSupportEmail() {
        return creditCardSupportEmail;
    }

    @JsonProperty("credit_card_support_email")
    public void setCreditCardSupportEmail(String creditCardSupportEmail) {
        this.creditCardSupportEmail = creditCardSupportEmail;
    }

    public BackendConfiguration withCreditCardSupportEmail(String creditCardSupportEmail) {
        this.creditCardSupportEmail = creditCardSupportEmail;
        return this;
    }

    @JsonProperty("buyers_manage_apps")
    public boolean isBuyersManageApps() {
        return buyersManageApps;
    }

    @JsonProperty("buyers_manage_apps")
    public void setBuyersManageApps(boolean buyersManageApps) {
        this.buyersManageApps = buyersManageApps;
    }

    public BackendConfiguration withBuyersManageApps(boolean buyersManageApps) {
        this.buyersManageApps = buyersManageApps;
        return this;
    }

    @JsonProperty("buyers_manage_keys")
    public boolean isBuyersManageKeys() {
        return buyersManageKeys;
    }

    @JsonProperty("buyers_manage_keys")
    public void setBuyersManageKeys(boolean buyersManageKeys) {
        this.buyersManageKeys = buyersManageKeys;
    }

    public BackendConfiguration withBuyersManageKeys(boolean buyersManageKeys) {
        this.buyersManageKeys = buyersManageKeys;
        return this;
    }

    @JsonProperty("custom_keys_enabled")
    public boolean isCustomKeysEnabled() {
        return customKeysEnabled;
    }

    @JsonProperty("custom_keys_enabled")
    public void setCustomKeysEnabled(boolean customKeysEnabled) {
        this.customKeysEnabled = customKeysEnabled;
    }

    public BackendConfiguration withCustomKeysEnabled(boolean customKeysEnabled) {
        this.customKeysEnabled = customKeysEnabled;
        return this;
    }

    @JsonProperty("buyer_plan_change_permission")
    public String getBuyerPlanChangePermission() {
        return buyerPlanChangePermission;
    }

    @JsonProperty("buyer_plan_change_permission")
    public void setBuyerPlanChangePermission(String buyerPlanChangePermission) {
        this.buyerPlanChangePermission = buyerPlanChangePermission;
    }

    public BackendConfiguration withBuyerPlanChangePermission(String buyerPlanChangePermission) {
        this.buyerPlanChangePermission = buyerPlanChangePermission;
        return this;
    }

    @JsonProperty("buyer_can_select_plan")
    public boolean isBuyerCanSelectPlan() {
        return buyerCanSelectPlan;
    }

    @JsonProperty("buyer_can_select_plan")
    public void setBuyerCanSelectPlan(boolean buyerCanSelectPlan) {
        this.buyerCanSelectPlan = buyerCanSelectPlan;
    }

    public BackendConfiguration withBuyerCanSelectPlan(boolean buyerCanSelectPlan) {
        this.buyerCanSelectPlan = buyerCanSelectPlan;
        return this;
    }

    @JsonProperty("notification_settings")
    public Object getNotificationSettings() {
        return notificationSettings;
    }

    @JsonProperty("notification_settings")
    public void setNotificationSettings(Object notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public BackendConfiguration withNotificationSettings(Object notificationSettings) {
        this.notificationSettings = notificationSettings;
        return this;
    }

    @JsonProperty("default_application_plan_id")
    public long getDefaultApplicationPlanId() {
        return defaultApplicationPlanId;
    }

    @JsonProperty("default_application_plan_id")
    public void setDefaultApplicationPlanId(long defaultApplicationPlanId) {
        this.defaultApplicationPlanId = defaultApplicationPlanId;
    }

    public BackendConfiguration withDefaultApplicationPlanId(long defaultApplicationPlanId) {
        this.defaultApplicationPlanId = defaultApplicationPlanId;
        return this;
    }

    @JsonProperty("default_service_plan_id")
    public long getDefaultServicePlanId() {
        return defaultServicePlanId;
    }

    @JsonProperty("default_service_plan_id")
    public void setDefaultServicePlanId(long defaultServicePlanId) {
        this.defaultServicePlanId = defaultServicePlanId;
    }

    public BackendConfiguration withDefaultServicePlanId(long defaultServicePlanId) {
        this.defaultServicePlanId = defaultServicePlanId;
        return this;
    }

    @JsonProperty("buyer_can_see_log_requests")
    public boolean isBuyerCanSeeLogRequests() {
        return buyerCanSeeLogRequests;
    }

    @JsonProperty("buyer_can_see_log_requests")
    public void setBuyerCanSeeLogRequests(boolean buyerCanSeeLogRequests) {
        this.buyerCanSeeLogRequests = buyerCanSeeLogRequests;
    }

    public BackendConfiguration withBuyerCanSeeLogRequests(boolean buyerCanSeeLogRequests) {
        this.buyerCanSeeLogRequests = buyerCanSeeLogRequests;
        return this;
    }

    @JsonProperty("default_end_user_plan_id")
    public Object getDefaultEndUserPlanId() {
        return defaultEndUserPlanId;
    }

    @JsonProperty("default_end_user_plan_id")
    public void setDefaultEndUserPlanId(String defaultEndUserPlanId) {
        this.defaultEndUserPlanId = defaultEndUserPlanId;
    }

    public BackendConfiguration withDefaultEndUserPlanId(String defaultEndUserPlanId) {
        this.defaultEndUserPlanId = defaultEndUserPlanId;
        return this;
    }

    @JsonProperty("end_user_registration_required")
    public boolean isEndUserRegistrationRequired() {
        return endUserRegistrationRequired;
    }

    @JsonProperty("end_user_registration_required")
    public void setEndUserRegistrationRequired(boolean endUserRegistrationRequired) {
        this.endUserRegistrationRequired = endUserRegistrationRequired;
    }

    public BackendConfiguration withEndUserRegistrationRequired(boolean endUserRegistrationRequired) {
        this.endUserRegistrationRequired = endUserRegistrationRequired;
        return this;
    }

    @JsonProperty("tenant_id")
    public long getTenantId() {
        return tenantId;
    }

    @JsonProperty("tenant_id")
    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public BackendConfiguration withTenantId(long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @JsonProperty("system_name")
    public String getSystemName() {
        return systemName;
    }

    @JsonProperty("system_name")
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public BackendConfiguration withSystemName(String systemName) {
        this.systemName = systemName;
        return this;
    }

    @JsonProperty("backend_version")
    public String getBackendVersion() {
        return backendVersion;
    }

    @JsonProperty("backend_version")
    public void setBackendVersion(String backendVersion) {
        this.backendVersion = backendVersion;
    }

    public BackendConfiguration withBackendVersion(String backendVersion) {
        this.backendVersion = backendVersion;
        return this;
    }

    @JsonProperty("mandatory_app_key")
    public boolean isMandatoryAppKey() {
        return mandatoryAppKey;
    }

    @JsonProperty("mandatory_app_key")
    public void setMandatoryAppKey(boolean mandatoryAppKey) {
        this.mandatoryAppKey = mandatoryAppKey;
    }

    public BackendConfiguration withMandatoryAppKey(boolean mandatoryAppKey) {
        this.mandatoryAppKey = mandatoryAppKey;
        return this;
    }

    @JsonProperty("buyer_key_regenerate_enabled")
    public boolean isBuyerKeyRegenerateEnabled() {
        return buyerKeyRegenerateEnabled;
    }

    @JsonProperty("buyer_key_regenerate_enabled")
    public void setBuyerKeyRegenerateEnabled(boolean buyerKeyRegenerateEnabled) {
        this.buyerKeyRegenerateEnabled = buyerKeyRegenerateEnabled;
    }

    public BackendConfiguration withBuyerKeyRegenerateEnabled(boolean buyerKeyRegenerateEnabled) {
        this.buyerKeyRegenerateEnabled = buyerKeyRegenerateEnabled;
        return this;
    }

    @JsonProperty("support_email")
    public String getSupportEmail() {
        return supportEmail;
    }

    @JsonProperty("support_email")
    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public BackendConfiguration withSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
        return this;
    }

    @JsonProperty("referrer_filters_required")
    public boolean isReferrerFiltersRequired() {
        return referrerFiltersRequired;
    }

    @JsonProperty("referrer_filters_required")
    public void setReferrerFiltersRequired(boolean referrerFiltersRequired) {
        this.referrerFiltersRequired = referrerFiltersRequired;
    }

    public BackendConfiguration withReferrerFiltersRequired(boolean referrerFiltersRequired) {
        this.referrerFiltersRequired = referrerFiltersRequired;
        return this;
    }

    @JsonProperty("deployment_option")
    public String getDeploymentOption() {
        return deploymentOption;
    }

    @JsonProperty("deployment_option")
    public void setDeploymentOption(String deploymentOption) {
        this.deploymentOption = deploymentOption;
    }

    public BackendConfiguration withDeploymentOption(String deploymentOption) {
        this.deploymentOption = deploymentOption;
        return this;
    }

    @JsonProperty("proxiable?")
    public boolean isProxiable() {
        return proxiable;
    }

    @JsonProperty("proxiable?")
    public void setProxiable(boolean proxiable) {
        this.proxiable = proxiable;
    }

    public BackendConfiguration withProxiable(boolean proxiable) {
        this.proxiable = proxiable;
        return this;
    }

    @JsonProperty("backend_authentication_type")
    public String getBackendAuthenticationType() {
        return backendAuthenticationType;
    }

    @JsonProperty("backend_authentication_type")
    public void setBackendAuthenticationType(String backendAuthenticationType) {
        this.backendAuthenticationType = backendAuthenticationType;
    }

    public BackendConfiguration withBackendAuthenticationType(String backendAuthenticationType) {
        this.backendAuthenticationType = backendAuthenticationType;
        return this;
    }

    @JsonProperty("backend_authentication_value")
    public String getBackendAuthenticationValue() {
        return backendAuthenticationValue;
    }

    @JsonProperty("backend_authentication_value")
    public void setBackendAuthenticationValue(String backendAuthenticationValue) {
        this.backendAuthenticationValue = backendAuthenticationValue;
    }

    public BackendConfiguration withBackendAuthenticationValue(String backendAuthenticationValue) {
        this.backendAuthenticationValue = backendAuthenticationValue;
        return this;
    }

    @JsonProperty("proxy")
    public Proxy getProxy() {
        return proxy;
    }

    @JsonProperty("proxy")
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public BackendConfiguration withProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public BackendConfiguration withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @SuppressWarnings("nls")
    public AuthTypeEnum getAuthType() {
        switch(getBackendVersion()) {
        case "1":
            return AuthTypeEnum.API_KEY;
        case "2":
            return AuthTypeEnum.APP_ID;
        case "oauth": // Yes, really.
            return AuthTypeEnum.OAUTH;
        default:
            throw new IllegalStateException("Unrecognised auth type: " + getBackendVersion());
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(accountId).append(name).append(onelineDescription).append(description).append(txtApi)
                .append(txtSupport).append(txtFeatures).append(createdAt).append(updatedAt).append(logoFileName).append(logoContentType)
                .append(logoFileSize).append(state).append(intentionsRequired).append(draftName).append(infobar).append(terms)
                .append(displayProviderKeys).append(techSupportEmail).append(adminSupportEmail).append(creditCardSupportEmail)
                .append(buyersManageApps).append(buyersManageKeys).append(customKeysEnabled).append(buyerPlanChangePermission)
                .append(buyerCanSelectPlan).append(notificationSettings).append(defaultApplicationPlanId).append(defaultServicePlanId)
                .append(buyerCanSeeLogRequests).append(defaultEndUserPlanId).append(endUserRegistrationRequired).append(tenantId).append(systemName)
                .append(backendVersion).append(mandatoryAppKey).append(buyerKeyRegenerateEnabled).append(supportEmail).append(referrerFiltersRequired)
                .append(deploymentOption).append(proxiable).append(backendAuthenticationType).append(backendAuthenticationValue).append(proxy)
                .append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BackendConfiguration) == false) {
            return false;
        }
        BackendConfiguration rhs = ((BackendConfiguration) other);
        return new EqualsBuilder().append(id, rhs.id).append(accountId, rhs.accountId).append(name, rhs.name)
                .append(onelineDescription, rhs.onelineDescription).append(description, rhs.description).append(txtApi, rhs.txtApi)
                .append(txtSupport, rhs.txtSupport).append(txtFeatures, rhs.txtFeatures).append(createdAt, rhs.createdAt)
                .append(updatedAt, rhs.updatedAt).append(logoFileName, rhs.logoFileName).append(logoContentType, rhs.logoContentType)
                .append(logoFileSize, rhs.logoFileSize).append(state, rhs.state).append(intentionsRequired, rhs.intentionsRequired)
                .append(draftName, rhs.draftName).append(infobar, rhs.infobar).append(terms, rhs.terms)
                .append(displayProviderKeys, rhs.displayProviderKeys).append(techSupportEmail, rhs.techSupportEmail)
                .append(adminSupportEmail, rhs.adminSupportEmail).append(creditCardSupportEmail, rhs.creditCardSupportEmail)
                .append(buyersManageApps, rhs.buyersManageApps).append(buyersManageKeys, rhs.buyersManageKeys)
                .append(customKeysEnabled, rhs.customKeysEnabled).append(buyerPlanChangePermission, rhs.buyerPlanChangePermission)
                .append(buyerCanSelectPlan, rhs.buyerCanSelectPlan).append(notificationSettings, rhs.notificationSettings)
                .append(defaultApplicationPlanId, rhs.defaultApplicationPlanId).append(defaultServicePlanId, rhs.defaultServicePlanId)
                .append(buyerCanSeeLogRequests, rhs.buyerCanSeeLogRequests).append(defaultEndUserPlanId, rhs.defaultEndUserPlanId)
                .append(endUserRegistrationRequired, rhs.endUserRegistrationRequired).append(tenantId, rhs.tenantId)
                .append(systemName, rhs.systemName).append(backendVersion, rhs.backendVersion).append(mandatoryAppKey, rhs.mandatoryAppKey)
                .append(buyerKeyRegenerateEnabled, rhs.buyerKeyRegenerateEnabled).append(supportEmail, rhs.supportEmail)
                .append(referrerFiltersRequired, rhs.referrerFiltersRequired).append(deploymentOption, rhs.deploymentOption)
                .append(proxiable, rhs.proxiable).append(backendAuthenticationType, rhs.backendAuthenticationType)
                .append(backendAuthenticationValue, rhs.backendAuthenticationValue).append(proxy, rhs.proxy)
                .append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
