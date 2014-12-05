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
 * Event fired when the user clicks either Yes or No on the confirmation
 * dialog.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfirmationEvent extends GwtEvent<ConfirmationEvent.Handler> {

    /**
     * Handler for {@link ConfirmationEvent}.
     */
    public static interface Handler extends EventHandler {

        /**
         * Called when {@link ConfirmationEvent} is fired.
         *
         * @param event the {@link ConfirmationEvent} that was fired
         */
        public void onConfirmation(ConfirmationEvent event);
    }

    /**
     * Indicates if a widget supports ok/cancel.
     */
    public static interface HasConfirmationHandlers extends HasHandlers {

        /**
         * Adds a handler to the widget.
         * @param handler
         */
        public HandlerRegistration addConfirmationHandler(Handler handler);

    }

    private static Type<Handler> TYPE;

    /**
     * Fires the event.
     * @param source
     * @param confirmed
     */
    public static ConfirmationEvent fire(HasHandlers source, Boolean confirmed) {
        ConfirmationEvent event = new ConfirmationEvent(confirmed);
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

    private Boolean confirmed;

    /**
     * Constructor.
     * @param confirmed
     */
    public ConfirmationEvent(Boolean confirmed) {
        this.setConfirmed(confirmed);
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
        handler.onConfirmation(this);
    }

    /**
     * @return the confirmed
     */
    public Boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @param confirmed the confirmed to set
     */
    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
}
