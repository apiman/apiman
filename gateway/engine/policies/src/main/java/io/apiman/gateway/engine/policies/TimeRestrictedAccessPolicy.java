/*
 * Copyright 2016 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.IgnoredResourcesConfig;
import io.apiman.gateway.engine.policies.config.TimeRestrictedAccess;
import io.apiman.gateway.engine.policies.config.TimeRestrictedAccessConfig;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

/**
 * Policy that restrict access to resource by time when resource can be accessed.
 */
public class TimeRestrictedAccessPolicy extends AbstractMappedPolicy<TimeRestrictedAccessConfig> {
    
    /**
     * Constructor.
     */
    public TimeRestrictedAccessPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.AbstractPolicy#getConfigurationClass()
     */
    @Override
    protected Class<TimeRestrictedAccessConfig> getConfigurationClass() {
        return TimeRestrictedAccessConfig.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest,
     *      io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object,
     *      io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, TimeRestrictedAccessConfig config,
            IPolicyChain<ApiRequest> chain) {
        if (canProcessRequest(config, request.getDestination())) {
            super.doApply(request, context, config, chain);
        } else {
            IPolicyFailureFactoryComponent ffactory = context
                    .getComponent(IPolicyFailureFactoryComponent.class);
            String msg = Messages.i18n.format("TimeRestrictedAccessPolicy.Unavailable", //$NON-NLS-1$
                    request.getDestination());
            PolicyFailure failure = ffactory.createFailure(PolicyFailureType.Other,
                    PolicyFailureCodes.ACCESS_TIME_RESTRICTED, msg);
            chain.doFailure(failure);
        }
    }

    
    /**
     * Evaluates whether the destination provided matches any of the configured
     * pathsToIgnore and matches specified time range.
     *
     * @param config
     *            The {@link IgnoredResourcesConfig} containing the
     *            pathsToIgnore
     * @param destination
     *            The destination to evaluate

     * @return true if any path matches the destination. false otherwise
     */
    private boolean canProcessRequest(TimeRestrictedAccessConfig config, String destination) {
        if (destination == null || destination.trim().length() == 0) {
            destination = "/"; //$NON-NLS-1$
        }
        List<TimeRestrictedAccess> rulesEnabledForPath = getRulesMatchingPath(config, destination);
        if(rulesEnabledForPath.size()!=0){
            DateTime currentTime = new DateTime(DateTimeZone.UTC);
            for (TimeRestrictedAccess rule : rulesEnabledForPath) {
                boolean matchesDay = matchesDay(currentTime, rule);
                if (matchesDay) {
                    boolean matchesTime = matchesTime(rule);
                    if (matchesTime) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Returns the set of rules that match the destination path (resource location)
     * being access by the request.
     * @param config
     * @param destination
     */
    private List<TimeRestrictedAccess> getRulesMatchingPath(TimeRestrictedAccessConfig config,
            String destination) {
        List<TimeRestrictedAccess> rulesForPath = new ArrayList<>();
        for (TimeRestrictedAccess rule : config.getRules()) {
            if (destination.matches(rule.getPathPattern())){
                rulesForPath.add(rule);
            }
        }
        return rulesForPath;
    }

    /**
     * Returns true if the given DateTime matches the time range indicated by the
     * filter/rule.
     * @param currentTime
     * @param filter
     */
    private boolean matchesTime(TimeRestrictedAccess filter) {
        Date start = filter.getTimeStart();
        Date end = filter.getTimeEnd();
        if (end == null || start == null) {
            return true;
        }
        long startMs = start.getTime();
        long endMs = end.getTime();
        DateTime currentTime = new LocalTime(DateTimeZone.UTC).toDateTime(new DateTime(0l));
        long nowMs = currentTime.toDate().getTime();
        
        return nowMs >= startMs && nowMs < endMs;
    }

    /**
     * Returns true if the given time matches the day-of-week restrictions specified
     * by the included filter/rule.
     * @param currentTime
     * @param filter
     */
    private boolean matchesDay(DateTime currentTime, TimeRestrictedAccess filter) {
        Integer dayStart = filter.getDayStart();
        Integer dayEnd = filter.getDayEnd();
        int dayNow = currentTime.getDayOfWeek();
        if (dayStart >= dayEnd) {
            return dayNow >= dayStart && dayNow <= dayEnd;
        } else {
            return dayNow <= dayEnd && dayNow <= dayStart;
        }
    }
}
