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

package io.apiman.manager.api.rest.impl;

import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.core.IDownloadManager;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.contract.IDownloadResource;
import io.apiman.manager.api.rest.contract.ISystemResource;
import io.apiman.manager.api.rest.contract.exceptions.DownloadNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Implementation of the System API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class DownloadResourceImpl implements IDownloadResource {

    @Inject
    private IDownloadManager downloadManager;
    
    @Inject
    private ISystemResource system;

    @Context
    private HttpServletRequest request;

    /**
     * Constructor.
     */
    public DownloadResourceImpl() {
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IDownloadResource#download(java.lang.String)
     */
    @Override
    public Response download(String downloadId) throws DownloadNotFoundException {
        DownloadBean download;
        try {
            download = downloadManager.getDownload(downloadId);
            if (download == null) {
                throw new DownloadNotFoundException();
            }
        } catch (StorageException e) {
            throw new DownloadNotFoundException(e);
        }
        switch (download.getType()) {
            case apiRegistryJson:
                return null;
            case apiRegistryXml:
                return null;
            case exportJson:
                return system.exportData();
            default:
                throw new DownloadNotFoundException();
        }
    }

    /**
     * @return the downloadManager
     */
    public IDownloadManager getDownloadManager() {
        return downloadManager;
    }

    /**
     * @param downloadManager the downloadManager to set
     */
    public void setDownloadManager(IDownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }
}
