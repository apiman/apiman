/*
 * Copyright 2015 JBoss Inc
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
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;

/**
 * Event fired by a widget capable of tracking whether the mouse has entered it.
 *
 * @author eric.wittmann@redhat.com
 */
public class MouseInEvent extends GwtEvent<MouseInEvent.Handler> {

    /**
     * Handler for {@link MouseInEvent}.
     */
    public static interface Handler extends EventHandler {

        /**
         * Called when {@link MouseInEvent} is fired.
         *
         * @param event the {@link MouseInEvent} that was fired
         */
        public void onMouseIn(MouseInEvent event);
    }

    /**
     * Indicates if a widget supports ok/cancel.
     */
    public static interface HasMouseInHandlers extends HasHandlers {

        /**
         * Adds an ok/cancel handler to the widget.
         * @param handler
         */
        public HandlerRegistration addMouseInHandler(Handler handler);

    }

    private static Type<Handler> TYPE;

    /**
     * Fires the event.
     *
     * @param source the source of the event
     * @param sortList the {@link ColumnSortList} of sorted columns
     * @return the {@link ColumnSortEvent} that was fired
     */
    public static MouseInEvent fire(HasHandlers source) {
        MouseInEvent event = new MouseInEvent();
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

    /**
     * Constructor.
     */
    public MouseInEvent() {
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
        handler.onMouseIn(this);
    }
}