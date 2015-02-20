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
package io.apiman.gateway.test.policies.beans;

/**
 * A simple data bean.  Used for testing.
 *
 * @author eric.wittmann@redhat.com
 */
public class DataBean {
    
    private String property1;
    private boolean property2;
    private int property3;
    
    /**
     * Constructor.
     */
    public DataBean() {
    }

    /**
     * @return the property1
     */
    public String getProperty1() {
        return property1;
    }

    /**
     * @param property1 the property1 to set
     */
    public void setProperty1(String property1) {
        this.property1 = property1;
    }

    /**
     * @return the property2
     */
    public boolean isProperty2() {
        return property2;
    }

    /**
     * @param property2 the property2 to set
     */
    public void setProperty2(boolean property2) {
        this.property2 = property2;
    }

    /**
     * @return the property3
     */
    public int getProperty3() {
        return property3;
    }

    /**
     * @param property3 the property3 to set
     */
    public void setProperty3(int property3) {
        this.property3 = property3;
    }

}
