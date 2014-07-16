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

import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Event fired when the user clicks the Request Membership button in the list of 
 * organizations (consumer UI).
 *
 * @author eric.wittmann@redhat.com
 */
public class RequestMembershipEvent extends GwtEvent<RequestMembershipEvent.Handler> {

    /**
     * Handler for {@link RequestMembershipEvent}.
     */
    public static interface Handler extends EventHandler {

        /**
         * Called when {@link RequestMembershipEvent} is fired.
         *
         * @param event the {@link RequestMembershipEvent} that was fired
         */
        public void onRequestMembership(RequestMembershipEvent event);
    }

    /**
     * Indicates if a widget supports ok/cancel.
     */
    public static interface HasRequestMembershipHandlers extends HasHandlers {

        /**
         * Adds a handler to the widget.
         * @param handler
         */
        public HandlerRegistration addRequestMembershipHandler(Handler handler);

    }

    private static Type<Handler> TYPE;

    /**
     * Fires the event.
     * @param source
     * @param organization
     */
    public static RequestMembershipEvent fire(HasHandlers source, OrganizationBean organization) {
        RequestMembershipEvent event = new RequestMembershipEvent(organization);
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

    private OrganizationBean organization;

    /**
     * Constructor.
     * @param organization
     */
    public RequestMembershipEvent(OrganizationBean organization) {
        this.setOrganization(organization);
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
        handler.onRequestMembership(this);
    }

    /**
     * @return the organization
     */
    public OrganizationBean getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(OrganizationBean organization) {
        this.organization = organization;
    }
}
