package io.apiman.plugins.timeoutpolicy;

import java.util.Map;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.timeoutpolicy.beans.TimeoutConfigBean;

/**
 * 
 * Set, overwrite timeout on backend request.
 *
 * @author William Beck {@literal <william.beck.pro@gmail.com>}
 */
public class TimeoutPolicy extends AbstractMappedPolicy<TimeoutConfigBean> {

    @Override
    protected Class<TimeoutConfigBean> getConfigurationClass() {
        return TimeoutConfigBean.class;
    }

    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, TimeoutConfigBean config,
            IPolicyChain<ApiRequest> chain) {
        Map<String, String> mapPropertiesOfAPI = request.getApi().getEndpointProperties();
        // update the endpoint properties. Use in the
        // io.apiman.gateway.platforms.servlet.connectors.HttpApiConnection
        String newTimeout = config.getTimeoutConnect();
        if (newTimeout != null && newTimeout.length() > 0) {
            mapPropertiesOfAPI.put("timeouts.connect", newTimeout);
        }
        newTimeout = config.getTimeoutRead();
        if (newTimeout != null && newTimeout.length() > 0) {
            mapPropertiesOfAPI.put("timeouts.read", newTimeout);
        }
        chain.doApply(request);
    }

}
