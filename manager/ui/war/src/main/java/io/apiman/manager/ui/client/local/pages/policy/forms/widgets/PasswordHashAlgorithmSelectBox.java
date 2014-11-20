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
package io.apiman.manager.ui.client.local.pages.policy.forms.widgets;

import io.apiman.gateway.engine.policies.config.basicauth.PasswordHashAlgorithmType;
import io.apiman.manager.ui.client.local.pages.common.SelectBox;

import java.util.ArrayList;
import java.util.List;

/**
 * All the supported hashing algorithms.
 *
 * @author eric.wittmann@redhat.com
 */
public class PasswordHashAlgorithmSelectBox extends SelectBox<PasswordHashAlgorithmType> {

    private static final List<PasswordHashAlgorithmType> OPTIONS = new ArrayList<PasswordHashAlgorithmType>();
    static {
        OPTIONS.add(PasswordHashAlgorithmType.None);
        OPTIONS.add(PasswordHashAlgorithmType.MD5);
        OPTIONS.add(PasswordHashAlgorithmType.SHA1);
        OPTIONS.add(PasswordHashAlgorithmType.SHA256);
        OPTIONS.add(PasswordHashAlgorithmType.SHA384);
        OPTIONS.add(PasswordHashAlgorithmType.SHA512);
    }

    /**
     * Constructor.
     */
    public PasswordHashAlgorithmSelectBox() {
        setOptions(OPTIONS);
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
     */
    @Override
    protected String optionName(PasswordHashAlgorithmType option) {
        return option.name();
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
     */
    @Override
    protected String optionValue(PasswordHashAlgorithmType option) {
        return option.name();
    }
}
