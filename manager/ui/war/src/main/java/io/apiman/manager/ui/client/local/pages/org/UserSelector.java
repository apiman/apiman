/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.manager.ui.client.local.pages.org;

import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.ui.client.local.AppMessages;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Shows the user a list of users and allows her to select one.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class UserSelector extends FlowPanel implements HasValue<UserBean> {
    
    private UserBean value;
    private Anchor selectedRow;
    private boolean enabled = true;
    
    @Inject TranslationService i18n;
    
    /**
     * Constructor.
     */
    public UserSelector() {
    }
    
    /**
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            getElement().removeClassName("disabled"); //$NON-NLS-1$
        else
            getElement().addClassName("disabled"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<UserBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Called to display a list of users to choose from.
     * @param users
     */
    public void setUsers(List<UserBean> users) {
        clear();
        if (users.isEmpty()) {
            add(new Label(i18n.format(AppMessages.USER_SELECTOR_NONE_FOUND)));
        } else {
            for (UserBean userBean : users) {
                Widget row = createUserRow(userBean);
                add(row);
            }
        }
    }

    /**
     * Creates a row in the output table for each user.
     * @param userBean
     */
    private Widget createUserRow(final UserBean userBean) {
        final Anchor a = new Anchor();
        a.getElement().setClassName("item"); //$NON-NLS-1$
        StringBuilder builder = new StringBuilder();
        builder.append("<i class=\"fa fa-user fa-fw\"></i> "); //$NON-NLS-1$
        builder.append("<span class=\"\">" + userBean.getFullName() + "</span> "); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("<span>(</span><span>" + userBean.getUsername() + "</span><span>)</span>"); //$NON-NLS-1$ //$NON-NLS-2$
        a.setHTML(builder.toString());
        a.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (enabled)
                    onUserRowSelected(a, userBean);
            }
        });
        return a;
    }

    /**
     * Called when the user clicks on a user/row in the list.
     * @param row
     * @param userBean
     */
    protected void onUserRowSelected(Anchor row, UserBean userBean) {
        if (row == selectedRow)
            return;
        if (selectedRow != null) {
            selectedRow.getElement().removeClassName("selected"); //$NON-NLS-1$
            selectedRow = null;
        }
        row.getElement().addClassName("selected"); //$NON-NLS-1$
        selectedRow = row;
        setValue(userBean, true);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public UserBean getValue() {
        return value;
    }
    
    /**
     * @see com.google.gwt.user.client.ui.FlowPanel#clear()
     */
    @Override
    public void clear() {
        super.clear();
        this.value = null;
        selectedRow = null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(UserBean value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(UserBean value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

}
