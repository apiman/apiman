package io.apiman.plugins.urlwhitelist;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.urlwhitelist.beans.UrlWhitelistBean;
import io.apiman.plugins.urlwhitelist.beans.WhitelistEntryBean;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * A policy that only permits requests matching a whitelist.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class UrlWhitelistPolicy extends AbstractMappedPolicy<UrlWhitelistBean> {
    private static final String APIMAN_GATEWAY = "/apiman-gateway";

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<UrlWhitelistBean> getConfigurationClass() {
        return UrlWhitelistBean.class;
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
            chain.throwError(new RuntimeException(String.format(
                    "Error getting normalised path from: %s", request.getUrl()), e));
            return;
        }

        final boolean requestPermitted;
        try {
            requestPermitted = isRequestPermitted(config, normalisedPath, request.getType());
        } catch (Exception e) {
            chain.throwError(new RuntimeException(String.format(
                    "Error checking if request is permitted: %s %s", request.getType(), normalisedPath), e));
            return;
        }

        if (requestPermitted) {
            chain.doApply(request);
        } else {
            chain.doFailure(new PolicyFailure(PolicyFailureType.Authorization,
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    String.format("Normalised URL '%s' not permitted.", normalisedPath)));
        }
    }

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

    private boolean isRequestPermitted(UrlWhitelistBean config, String normalisedUrl, String method) {
        for (WhitelistEntryBean whitelistEntry : config.getWhitelist()) {
            if (Pattern.compile(whitelistEntry.getRegex()).matcher(normalisedUrl).matches()) {
                return isMethodPermitted(whitelistEntry, method);
            }
        }
        return false;
    }

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

            default:
                throw new UnsupportedOperationException(String.format("Method '%s' is not supported", method));
        }
    }
}
