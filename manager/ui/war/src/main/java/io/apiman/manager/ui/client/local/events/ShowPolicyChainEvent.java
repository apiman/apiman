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

package io.apiman.manager.ui.client.local.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Event fired when the user chooses to display the details of a policy chain.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShowPolicyChainEvent extends GwtEvent<ShowPolicyChainEvent.Handler> {

    /**
     * Handler for {@link ShowPolicyChainEvent}.
     */
    public static interface Handler extends EventHandler {

        /**
         * Called when {@link ShowPolicyChainEvent} is fired.
         *
         * @param event the {@link ShowPolicyChainEvent} that was fired
         */
        public void onShowPolicyChain(ShowPolicyChainEvent event);
    }

    /**
     * Indicates if a widget supports ok/cancel.
     */
    public static interface HasShowPolicyChainHandlers extends HasHandlers {

        /**
         * Adds a handler to the widget.
         * @param handler
         */
        public HandlerRegistration addShowPolicyChainHandler(Handler handler);

    }

    private static Type<Handler> TYPE;

    /**
     * Fires the event.
     * @param source
     * @param planId
     */
    public static ShowPolicyChainEvent fire(HasHandlers source, String planId) {
        ShowPolicyChainEvent event = new ShowPolicyChainEvent(planId);
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

    private String planId;

    /**
     * Constructor.
     * @param planId
     */
    public ShowPolicyChainEvent(String planId) {
        this.setPlanId(planId);
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
        handler.onShowPolicyChain(this);
    }

    /**
     * @return the planId
     */
    public String getPlanId() {
        return planId;
    }

    /**
     * @param planId the planId to set
     */
    public void setPlanId(String planId) {
        this.planId = planId;
    }
}
