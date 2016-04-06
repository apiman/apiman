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
package io.apiman.manager.api.beans.apis;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Models a gateway that an API should be published to.
 *
 * @author eric.wittmann@redhat.com
 */
@Embeddable
public class ApiGatewayBean implements Serializable {

    private static final long serialVersionUID = 3128363408009800282L;

    @Column(name = "gateway_id", nullable=false)
    private String gatewayId;

    /**
     * Constructor.
     */
    public ApiGatewayBean() {
    }

    /**
     * @return the gatewayId
     */
    public String getGatewayId() {
        return gatewayId;
    }

    /**
     * @param gatewayId the gatewayId to set
     */
    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gatewayId == null) ? 0 : gatewayId.hashCode());
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
        ApiGatewayBean other = (ApiGatewayBean) obj;
        if (gatewayId == null) {
            if (other.gatewayId != null)
                return false;
        } else if (!gatewayId.equals(other.gatewayId))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "APIGatewayBean [gatewayId=" + gatewayId + "]";
    }

}
