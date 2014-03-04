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
package org.overlord.apiman.dt.ui.client.local.util;

import java.util.Date;
import java.util.List;

import org.overlord.apiman.dt.api.beans.members.MemberBean;
import org.overlord.apiman.dt.api.beans.members.MemberRoleBean;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Provides static methods to aid in formatting various objects for
 * display.
 *
 * @author eric.wittmann@redhat.com
 */
public class Formatting {
    
    /**
     * Formats just the date portion.
     * @param date
     */
    public static final String formatShortDate(Date date) {
        if (date == null)
            return "n/a"; //$NON-NLS-1$
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
        return format.format(date);
    }

    /**
     * Formats the member's roles into a comma separated string.
     * 
     * @param member
     */
    public static String formatRoles(MemberBean member) {
        StringBuilder builder = new StringBuilder();
        List<MemberRoleBean> roles = member.getRoles();
        boolean first = true;
        for (MemberRoleBean role : roles) {
            if (first) {
                first = false;
            } else {
                builder.append(", "); //$NON-NLS-1$
            }
            builder.append(role.getRoleName());
        }
        return builder.toString();
    }

}
