package io.apiman.gateway.engine.policies.limiting;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingGranularity;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingPeriod;
import io.apiman.gateway.engine.policies.probe.RateLimitingProbeConfig;
import io.apiman.gateway.engine.policy.ProbeContext;
import io.apiman.gateway.engine.rates.RateBucketPeriod;

import java.util.function.Supplier;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class BucketFactory {

    public static final String NO_USER_AVAILABLE = "NO_USER_AVAILABLE";
    public static final String NO_CLIENT_AVAILABLE = "NO_CLIENT_AVAILABLE";

    /**
     * Creates the ID of the rate bucket to use.  The ID is composed differently
     * depending on the configuration of the policy.
     */
    public String bucketId(RateLimitingConfig config, BucketIdBuilderContext context) {
        Api api = context.getApi();
        StringBuilder builder = new StringBuilder();
        // Public API in this branch
        if (context.getContract() == null) {
            builder.append("PUBLIC||"); //$NON-NLS-1$
            builder.append("||"); //$NON-NLS-1$
            builder.append(api.getOrganizationId());
            builder.append("||"); //$NON-NLS-1$
            builder.append(api.getApiId());
            builder.append("||"); //$NON-NLS-1$
            builder.append(api.getVersion());
            if (config.getGranularity() == RateLimitingGranularity.User) {
                String user = context.getUserSupplier().get();
                builder.append("||"); //$NON-NLS-1$
                builder.append(user);
            } else if (config.getGranularity() == RateLimitingGranularity.Ip) {
                builder.append("||"); //$NON-NLS-1$
                builder.append(context.getRemoteAddr());
            } else if (config.getGranularity() == RateLimitingGranularity.Api) {
            } else {
                return NO_CLIENT_AVAILABLE;
            }
        } else {
            // Have a fully valid contract in this branch.
            ApiContract contract = context.getContract();
            Client client = contract.getClient();
            String apiKey = client.getApiKey();
            builder.append(apiKey);
            if (config.getGranularity() == RateLimitingGranularity.User) {
                String user = context.getUserSupplier().get();
                if (user == null) {
                    return NO_USER_AVAILABLE;
                } else {
                    builder.append("||USER||"); //$NON-NLS-1$
                    builder.append(client.getOrganizationId());
                    builder.append("||"); //$NON-NLS-1$
                    builder.append(client.getClientId());
                    builder.append("||"); //$NON-NLS-1$
                    builder.append(user);
                }
            } else if (config.getGranularity() == RateLimitingGranularity.Client) {
                builder.append(apiKey);
                builder.append("||APP||"); //$NON-NLS-1$
                builder.append(client.getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(client.getClientId());
            } else if (config.getGranularity() == RateLimitingGranularity.Ip) {
                builder.append(apiKey);
                builder.append("||IP||"); //$NON-NLS-1$
                builder.append(client.getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(context.getRemoteAddr());
            } else {
                builder.append(apiKey);
                builder.append("||SERVICE||"); //$NON-NLS-1$
                builder.append(api.getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(api.getApiId());
            }
        }
        return builder.toString();
    }

    /**
     * Gets the appropriate bucket period from the config.
     */
    public RateBucketPeriod getPeriod(RateLimitingConfig config) {
        RateLimitingPeriod period = config.getPeriod();
        switch (period) {
            case Second:
                return RateBucketPeriod.Second;
            case Day:
                return RateBucketPeriod.Day;
            case Hour:
                return RateBucketPeriod.Hour;
            case Minute:
                return RateBucketPeriod.Minute;
            case Month:
                return RateBucketPeriod.Month;
            case Year:
                return RateBucketPeriod.Year;
            default:
                return RateBucketPeriod.Month;
        }
    }

    /**
     * Generate rate limiting bucket ID for API Request
     */
    public String bucketId(ApiRequest request, RateLimitingConfig config) {
        BucketIdBuilderContext bucketInfo = new BucketIdBuilderContext()
                .setRateLimitingConfig(config)
                .setApi(request.getApi())
                .setContract(request.getContract())
                .setUserSupplier(() -> {
                    String header = config.getUserHeader();
                    if (!request.getHeaders().containsKey(header)) {
                        return NO_USER_AVAILABLE;
                    }
                    return request.getHeaders().get(header);
                })
                .setRemoteAddr(request.getRemoteAddr());
        return bucketId(config, bucketInfo);
    }

    /**
     * Generate rate limiting bucket ID for a probe request
     */
    public String bucketId(RateLimitingProbeConfig probeConfig, ProbeContext probeContext, RateLimitingConfig config) {
        BucketIdBuilderContext bucketInfo = new BucketIdBuilderContext()
                .setRateLimitingConfig(config)
                .setApi(probeContext.getApi())
                .setContract(probeContext.getContract())
                .setUserSupplier(probeConfig::getUser)
                .setRemoteAddr(probeConfig.getCallerIp());
        return bucketId(config, bucketInfo);
    }

    // TODO: make me a record
    public static final class BucketIdBuilderContext {
        private RateLimitingConfig rateLimitingConfig;
        private Api api;
        private ApiContract contract; // Remember, with a public API we might not have a contract!
        private Supplier<String> userSupplier;
        private String remoteAddr;

        public BucketIdBuilderContext() {
        }

        public RateLimitingConfig getRateLimitingConfig() {
            return rateLimitingConfig;
        }

        public BucketIdBuilderContext setRateLimitingConfig(
                RateLimitingConfig rateLimitingConfig) {
            this.rateLimitingConfig = rateLimitingConfig;
            return this;
        }

        public ApiContract getContract() {
            return contract;
        }

        public BucketIdBuilderContext setContract(ApiContract contract) {
            this.contract = contract;
            return this;
        }

        public Supplier<String> getUserSupplier() {
            return userSupplier;
        }

        public BucketIdBuilderContext setUserSupplier(Supplier<String> userSupplier) {
            this.userSupplier = userSupplier;
            return this;
        }

        public String getRemoteAddr() {
            return remoteAddr;
        }

        public BucketIdBuilderContext setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Api getApi() {
            return api;
        }

        public BucketIdBuilderContext setApi(Api api) {
            this.api = api;
            return this;
        }
    }
}
