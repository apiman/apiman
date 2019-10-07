package io.apiman.plugins.uniqueheader;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.uniqueheader.beans.UniqueHeaderBean;
import io.apiman.plugins.uniqueheader.util.Messages;
import org.apache.commons.lang.StringUtils;

import java.util.UUID;

/**
 * A policy that sets a unique value in a named header.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class UniqueHeaderPolicy extends AbstractMappedPolicy<UniqueHeaderBean> {
    private static final Messages MESSAGES = new Messages("io.apiman.plugins.uniqueheader", "UniqueHeaderPolicy");

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

        request.setHeaders(handleHeader(request.getHeaders(), config));
        chain.doApply(request);
    }

    /**
     * Handles the headers in regards to io.apiman.plugins.uniqueheader.beans.UniqueHeaderBean#isOverwriteHeader()
     * by overwriting the io.apiman.plugins.uniqueheader.beans.UniqueHeaderBean#getHeaderName() if the overwriteHeader
     * value is true
     * @param headers provided in the request
     * @param config the policy's configuration information
     * @return the header map containing the new header
     */
    protected HeaderMap handleHeader(HeaderMap headers, UniqueHeaderBean config) {
        if(!config.isOverwriteHeaderValue()){
            if(!isHeaderPresent(headers, config)){
                return putHeader(headers, config);
            }
            return headers;
        }

        return putHeader(headers, config);
    }

    /**
     * Check if the expected header was provided
     * @param headers provided in the request
     * @param config the policy's configuration information
     * @return true if the header is present
     */
    protected boolean isHeaderPresent(HeaderMap headers, UniqueHeaderBean config) {
        return headers.containsKey(config.getHeaderName());
    }

    /**
     * Adds a new header into the request.getHeaders()
     * @param headerMap the request headers
     * @param config the policy's configuration information
     * @return the header map containing the new added header
     */
    protected HeaderMap putHeader(HeaderMap headerMap, UniqueHeaderBean config) {
        headerMap.put(config.getHeaderName(), generateUniqueString());
        return headerMap;
    }

    /**
     * @return a unique String to set in the header
     */
    protected String generateUniqueString() {
        return UUID.randomUUID().toString();
    }
}
