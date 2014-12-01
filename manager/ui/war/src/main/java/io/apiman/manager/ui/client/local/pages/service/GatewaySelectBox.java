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
package io.apiman.manager.ui.client.local.pages.service;

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.ui.client.local.pages.common.SelectBox;

import java.util.List;

/**
 * A UI selector for the API endpoint type.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewaySelectBox extends SelectBox<GatewayBean> {
    
    /**
     * Constructor.
     */
    public GatewaySelectBox() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
     */
    @Override
    protected String optionName(GatewayBean option) {
        return option.getName();
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
     */
    @Override
    protected String optionValue(GatewayBean option) {
        return option.getId();
    }

    /**
     * @param gatewayId
     */
    public void selectGatewayById(String gatewayId) {
        List<GatewayBean> theOptions = getOptions();
        for (GatewayBean gatewayBean : theOptions) {
            if (gatewayBean.getId().equals(gatewayId)) {
                setValue(gatewayBean);
                return;
            }
        }
    }

}
