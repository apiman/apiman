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
package org.overlord.apiman.dt.ui.client.local.pages.common;

import java.util.ArrayList;
import java.util.List;

import org.overlord.apiman.dt.ui.client.local.services.LoggerSeverity;

/**
 * Used to select the severity in the log viewer.
 *
 * @author eric.wittmann@redhat.com
 */
public class SeveritySelectBox extends SelectBox<LoggerSeverity> {
    
    private static final List<LoggerSeverity> OPTIONS = new ArrayList<LoggerSeverity>();
    static {
        OPTIONS.add(LoggerSeverity.Debug);
        OPTIONS.add(LoggerSeverity.Info);
        OPTIONS.add(LoggerSeverity.Warning);
        OPTIONS.add(LoggerSeverity.Error);
    }
    
    /**
     * Constructor.
     */
    public SeveritySelectBox() {
        setOptions(OPTIONS);
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
     */
    @Override
    protected String optionName(LoggerSeverity option) {
        return option.name();
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
     */
    @Override
    protected String optionValue(LoggerSeverity option) {
        return option.name();
    }

}
