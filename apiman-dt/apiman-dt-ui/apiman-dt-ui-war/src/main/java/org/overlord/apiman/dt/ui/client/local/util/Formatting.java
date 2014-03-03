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
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
        return format.format(date);
    }

}
