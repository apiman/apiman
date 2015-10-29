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

package io.apiman.manager.api.beans.download;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Models a temporary download link.  A single download link is active
 * and available for a limited amount of time.  The /download REST 
 * endpoint is used to fetch the data from one of these links.  Each
 * link has a type and a URL/path.  The download itself will be handled
 * by whatever REST resource originally created it.  For example, if
 * the /system/export REST endpoint created a temporary download link,
 * then when the link is hit, the download resource will ask the 
 * system resource to satisfy the request.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "downloads")
public class DownloadBean {
    
    @Id
    @Column(updatable=false, nullable=false)
    private String id;
    @Column
    private DownloadType type;
    @Column
    private String path;
    @Column(updatable=false)
    private Date expires;
    
    /**
     * Constructor.
     */
    public DownloadBean() {
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public DownloadType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(DownloadType type) {
        this.type = type;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the expires
     */
    public Date getExpires() {
        return expires;
    }

    /**
     * @param expires the expires to set
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }

}
