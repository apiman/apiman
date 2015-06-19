/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.beans.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean returned for the "Usage per App" metric.
 *
 * @author eric.wittmann@redhat.com
 */
public class UsagePerAppBean {

    private Map<String, Long> data = new HashMap<>();

    /**
     * Constructor.
     */
    public UsagePerAppBean() {
    }

    /**
     * @return the data
     */
    public Map<String, Long> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Map<String, Long> data) {
        this.data = data;
    }

}
