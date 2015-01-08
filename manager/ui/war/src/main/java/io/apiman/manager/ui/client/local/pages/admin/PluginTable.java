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

import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.EditPluginPage;
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
 * A table of plugins.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class PluginTable extends TemplatedWidgetTable implements TakesValue<List<PluginSummaryBean>> {

    @Inject
    protected TranslationService i18n;
    
    @Inject
    protected TransitionAnchorFactory<EditPluginPage> editPluginLinkFactory;

    private List<PluginSummaryBean> plugins;

    /**
     * Constructor.
     */
    public PluginTable() {
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<PluginSummaryBean> value) {
        plugins = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (plugins != null && !plugins.isEmpty()) {
            int rowIdx = 0;
            for (PluginSummaryBean bean : plugins) {
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
    private void addRow(int rowIdx, PluginSummaryBean bean) {
        add(rowIdx, 0, new InlineLabel(bean.getName()));
        TransitionAnchor<EditPluginPage> anchor = editPluginLinkFactory.get("id", bean.getId().toString()); //$NON-NLS-1$
        anchor.setText(getCoordinates(bean));
        add(rowIdx, 1, anchor);
    }

    /**
     * Gets the plugin coordinates in string form.
     * @param bean
     */
    protected String getCoordinates(PluginSummaryBean bean) {
        StringBuilder builder = new StringBuilder();
        builder.append(bean.getGroupId());
        builder.append(':');
        builder.append(bean.getArtifactId());
        builder.append(':');
        builder.append(bean.getVersion());
        if (bean.getClassifier() != null) {
            builder.append('-').append(bean.getClassifier());
        }
        builder.append(':');
        if (bean.getType() == null) {
            builder.append("war"); //$NON-NLS-1$
        } else {
            builder.append(bean.getType());
        }

        return builder.toString();
    }

    /**
     * @return a widget to display when no items are found
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        return new NoEntitiesWidget(i18n.format(AppMessages.NO_PLUGINS_ADMIN_MESSAGE), false);
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#getValue()
     */
    @Override
    public List<PluginSummaryBean> getValue() {
        return plugins;
    }
}
