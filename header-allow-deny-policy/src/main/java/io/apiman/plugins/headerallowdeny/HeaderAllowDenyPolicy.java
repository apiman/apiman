package io.apiman.plugins.headerallowdeny;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.headerallowdeny.beans.CheckResult;
import io.apiman.plugins.headerallowdeny.beans.config.HeaderAllowDenyBean;
import io.apiman.plugins.headerallowdeny.beans.config.HeaderEntryBean;
import io.apiman.plugins.headerallowdeny.beans.config.HeaderRulesBean;
import io.apiman.plugins.headerallowdeny.util.Messages;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * A policy that only permits requests matching specific header values.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class HeaderAllowDenyPolicy extends AbstractMappedPolicy<HeaderAllowDenyBean> {
    private static final Messages MESSAGES = new Messages("io.apiman.plugins.headerallowdeny", "HeaderAllowDenyPolicy"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Cache of precompiled URL patterns. Note that {@link Pattern} is thread-safe.
     */
    private static final Map<String, Pattern> COMPILED_PATTERNS = new ConcurrentHashMap<>();

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<HeaderAllowDenyBean> getConfigurationClass() {
        return HeaderAllowDenyBean.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#parseConfiguration(String)
     */
    @Override
    public HeaderAllowDenyBean parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
        final HeaderAllowDenyBean config = super.parseConfiguration(jsonConfiguration);

        // precompile patterns for performance
        if (null != config.getEntries()) {
            for (HeaderEntryBean entry : config.getEntries()) {
                if (null != entry.getRules()) {
                    for (HeaderRulesBean rule : entry.getRules()) {
                        try {
                            if (!COMPILED_PATTERNS.containsKey(rule.getHeaderValueRegex())) {
                                COMPILED_PATTERNS.put(rule.getHeaderValueRegex(), Pattern.compile(rule.getHeaderValueRegex()));
                            }
                        } catch (Exception e) {
                            throw new ConfigurationParseException(MESSAGES.format("Error.CompilingPattern", rule.getHeaderValueRegex()), e); //$NON-NLS-1$
                        }
                    }
                }
            }
        }

        return config;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, HeaderAllowDenyBean config,
                           IPolicyChain<ApiRequest> chain) {

        // short circuit if no rules
        if (null == config.getEntries() || config.getEntries().isEmpty()) {
            chain.doApply(request);
            return;
        }

        for (HeaderEntryBean entry : config.getEntries()) {
            final List<String> headerValues = request.getHeaders().getAll(entry.getHeaderName().toLowerCase());

            // no such request header
            if (headerValues.isEmpty()) {
                if (!entry.isAllowIfHeaderMissing()) {
                    chain.doFailure(new PolicyFailure(PolicyFailureType.Authorization,
                            HttpURLConnection.HTTP_FORBIDDEN, MESSAGES.format("Failure.HeaderMissing", entry.getHeaderName()))); //$NON-NLS-1$
                    return;
                }
                continue;
            }

            switch (checkRequestPermitted(entry, headerValues)) {
                case NO_MATCH:
                    if (!entry.isAllowIfNoRulesMatch()) {
                        chain.doFailure(new PolicyFailure(PolicyFailureType.Authorization,
                                HttpURLConnection.HTTP_FORBIDDEN, MESSAGES.format("Failure.NoRulesMatch"))); //$NON-NLS-1$
                        return;
                    }
                    break;

                case ALLOW:
                    continue;

                case DENY:
                    chain.doFailure(new PolicyFailure(PolicyFailureType.Authorization,
                            HttpURLConnection.HTTP_FORBIDDEN, MESSAGES.format("Failure.HeaderValueNotPermitted", entry.getHeaderName(), headerValues))); //$NON-NLS-1$
                    return;
            }
        }

        chain.doApply(request);
    }

    /**
     * Evaluates whether the request header is permitted by the rules in the {@code entry}.
     *
     * @param entry        the configuration for this header name
     * @param headerValues the values for the HTTP header in this request
     * @return the result of checking all rules against all header values
     */
    private CheckResult checkRequestPermitted(HeaderEntryBean entry, List<String> headerValues) {
        if (null != entry.getRules()) {
            for (HeaderRulesBean rule : entry.getRules()) {
                final Pattern pattern = COMPILED_PATTERNS.get(rule.getHeaderValueRegex());
                if (headerValues.stream().anyMatch(headerValue -> pattern.matcher(headerValue).matches())) {
                    return rule.isAllowRequest() ? CheckResult.ALLOW : CheckResult.DENY;
                }
            }
        }
        return CheckResult.NO_MATCH;
    }
}
