package io.apiman.plugins.urlwhitelist;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.urlwhitelist.beans.UrlWhitelistBean;
import io.apiman.plugins.urlwhitelist.beans.WhitelistEntryBean;
import io.apiman.plugins.urlwhitelist.util.Messages;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * A policy that only permits requests matching a whitelist.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class UrlWhitelistPolicy extends AbstractMappedPolicy<UrlWhitelistBean> {
    private static final Messages MESSAGES = new Messages("io.apiman.plugins.urlwhitelist", "UrlWhitelistPolicy");
    private static final String APIMAN_GATEWAY = "/apiman-gateway";

    /**
     * Cache of precompiled URL patterns. Note that {@link Pattern} is thread-safe.
     */
    private static final Map<String, Pattern> PATTERN_MAP = new ConcurrentHashMap<>();

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<UrlWhitelistBean> getConfigurationClass() {
        return UrlWhitelistBean.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#parseConfiguration(String)
     */
    @Override
    public UrlWhitelistBean parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
        final UrlWhitelistBean config = super.parseConfiguration(jsonConfiguration);

        // precompile patterns for performance
        for (WhitelistEntryBean whitelistEntry : config.getWhitelist()) {
            try {
                PATTERN_MAP.put(whitelistEntry.getRegex(), Pattern.compile(whitelistEntry.getRegex()));
            } catch (Exception e) {
                throw new ConfigurationParseException(MESSAGES.format("Error.CompilingPattern", whitelistEntry.getRegex()), e); //$NON-NLS-1$
            }
        }

        return config;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, UrlWhitelistBean config,
                           IPolicyChain<ApiRequest> chain) {

        // normalise, for safety
        final String normalisedPath;
        try {
            normalisedPath = getNormalisedPath(config, request);
        } catch (Exception e) {
            chain.throwError(new RuntimeException(MESSAGES.format("Error.NormalisingPath", request.getUrl()), e)); //$NON-NLS-1$
            return;
        }

        final boolean requestPermitted;
        try {
            requestPermitted = isRequestPermitted(config, normalisedPath, request.getType());
        } catch (Exception e) {
            chain.throwError(new RuntimeException(MESSAGES.format(
                    "Error.CheckingRequest", request.getType(), normalisedPath), e)); //$NON-NLS-1$
            return;
        }

        if (requestPermitted) {
            chain.doApply(request);
        } else {
            chain.doFailure(new PolicyFailure(PolicyFailureType.Authorization,
                    HttpURLConnection.HTTP_UNAUTHORIZED, MESSAGES.format("Failure.UrlNotPermitted", normalisedPath))); //$NON-NLS-1$
        }
    }

    /**
     * Normalise the request URL before evaluating any access control rules, for safety. Returns the path
     * component of the normalised URL.
     *
     * @param config  the policy configuration
     * @param request the incoming request
     * @return the normalised path
     * @throws URISyntaxException
     */
    private String getNormalisedPath(UrlWhitelistBean config, ApiRequest request) throws URISyntaxException {
        // normalise, for safety
        final URI normalisedUrl = new URI(request.getUrl()).normalize();

        String path = normalisedUrl.getPath();
        if (config.isRemovePathPrefix()) {
            if (path.startsWith(APIMAN_GATEWAY)) {
                path = path.substring(APIMAN_GATEWAY.length());
            }

            // remove org/API/version prefix, e.g. '/myorg/myapi/1.0'
            final String apiPrefix = String.format("/%s/%s/%s", //$NON-NLS-1$
                    request.getApiOrgId(),
                    request.getApiId(),
                    request.getApiVersion());

            path = path.substring(apiPrefix.length());
        }

        return path;
    }

    /**
     * Evaluates whether the request for the {@code normalisedPath} and {@code method} is permitted by
     * the rules in the {@code config}.
     *
     * @param config         the policy configuration
     * @param normalisedPath the normalised request path
     * @param method         the HTTP method
     * @return {@code true} if the request is permitted, otherwise {@code false}
     */
    private boolean isRequestPermitted(UrlWhitelistBean config, String normalisedPath, String method) {
        for (WhitelistEntryBean whitelistEntry : config.getWhitelist()) {
            final Pattern pattern = PATTERN_MAP.get(whitelistEntry.getRegex());
            if (null != pattern && pattern.matcher(normalisedPath).matches()) {
                return isMethodPermitted(whitelistEntry, method);
            }
        }
        return false;
    }

    /**
     * Evaluates whether the request for the {@code method} is permitted by the configuration of
     * the {@code whitelistEntry}.
     *
     * @param whitelistEntry the whitelist entry matching the request URL
     * @param method         the HTTP method
     * @return {@code true} if the method is permitted, otherwise {@code false}
     */
    private boolean isMethodPermitted(WhitelistEntryBean whitelistEntry, String method) {
        switch (method.toUpperCase()) {
            case "GET": //$NON-NLS-1$
                return whitelistEntry.isMethodGet();
            case "POST": //$NON-NLS-1$
                return whitelistEntry.isMethodPost();
            case "PUT": //$NON-NLS-1$
                return whitelistEntry.isMethodPut();
            case "PATCH": //$NON-NLS-1$
                return whitelistEntry.isMethodPatch();
            case "DELETE": //$NON-NLS-1$
                return whitelistEntry.isMethodDelete();
            case "HEAD": //$NON-NLS-1$
                return whitelistEntry.isMethodHead();
            case "OPTIONS": //$NON-NLS-1$
                return whitelistEntry.isMethodOptions();
            case "TRACE": //$NON-NLS-1$
                return whitelistEntry.isMethodTrace();

            default:
                throw new UnsupportedOperationException(MESSAGES.format("Error.MethodNotSupported", method)); //$NON-NLS-1$
        }
    }
}
