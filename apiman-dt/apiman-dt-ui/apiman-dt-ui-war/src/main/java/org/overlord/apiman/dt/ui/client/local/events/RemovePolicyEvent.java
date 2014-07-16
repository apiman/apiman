/*
 * Copyright 2013 JBoss Inc
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

package org.overlord.apiman.dt.ui.client.local.events;

import org.overlord.apiman.dt.api.beans.policies.PolicyBean;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Event fired when the user clicks the Remove link in the list of 
 * policies for an app, service, or plan.
 *
 * @author eric.wittmann@redhat.com
 */
public class RemovePolicyEvent extends GwtEvent<RemovePolicyEvent.Handler> {

    /**
     * Handler for {@link RemovePolicyEvent}.
     */
    public static interface Handler extends EventHandler {

        /**
         * Called when {@link RemovePolicyEvent} is fired.
         *
         * @param event the {@link RemovePolicyEvent} that was fired
         */
        public void onRemovePolicy(RemovePolicyEvent event);
    }

    /**
     * Indicates if a widget supports ok/cancel.
     */
    public static interface HasRemovePolicyHandlers extends HasHandlers {

        /**
         * Adds a handler to the widget.
         * @param handler
         */
        public HandlerRegistration addRemovePolicyHandler(Handler handler);

    }

    private static Type<Handler> TYPE;

    /**
     * Fires the event.
     * @param source
     * @param policy
     */
    public static RemovePolicyEvent fire(HasHandlers source, PolicyBean policy) {
        RemovePolicyEvent event = new RemovePolicyEvent(policy);
        if (TYPE != null)
            source.fireEvent(event);
        return event;
    }

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<Handler> getType() {
        if (TYPE == null) {
            TYPE = new Type<Handler>();
        }
        return TYPE;
    }

    private PolicyBean policy;

    /**
     * Constructor.
     * @param policy
     */
    public RemovePolicyEvent(PolicyBean policy) {
        this.setPolicy(policy);
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void dispatch(Handler handler) {
        handler.onRemovePolicy(this);
    }

    /**
     * @return the policy
     */
    public PolicyBean getPolicy() {
        return policy;
    }

    /**
     * @param policy the policy to set
     */
    public void setPolicy(PolicyBean policy) {
        this.policy = policy;
    }
}
