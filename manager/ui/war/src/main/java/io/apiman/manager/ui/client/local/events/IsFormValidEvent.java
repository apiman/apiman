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
 * Event fired when a form becomes valid (because a user has entered sufficient
 * information into it).
 *
 * @author eric.wittmann@redhat.com
 */
public class IsFormValidEvent extends GwtEvent<IsFormValidEvent.Handler> {

    /**
     * Handler for {@link IsFormValidEvent}.
     */
    public static interface Handler extends EventHandler {

        /**
         * Called when {@link IsFormValidEvent} is fired.
         *
         * @param event the {@link IsFormValidEvent} that was fired
         */
        public void onIsFormValid(IsFormValidEvent event);
    }

    /**
     * Indicates if a widget supports ok/cancel.
     */
    public static interface HasIsFormValidHandlers extends HasHandlers {

        /**
         * Adds a handler to the widget.
         * @param handler
         */
        public HandlerRegistration addIsFormValidHandler(Handler handler);

    }

    private static Type<Handler> TYPE;

    /**
     * Fires the event.
     * @param source
     * @param validity
     */
    public static IsFormValidEvent fire(HasHandlers source, Boolean validity) {
        IsFormValidEvent event = new IsFormValidEvent(validity);
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

    private Boolean validity;

    /**
     * Constructor.
     * @param validity
     */
    public IsFormValidEvent(Boolean validity) {
        this.setValidity(validity);
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
        handler.onIsFormValid(this);
    }

    /**
     * @return the validity
     */
    public Boolean isValid() {
        return validity;
    }

    /**
     * @param validity the validity to set
     */
    public void setValidity(Boolean validity) {
        this.validity = validity;
    }
}
