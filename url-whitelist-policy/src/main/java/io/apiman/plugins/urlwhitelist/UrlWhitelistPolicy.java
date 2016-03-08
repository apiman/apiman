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
import java.util.HashMap;
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
    private HashMap<String, Pattern> patternMap;

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
        patternMap = new HashMap<>();
        for (WhitelistEntryBean whitelistEntry : config.getWhitelist()) {
            try {
                patternMap.put(whitelistEntry.getRegex(), Pattern.compile(whitelistEntry.getRegex()));
            } catch (Exception e) {
                throw new ConfigurationParseException(MESSAGES.format("Error.CompilingPattern", whitelistEntry.getRegex()), e);
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
            chain.throwError(new RuntimeException(MESSAGES.format("Error.NormalisingPath", request.getUrl()), e));
            return;
        }

        final boolean requestPermitted;
        try {
            requestPermitted = isRequestPermitted(config, normalisedPath, request.getType());
        } catch (Exception e) {
            chain.throwError(new RuntimeException(MESSAGES.format(
                    "Error.CheckingRequest", request.getType(), normalisedPath), e));
            return;
        }

        if (requestPermitted) {
            chain.doApply(request);
        } else {
            chain.doFailure(new PolicyFailure(PolicyFailureType.Authorization,
                    HttpURLConnection.HTTP_FORBIDDEN, MESSAGES.format("Failure.UrlNotPermitted", normalisedPath)));
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
            final String apiPrefix = String.format("/%s/%s/%s",
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
            if (patternMap.get(whitelistEntry.getRegex()).matcher(normalisedPath).matches()) {
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
            case "GET":
                return whitelistEntry.isMethodGet();
            case "POST":
                return whitelistEntry.isMethodPost();
            case "PUT":
                return whitelistEntry.isMethodPut();
            case "PATCH":
                return whitelistEntry.isMethodPatch();
            case "DELETE":
                return whitelistEntry.isMethodDelete();
            case "HEAD":
                return whitelistEntry.isMethodHead();
            case "OPTIONS":
                return whitelistEntry.isMethodOptions();
            case "TRACE":
                return whitelistEntry.isMethodTrace();

            default:
                throw new UnsupportedOperationException(MESSAGES.format("Error.MethodNotSupported", method));
        }
    }
}
