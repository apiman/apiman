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
package org.overlord.apiman.dt.ui.client.local.pages.admin;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.EditRolePage;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
import org.overlord.apiman.dt.ui.client.local.services.NavigationHelperService;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.AnchorPanel;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * List of roles.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class RoleList extends FlowPanel implements TakesValue<List<RoleBean>> {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    @Inject
    protected TransitionAnchorFactory<EditRolePage> toEditRoleFactory;
    
    private List<RoleBean> roles;
    private boolean filtered;

    /**
     * Constructor.
     */
    public RoleList() {
        getElement().setClassName("apiman-roles"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<RoleBean> getValue() {
        return roles;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(List<RoleBean> value) {
        filtered = true;
        roles = value;
        clear();
        refresh();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<RoleBean> value) {
        filtered = false;
        roles = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (roles != null && !roles.isEmpty()) {
            for (RoleBean bean : roles) {
                Widget row = createRow(bean);
                add(row);
            }
        } else {
            add(createNoEntitiesWidget());
        }
    }

    /**
     * @return a widget to display when no items are found
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered()) {
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_ROLES_ADMIN_MESSAGE), true);
        } else {
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_ROLES_ADMIN_MESSAGE), true);
        }
    }

    /**
     * Creates a single plan row.
     * @param bean
     */
    private Widget createRow(RoleBean bean) {
        FlowPanel container = new FlowPanel();
        container.getElement().setClassName("container-fluid"); //$NON-NLS-1$
        container.getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        
        FlowPanel row1 = new FlowPanel();
        container.add(row1);
        row1.getElement().setClassName("row"); //$NON-NLS-1$
        
        createTitle(bean, row1);
        if (bean.getAutoGrant() != null && bean.getAutoGrant().booleanValue()) {
            createAutoGrantIcon(row1);
        }

        FlowPanel row2 = new FlowPanel();
        row2.getElement().getStyle().setMarginBottom(8, Unit.PX);
        container.add(row2);
        row2.getElement().setClassName("row"); //$NON-NLS-1$
        createDescription(bean, row2);
        
        FlowPanel row3 = new FlowPanel();
        container.add(row3);
        row3.getElement().setClassName("row"); //$NON-NLS-1$
        createPermissions(bean, row3);
        
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$
        
        return container;
    }

    /**
     * Creates the title section of a row.
     * @param bean
     * @param row1
     */
    protected void createTitle(RoleBean bean, FlowPanel row) {
        SpanPanel sp = new SpanPanel();
        row.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        Anchor a = toEditRoleFactory.get(MultimapUtil.singleItemMap("id", bean.getId())); //$NON-NLS-1$
        sp.add(a);
        a.setText(bean.getName());
    }

    /**
     * Creates the auto-grant icon.
     * @param row
     */
    private void createAutoGrantIcon(FlowPanel row) {
        AnchorPanel a = new AnchorPanel();
        a.getElement().setClassName("apiman-summaryrow-icon"); //$NON-NLS-1$
        row.add(a);
        
        FontAwesomeIcon icon = new FontAwesomeIcon("check", true); //$NON-NLS-1$
        a.add(icon);
        
        InlineLabel label = new InlineLabel(i18n.format(AppMessages.AUTO_GRANTED_TO_CREATOR));
        a.add(label);
        label.getElement().setClassName("title-summary-item"); //$NON-NLS-1$
    }

    /**
     * Creates the description area of the plan listing.
     * @param bean
     * @param row
     */
    protected void createDescription(RoleBean bean, FlowPanel row) {
        InlineLabel description = new InlineLabel(bean.getDescription());
        row.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
        description.getElement().addClassName("apiman-label-faded"); //$NON-NLS-1$
    }

    /**
     * Creates the description area of the plan listing.
     * @param bean
     * @param row
     */
    protected void createPermissions(RoleBean bean, FlowPanel row) {
        FlowPanel permDiv = new FlowPanel();
        permDiv.getElement().setClassName("permissions"); //$NON-NLS-1$
        row.add(permDiv);
        
        InlineLabel label = new InlineLabel(i18n.format(AppMessages.GRANTS_PERMISSIONS) + " "); //$NON-NLS-1$
        label.getElement().setClassName("emphasis"); //$NON-NLS-1$
        permDiv.add(label);
        
        String permissionsTxt = generatePermissionsText(bean);
        InlineLabel permLabel = new InlineLabel(permissionsTxt);
        permLabel.getElement().setClassName("description"); //$NON-NLS-1$
        permDiv.add(permLabel);
    }

    /**
     * Generates a CSV of the permissions for the role.
     * @param bean
     */
    private String generatePermissionsText(RoleBean bean) {
        Set<String> permissions = bean.getPermissions();
        TreeSet<String> sortedPerms = new TreeSet<String>();
        for (String permission : permissions) {
            String i18nTxt = lookup(permission);
            if (i18nTxt != null) {
                sortedPerms.add(i18nTxt);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String permission : sortedPerms) {
            if (!first) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append(permission);
            first = false;
        }
        return sb.toString();
    }

    /**
     * Maps the permission string to an i18n value.
     * @param permission
     */
    private String lookup(String permission) {
        PermissionType type = PermissionType.valueOf(permission);
        switch(type) {
        case appAdmin:
            return i18n.format(AppMessages.PERMISSION_APP_ADMIN);
        case appEdit:
            return i18n.format(AppMessages.PERMISSION_APP_EDIT);
        case appView:
            return i18n.format(AppMessages.PERMISSION_APP_VIEW);
        case orgAdmin:
            return i18n.format(AppMessages.PERMISSION_ORG_ADMIN);
        case orgEdit:
            return i18n.format(AppMessages.PERMISSION_ORG_EDIT);
        case orgView:
            return i18n.format(AppMessages.PERMISSION_ORG_VIEW);
        case planAdmin:
            return i18n.format(AppMessages.PERMISSION_PLAN_ADMIN);
        case planEdit:
            return i18n.format(AppMessages.PERMISSION_PLAN_EDIT);
        case planView:
            return i18n.format(AppMessages.PERMISSION_PLAN_VIEW);
        case svcAdmin:
            return i18n.format(AppMessages.PERMISSION_SVC_ADMIN);
        case svcEdit:
            return i18n.format(AppMessages.PERMISSION_SVC_EDIT);
        case svcView:
            return i18n.format(AppMessages.PERMISSION_SVC_VIEW);
        }
        return null;
    }

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }

}
