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
package io.apiman.manager.ui.client.local.pages.admin;

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.EditGatewayPage;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.TemplatedWidgetTable;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table of gateways.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class GatewayTable extends TemplatedWidgetTable implements TakesValue<List<GatewayBean>> {

    @Inject
    protected TranslationService i18n;
    
    @Inject
    protected TransitionAnchorFactory<EditGatewayPage> editGatewayLinkFactory;

    private List<GatewayBean> gateways;

    /**
     * Constructor.
     */
    public GatewayTable() {
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<GatewayBean> value) {
        gateways = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (gateways != null && !gateways.isEmpty()) {
            int rowIdx = 0;
            for (GatewayBean bean : gateways) {
                addRow(rowIdx++, bean);
            }
        } else {
            Element tdElement = add(0, 0, createNoEntitiesWidget());
            tdElement.setAttribute("colspan", "2"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Adds a row to the table.
     * @param rowIdx
     * @param bean
     */
    private void addRow(int rowIdx, GatewayBean bean) {
        TransitionAnchor<EditGatewayPage> anchor = editGatewayLinkFactory.get("id", bean.getId()); //$NON-NLS-1$
        anchor.setText(bean.getName());
        add(rowIdx, 0, anchor);
        add(rowIdx, 1, new InlineLabel(bean.getType().name()));
    }

    /**
     * @return a widget to display when no items are found
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        return new NoEntitiesWidget(i18n.format(AppMessages.NO_GATEWAYS_ADMIN_MESSAGE), false);
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#getValue()
     */
    @Override
    public List<GatewayBean> getValue() {
        return gateways;
    }
}
