package io.apiman.plugins.uniqueheader;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.uniqueheader.beans.UniqueHeaderBean;
import io.apiman.plugins.uniqueheader.util.Messages;
import org.apache.commons.lang.StringUtils;

import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;

/**
 * A policy that sets a unique value in a named header.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class UniqueHeaderPolicy extends AbstractMappedPolicy<UniqueHeaderBean> {
    private static final Messages MESSAGES = new Messages("io.apiman.plugins.uniqueheader", "UniqueHeaderPolicy");
    private static final String UNIQUE_HEADER_KEY = "unique.header.value";

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<UniqueHeaderBean> getConfigurationClass() {
        return UniqueHeaderBean.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#parseConfiguration(String)
     */
    @Override
    public UniqueHeaderBean parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
        final UniqueHeaderBean config = super.parseConfiguration(jsonConfiguration);

        // validate configuration
        if (StringUtils.isBlank(config.getHeaderName())) {
            throw new ConfigurationParseException(MESSAGES.format("Error.BlankHeaderName"));
        }

        return config;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, UniqueHeaderBean config,
                           IPolicyChain<ApiRequest> chain) {

        handleHeader(request.getHeaders(), context, config);
        chain.doApply(request);
    }

    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, UniqueHeaderBean config,
                           IPolicyChain<ApiResponse> chain) {

        if (TRUE.equals(config.getResponseHeader())) {
            final String uniqueValue = context.getAttribute(UNIQUE_HEADER_KEY, null);
            if (nonNull(uniqueValue)) {
                response.getHeaders().put(config.getHeaderName(), uniqueValue);
            }
        }
        super.doApply(response, context, config, chain);
    }

    /**
     * Handles the headers in regards to io.apiman.plugins.uniqueheader.beans.UniqueHeaderBean#isOverwriteHeader()
     * by overwriting the io.apiman.plugins.uniqueheader.beans.UniqueHeaderBean#getHeaderName() if the overwriteHeader
     * value is true
     * @param headers provided in the request
     * @param config the policy's configuration information
     */
    private void handleHeader(HeaderMap headers, IPolicyContext context, UniqueHeaderBean config) {
        String headerValue = null;
        if (!config.isOverwriteHeaderValue()) {
            headerValue = headers.get(config.getHeaderName());
        }
        if (StringUtils.isBlank(headerValue)) {
            headerValue = generateUniqueString();
        }
        headers.put(config.getHeaderName(), headerValue);
        context.setAttribute(UNIQUE_HEADER_KEY, headerValue);
    }

    /**
     * @return a unique String to set in the header
     */
    private String generateUniqueString() {
        return UUID.randomUUID().toString();
    }
}
