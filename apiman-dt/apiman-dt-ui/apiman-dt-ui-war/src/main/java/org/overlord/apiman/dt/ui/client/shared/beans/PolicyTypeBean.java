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
package org.overlord.apiman.dt.ui.client.shared.beans;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models a single policy type in the UI.  This is used, for example, to present
 * the user with a drop-down of possible policies she could add.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class PolicyTypeBean implements Serializable {

    private static final long serialVersionUID = -8567696386427739137L;
    
    private String label;
    private String impl;
    private String icon;

    /**
     * Constructor.
     */
    public PolicyTypeBean() {
    }
    
    /**
     * Constructor.
     * @param label
     * @param impl
     * @param icon
     */
    public PolicyTypeBean(String label, String impl, String icon) {
        this.setLabel(label);
        this.setImpl(impl);
        this.setIcon(icon);
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the impl
     */
    public String getImpl() {
        return impl;
    }

    /**
     * @param impl the impl to set
     */
    public void setImpl(String impl) {
        this.impl = impl;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((impl == null) ? 0 : impl.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PolicyTypeBean other = (PolicyTypeBean) obj;
        if (impl == null) {
            if (other.impl != null)
                return false;
        } else if (!impl.equals(other.impl))
            return false;
        return true;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
}
