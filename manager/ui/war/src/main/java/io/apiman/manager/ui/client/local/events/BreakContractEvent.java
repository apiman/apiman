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

import io.apiman.manager.api.beans.summary.ContractSummaryBean;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Event fired when the user clicks the Break Contract link in the list of 
 * contracts for an application.
 *
 * @author eric.wittmann@redhat.com
 */
public class BreakContractEvent extends GwtEvent<BreakContractEvent.Handler> {

    /**
     * Handler for {@link BreakContractEvent}.
     */
    public static interface Handler extends EventHandler {

        /**
         * Called when {@link BreakContractEvent} is fired.
         *
         * @param event the {@link BreakContractEvent} that was fired
         */
        public void onBreakContract(BreakContractEvent event);
    }

    /**
     * Indicates if a widget supports ok/cancel.
     */
    public static interface HasBreakContractHandlers extends HasHandlers {

        /**
         * Adds a handler to the widget.
         * @param handler
         */
        public HandlerRegistration addBreakContractHandler(Handler handler);

    }

    private static Type<Handler> TYPE;

    /**
     * Fires the event.
     * @param source
     * @param contract
     */
    public static BreakContractEvent fire(HasHandlers source, ContractSummaryBean contract) {
        BreakContractEvent event = new BreakContractEvent(contract);
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

    private ContractSummaryBean contract;

    /**
     * Constructor.
     * @param contract
     */
    public BreakContractEvent(ContractSummaryBean contract) {
        this.setContract(contract);
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
        handler.onBreakContract(this);
    }

    /**
     * @return the contract
     */
    public ContractSummaryBean getContract() {
        return contract;
    }

    /**
     * @param contract the contract to set
     */
    public void setContract(ContractSummaryBean contract) {
        this.contract = contract;
    }
}
